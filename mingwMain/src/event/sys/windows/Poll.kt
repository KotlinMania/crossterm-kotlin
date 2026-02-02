// port-lint: source event/sys/windows/poll.rs
package io.github.kotlinmania.crossterm.event.sys.windows

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.memScoped
import platform.windows.GetStdHandle
import platform.windows.INVALID_HANDLE_VALUE
import platform.windows.STD_INPUT_HANDLE
import platform.windows.WaitForMultipleObjects
import platform.windows.WAIT_ABANDONED_0
import platform.windows.WAIT_FAILED
import platform.windows.WAIT_OBJECT_0
import platform.windows.WAIT_TIMEOUT
import platform.windows.WINBOOL
import platform.windows.HANDLE
import platform.windows.HANDLEVar
import kotlin.time.Duration

/**
 * Exception thrown when a poll operation is interrupted by a waker.
 *
 * This is analogous to Rust's wake mechanism where the poll returns
 * early to signal that an external wake was requested.
 */
class WakeInterruptException(message: String) : Exception(message)

/**
 * Windows API poll implementation for console input events.
 *
 * This class wraps Windows WaitForMultipleObjects to poll for console input
 * events with optional timeout support.
 */
class WinApiPoll private constructor(
    private val waker: Waker?
) {
    companion object {
        /**
         * Creates a new WinApiPoll without event-stream support.
         */
        fun new(): WinApiPoll = WinApiPoll(waker = null)

        /**
         * Creates a new WinApiPoll with event-stream support (includes waker).
         */
        fun newWithWaker(): WinApiPoll = WinApiPoll(waker = Waker.new())
    }

    /**
     * Polls for console input events.
     *
     * @param timeout Optional timeout duration. If null, waits indefinitely.
     * @return `true` if input is available, `false` if timeout elapsed, or throws on wake/error.
     * @throws InterruptedException if woken by the waker
     * @throws IllegalStateException if the poll operation fails
     */
    fun poll(timeout: Duration?): Boolean? {
        val dwMillis = timeout?.inWholeMilliseconds?.toUInt() ?: INFINITE

        // Get console input handle
        val consoleHandle = getCurrentInputHandle()

        // Build handles array - console handle and optionally waker semaphore
        val handles = if (waker != null) {
            listOf(consoleHandle, waker.semaphoreHandle())
        } else {
            listOf(consoleHandle)
        }

        val output = waitForMultipleObjects(handles, dwMillis)

        return when {
            output == WAIT_OBJECT_0 -> {
                // Input handle triggered
                true
            }
            waker != null && output == WAIT_OBJECT_0 + 1u -> {
                // Semaphore handle triggered (waker)
                waker.reset()
                throw WakeInterruptException("Poll operation was woken up by Waker.wake")
            }
            output == WAIT_TIMEOUT || output == WAIT_ABANDONED_0 -> {
                // Timeout elapsed
                null
            }
            output == WAIT_FAILED -> {
                throw IllegalStateException("WaitForMultipleObjects failed")
            }
            else -> {
                throw IllegalStateException("WaitForMultipleObjects returned unexpected result: $output")
            }
        }
    }

    /**
     * Returns the waker associated with this poll (if event-stream is enabled).
     */
    fun waker(): Waker? = waker?.clone()
}

// Windows constants
private const val INFINITE: UInt = 0xFFFFFFFFu

/**
 * Placeholder for Windows handle type.
 * Should be implemented using actual Windows API via Kotlin/Native.
 */
typealias WindowsHandle = HANDLE?

/**
 * Gets the current console input handle.
 */
@OptIn(ExperimentalForeignApi::class)
private fun getCurrentInputHandle(): WindowsHandle {
    val handle = GetStdHandle(STD_INPUT_HANDLE)
    if (handle == INVALID_HANDLE_VALUE) {
        throw IllegalStateException("Failed to get standard input handle")
    }
    return handle
}

/**
 * Waits for multiple objects.
 */
@OptIn(ExperimentalForeignApi::class)
private fun waitForMultipleObjects(handles: List<WindowsHandle>, timeout: UInt): UInt {
    memScoped {
        val handleArray = allocArray<HANDLEVar>(handles.size)
        handles.forEachIndexed { idx, handle ->
            handleArray[idx] = handle
        }

        return WaitForMultipleObjects(
            handles.size.toUInt(),
            handleArray,
            WINBOOL(0),
            timeout
        )
    }
}
