// port-lint: source terminal/sys/windows.rs
package io.github.kotlinmania.crossterm.terminal.sys

import io.github.kotlinmania.crossterm.terminal.WindowSize
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.windows.DWORDVar
import platform.windows.ENABLE_ECHO_INPUT
import platform.windows.ENABLE_LINE_INPUT
import platform.windows.ENABLE_PROCESSED_INPUT
import platform.windows.GetConsoleMode
import platform.windows.GetConsoleScreenBufferInfo
import platform.windows.GetStdHandle
import platform.windows.INVALID_HANDLE_VALUE
import platform.windows.SetConsoleMode
import platform.windows.STD_INPUT_HANDLE
import platform.windows.STD_OUTPUT_HANDLE
import platform.windows.CONSOLE_SCREEN_BUFFER_INFO

/**
 * Bits which cannot be set in raw mode.
 * When these bits are cleared, the terminal is in raw mode.
 */
private val NOT_RAW_MODE_MASK: UInt =
    (ENABLE_LINE_INPUT or ENABLE_ECHO_INPUT or ENABLE_PROCESSED_INPUT).toUInt()

/**
 * Checks whether raw mode is currently enabled.
 *
 * On Windows, raw mode is detected by checking that the LINE_INPUT, ECHO_INPUT,
 * and PROCESSED_INPUT flags are all cleared from the console mode.
 *
 * @return `true` if raw mode is enabled (none of the "cooked mode" bits are set),
 *         `false` otherwise
 * @throws IllegalStateException if the console mode cannot be read
 */
@OptIn(ExperimentalForeignApi::class)
actual fun isRawModeEnabled(): Boolean {
    memScoped {
        val handle = GetStdHandle(STD_INPUT_HANDLE)
        if (handle == INVALID_HANDLE_VALUE) {
            throw IllegalStateException("Failed to get standard input handle")
        }

        val mode = alloc<DWORDVar>()
        if (GetConsoleMode(handle, mode.ptr) == 0) {
            throw IllegalStateException("Failed to get console mode")
        }

        // Check if none of the "not raw" bits is set
        return (mode.value and NOT_RAW_MODE_MASK) == 0u
    }
}

/**
 * Enables raw mode for the terminal.
 *
 * On Windows, this clears the LINE_INPUT, ECHO_INPUT, and PROCESSED_INPUT
 * flags from the console mode, which:
 * - Disables line buffering (input is available immediately)
 * - Disables echo (characters are not echoed to the terminal)
 * - Disables special character processing (Ctrl-C, etc.)
 *
 * @throws IllegalStateException if raw mode cannot be enabled
 */
@OptIn(ExperimentalForeignApi::class)
actual fun enableRawMode() {
    memScoped {
        val handle = GetStdHandle(STD_INPUT_HANDLE)
        if (handle == INVALID_HANDLE_VALUE) {
            throw IllegalStateException("Failed to get standard input handle")
        }

        val mode = alloc<DWORDVar>()
        if (GetConsoleMode(handle, mode.ptr) == 0) {
            throw IllegalStateException("Failed to get console mode")
        }

        // Clear the raw mode mask bits
        val newMode = mode.value and NOT_RAW_MODE_MASK.inv()
        if (SetConsoleMode(handle, newMode) == 0) {
            throw IllegalStateException("Failed to set console mode to raw mode")
        }
    }
}

/**
 * Disables raw mode for the terminal.
 *
 * On Windows, this sets the LINE_INPUT, ECHO_INPUT, and PROCESSED_INPUT
 * flags on the console mode, restoring normal terminal behavior.
 *
 * @throws IllegalStateException if raw mode cannot be disabled
 */
@OptIn(ExperimentalForeignApi::class)
actual fun disableRawMode() {
    memScoped {
        val handle = GetStdHandle(STD_INPUT_HANDLE)
        if (handle == INVALID_HANDLE_VALUE) {
            throw IllegalStateException("Failed to get standard input handle")
        }

        val mode = alloc<DWORDVar>()
        if (GetConsoleMode(handle, mode.ptr) == 0) {
            throw IllegalStateException("Failed to get console mode")
        }

        // Set the raw mode mask bits (restore cooked mode)
        val newMode = mode.value or NOT_RAW_MODE_MASK
        if (SetConsoleMode(handle, newMode) == 0) {
            throw IllegalStateException("Failed to restore console mode")
        }
    }
}

/**
 * Returns the terminal size as a pair of (columns, rows).
 *
 * On Windows, this queries the console screen buffer info to get
 * the terminal dimensions. Note that Windows starts counting at 0,
 * while Unix starts at 1, so 1 is added to replicate Unix behavior.
 *
 * @return A pair of (columns, rows)
 * @throws IllegalStateException if the terminal size cannot be determined
 */
@OptIn(ExperimentalForeignApi::class)
actual fun size(): Pair<UShort, UShort> {
    memScoped {
        val handle = GetStdHandle(STD_OUTPUT_HANDLE)
        if (handle == INVALID_HANDLE_VALUE) {
            throw IllegalStateException("Failed to get standard output handle")
        }

        val csbi = alloc<CONSOLE_SCREEN_BUFFER_INFO>()
        if (GetConsoleScreenBufferInfo(handle, csbi.ptr) == 0) {
            throw IllegalStateException("Failed to get console screen buffer info")
        }

        // Calculate terminal size from the window rectangle
        // Windows starts counting at 0, Unix at 1, add one to replicate Unix behavior
        val width = (csbi.srWindow.Right - csbi.srWindow.Left + 1).toUShort()
        val height = (csbi.srWindow.Bottom - csbi.srWindow.Top + 1).toUShort()

        return Pair(width, height)
    }
}

/**
 * Returns the terminal window size including pixel dimensions.
 *
 * On Windows, pixel size information is not available through the
 * standard Windows Console API.
 *
 * @throws UnsupportedOperationException Window pixel size is not supported on Windows
 */
actual fun windowSize(): WindowSize {
    throw UnsupportedOperationException(
        "Window pixel size not implemented for the Windows API."
    )
}

/**
 * Queries the terminal's support for progressive keyboard enhancement.
 *
 * On Windows, this always returns `false` as keyboard enhancement
 * via the Kitty keyboard protocol is not supported.
 *
 * @return Always `false` on Windows
 */
actual fun supportsKeyboardEnhancement(): Boolean {
    return false
}
