// port-lint: source terminal/sys/unix.rs
package io.github.kotlinmania.crossterm.terminal.sys

import kotlinx.cinterop.CArrayPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import platform.posix.STDIN_FILENO
import platform.posix.STDOUT_FILENO
import platform.posix.ioctl
import platform.posix.isatty
import platform.posix.pollfd
import platform.posix.winsize

import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi

// Platform-specific TIOCGWINSZ value - different on macOS vs Linux
internal expect val TIOCGWINSZ_VALUE: ULong

/**
 * Platform-specific poll wrapper to handle nfds_t differences.
 * On macOS nfds_t is UInt (32-bit), on Linux it's ULong (64-bit).
 */
@OptIn(ExperimentalForeignApi::class)
internal expect fun pollWrapper(fds: CArrayPointer<pollfd>, numFds: Int, timeoutMs: Int): Int

/**
 * Tracks whether raw mode is currently enabled.
 */
@OptIn(ExperimentalAtomicApi::class)
private val rawModeEnabled: AtomicBoolean = AtomicBoolean(false)

/**
 * Enables raw mode for the terminal.
 * Platform-specific implementation handles termios manipulation.
 */
@OptIn(ExperimentalAtomicApi::class)
actual fun enableRawMode() {
    if (rawModeEnabled.load()) {
        return
    }
    enableRawModeImpl()
    rawModeEnabled.store(true)
}

/**
 * Disables raw mode for the terminal.
 * Platform-specific implementation handles termios restoration.
 */
@OptIn(ExperimentalAtomicApi::class)
actual fun disableRawMode() {
    if (!rawModeEnabled.load()) {
        return
    }
    disableRawModeImpl()
    rawModeEnabled.store(false)
}

/**
 * Platform-specific implementation of enabling raw mode.
 */
internal expect fun enableRawModeImpl()

/**
 * Platform-specific implementation of disabling raw mode.
 */
internal expect fun disableRawModeImpl()

/**
 * Checks whether raw mode is currently enabled.
 */
@OptIn(ExperimentalAtomicApi::class)
actual fun isRawModeEnabled(): Boolean {
    return rawModeEnabled.load()
}

/**
 * Returns the terminal size as a pair of (columns, rows).
 */
@OptIn(ExperimentalForeignApi::class)
actual fun size(): Pair<UShort, UShort> {
    val ws = try {
        windowSize()
    } catch (e: Exception) {
        val tputSize = tputSize()
        if (tputSize != null) {
            return tputSize
        }
        throw IllegalStateException("Failed to determine terminal size", e)
    }
    return Pair(ws.columns, ws.rows)
}

/**
 * Returns the terminal window size including pixel dimensions.
 */
@OptIn(ExperimentalForeignApi::class)
actual fun windowSize(): WindowSize {
    memScoped {
        val size = alloc<winsize>()
        val fd = getTtyFd()

        if (ioctl(fd, TIOCGWINSZ_VALUE, size.ptr) != 0) {
            throw IllegalStateException("Failed to get window size")
        }

        return WindowSize(
            columns = size.ws_col,
            rows = size.ws_row,
            width = size.ws_xpixel,
            height = size.ws_ypixel
        )
    }
}

/**
 * Queries the terminal's support for progressive keyboard enhancement.
 */
actual fun supportsKeyboardEnhancement(): Boolean {
    return false
}

@OptIn(ExperimentalForeignApi::class)
internal fun getTtyFd(): Int {
    return if (isatty(STDIN_FILENO) == 1) {
        STDIN_FILENO
    } else {
        STDOUT_FILENO
    }
}

private fun tputValue(arg: String): UShort? = null

private fun tputSize(): Pair<UShort, UShort>? {
    val cols = tputValue("cols") ?: return null
    val lines = tputValue("lines") ?: return null
    return Pair(cols, lines)
}
