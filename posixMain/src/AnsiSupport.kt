// port-lint: source ansi_support.rs
package io.github.kotlinmania.crossterm

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString
import platform.posix.getenv

/**
 * Native (POSIX and Windows/MinGW) implementation of ANSI support detection.
 *
 * On Unix-like systems:
 * - Most terminals support ANSI escape sequences by default
 * - Returns false only if TERM is set to "dumb"
 *
 * On Windows (MinGW):
 * - Uses the same POSIX getenv to check TERM
 * - Note: Full Windows support would require enabling virtual terminal
 *   processing via Windows API, which is not implemented here
 */
@OptIn(ExperimentalForeignApi::class)
internal actual fun detectAnsiSupport(): Boolean {
    // Some terminals on Windows like GitBash can't use WinAPI calls directly
    // so when we try to enable the ANSI-flag for Windows this won't work.
    // Because of that we should check first if the TERM-variable is set
    // and see if the current terminal is a terminal who does support ANSI.
    //
    // On Unix-like systems, most terminals support ANSI by default.
    // We only return false if TERM is explicitly set to "dumb".
    val term = getenv("TERM")?.toKString()

    // If TERM is not set, assume ANSI is supported (common on Unix)
    // If TERM is set, check that it's not "dumb"
    return term == null || term != "dumb"
}
