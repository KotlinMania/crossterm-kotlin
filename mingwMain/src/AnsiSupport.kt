// port-lint: source ansi_support.rs
package io.github.kotlinmania.crossterm

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.toKString
import kotlinx.cinterop.value
import platform.windows.DWORDVar
import platform.windows.ENABLE_VIRTUAL_TERMINAL_PROCESSING
import platform.windows.GetConsoleMode
import platform.windows.GetStdHandle
import platform.windows.INVALID_HANDLE_VALUE
import platform.windows.SetConsoleMode
import platform.windows.STD_OUTPUT_HANDLE
import platform.posix.getenv

/**
 * Windows implementation of ANSI support detection and enablement.
 *
 * On Windows 10 and later, ANSI escape sequences can be enabled by setting
 * the ENABLE_VIRTUAL_TERMINAL_PROCESSING flag on the console output handle.
 *
 * For terminals like GitBash that don't support WinAPI calls directly,
 * we fall back to checking the TERM environment variable.
 */
@OptIn(ExperimentalForeignApi::class)
internal actual fun detectAnsiSupport(): Boolean {
    // First, try to enable virtual terminal processing via Windows API
    val handle = GetStdHandle(STD_OUTPUT_HANDLE.toUInt())
    if (handle != INVALID_HANDLE_VALUE && handle != null) {
        memScoped {
            val mode = alloc<DWORDVar>()
            if (GetConsoleMode(handle, mode.ptr) != 0) {
                // Try to enable virtual terminal processing
                val newMode = mode.value or ENABLE_VIRTUAL_TERMINAL_PROCESSING.toUInt()
                if (SetConsoleMode(handle, newMode) != 0) {
                    return true
                }
            }
        }
    }

    // Fall back to checking TERM environment variable
    // Some terminals on Windows like GitBash can't use WinAPI calls directly
    // so when we try to enable the ANSI-flag for Windows this won't work.
    // Because of that we should check first if the TERM-variable is set
    // and see if the current terminal is a terminal who does support ANSI.
    val term = getenv("TERM")?.toKString()

    // If TERM is set and not "dumb", assume ANSI is supported
    return term != null && term != "dumb"
}
