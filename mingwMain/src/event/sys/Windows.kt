// port-lint: source event/sys/windows.rs
package io.github.kotlinmania.crossterm.event.sys

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.windows.DWORDVar
import platform.windows.GetConsoleMode
import platform.windows.GetStdHandle
import platform.windows.INVALID_HANDLE_VALUE
import platform.windows.SetConsoleMode
import platform.windows.STD_INPUT_HANDLE
import kotlin.concurrent.atomics.AtomicLong
import kotlin.concurrent.atomics.ExperimentalAtomicApi

/**
 * Mouse mode flags for Windows Console.
 *
 * This combines the flags required to enable mouse capture:
 * - ENABLE_MOUSE_INPUT (0x0010): Enables mouse input events
 * - ENABLE_EXTENDED_FLAGS (0x0080): Enables extended flags
 * - ENABLE_WINDOW_INPUT (0x0008): Enables window buffer size events
 */
private const val ENABLE_MOUSE_MODE: UInt = 0x0098u // 0x0010 or 0x0080 or 0x0008

/**
 * Value indicating the console mode has not been initialized.
 *
 * This is `ULong.MAX_VALUE` to distinguish from any valid `UInt` console mode value.
 */
private const val UNINITIALIZED_CONSOLE_MODE: ULong = ULong.MAX_VALUE

/**
 * Stores the original console mode before mouse capture was enabled.
 *
 * This is either [UNINITIALIZED_CONSOLE_MODE] if not yet initialized, or a valid
 * `UInt` value (stored as `ULong`) representing the original console mode.
 */
@OptIn(ExperimentalAtomicApi::class)
private val originalConsoleMode: AtomicLong = AtomicLong(UNINITIALIZED_CONSOLE_MODE.toLong())

/**
 * Initializes the original console mode.
 *
 * This function stores the original console mode if it has not already been stored.
 * It uses compare-and-exchange to ensure thread safety and that the value is only
 * set once.
 *
 * @param mode The original console mode to store.
 */
@OptIn(ExperimentalAtomicApi::class)
private fun initOriginalConsoleMode(mode: UInt) {
    originalConsoleMode.compareAndSet(
        UNINITIALIZED_CONSOLE_MODE.toLong(),
        mode.toLong()
    )
}

/**
 * Returns the original console mode that was saved before mouse capture was enabled.
 *
 * @return The original console mode.
 * @throws IllegalStateException if the original console mode has not been initialized.
 */
@OptIn(ExperimentalAtomicApi::class)
private fun getOriginalConsoleMode(): UInt {
    val value = originalConsoleMode.load().toULong()
    if (value == UNINITIALIZED_CONSOLE_MODE) {
        throw IllegalStateException("Initial console modes not set")
    }
    return value.toUInt()
}

/**
 * Enables mouse capture for the Windows console.
 *
 * This function saves the current console mode (if not already saved) and then
 * sets the console to mouse capture mode. The original mode can later be restored
 * by calling [disableMouseCapture].
 *
 * ## Mouse Events
 *
 * When mouse capture is enabled, the console will report:
 * - Mouse button clicks
 * - Mouse movement
 * - Mouse wheel scrolling
 * - Window resize events
 *
 * ## Thread Safety
 *
 * This function is thread-safe. The original console mode is only saved once,
 * even if this function is called multiple times from different threads.
 *
 * @throws IllegalStateException if the console handle cannot be obtained or
 *         if the console mode cannot be read or set.
 */
@OptIn(ExperimentalForeignApi::class)
fun enableMouseCapture() {
    memScoped {
        val handle = GetStdHandle(STD_INPUT_HANDLE)
        if (handle == INVALID_HANDLE_VALUE) {
            throw IllegalStateException("Failed to get standard input handle")
        }

        val mode = alloc<DWORDVar>()
        if (GetConsoleMode(handle, mode.ptr) == 0) {
            throw IllegalStateException("Failed to get console mode")
        }

        // Save the original mode (only the first time)
        initOriginalConsoleMode(mode.value)

        // Enable mouse mode
        if (SetConsoleMode(handle, ENABLE_MOUSE_MODE) == 0) {
            throw IllegalStateException("Failed to enable mouse capture mode")
        }
    }
}

/**
 * Disables mouse capture and restores the original console mode.
 *
 * This function restores the console mode that was saved when [enableMouseCapture]
 * was first called. It should be called to clean up after mouse capture is no
 * longer needed.
 *
 * ## Prerequisites
 *
 * [enableMouseCapture] must have been called at least once before calling this
 * function, otherwise an [IllegalStateException] will be thrown.
 *
 * @throws IllegalStateException if the original console mode has not been saved,
 *         if the console handle cannot be obtained, or if the console mode cannot
 *         be set.
 */
@OptIn(ExperimentalForeignApi::class)
fun disableMouseCapture() {
    memScoped {
        val handle = GetStdHandle(STD_INPUT_HANDLE)
        if (handle == INVALID_HANDLE_VALUE) {
            throw IllegalStateException("Failed to get standard input handle")
        }

        val originalMode = getOriginalConsoleMode()
        if (SetConsoleMode(handle, originalMode) == 0) {
            throw IllegalStateException("Failed to restore console mode")
        }
    }
}
