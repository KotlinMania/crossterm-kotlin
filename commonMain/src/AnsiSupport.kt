// port-lint: source ansi_support.rs
package io.github.kotlinmania.crossterm

/**
 * ANSI escape code support detection and enablement.
 *
 * This module provides functionality to detect and enable ANSI escape sequence
 * support in the terminal. On Windows, this requires enabling virtual terminal
 * processing. On Unix-like systems (macOS, Linux), ANSI support is typically
 * available by default.
 *
 * The detection is performed lazily on first access and cached for subsequent calls.
 */
object AnsiSupport {
    /**
     * Lazily initialized ANSI support status.
     *
     * The detection runs once on first access and the result is cached.
     * Thread-safe by default.
     */
    private val supportsAnsiEscapeCodes: Boolean by lazy {
        detectAnsiSupport()
    }

    /**
     * Checks if the current terminal supports ANSI escape sequences.
     *
     * This function performs lazy initialization - on the first call, it will
     * attempt to detect (and on Windows, enable) ANSI support. The result is
     * cached for subsequent calls.
     *
     * On Windows:
     * - Attempts to enable virtual terminal processing via Windows API
     * - Falls back to checking the TERM environment variable for terminals
     *   like GitBash that don't support WinAPI calls directly
     *
     * On Unix-like systems (macOS, Linux):
     * - ANSI support is typically always available
     * - May check the TERM environment variable to exclude "dumb" terminals
     *
     * @return `true` if the terminal supports ANSI escape sequences, `false` otherwise
     *
     * Example:
     * ```kotlin
     * if (AnsiSupport.supportsAnsi()) {
     *     print("\u001B[31mRed text\u001B[0m")
     * } else {
     *     print("Red text (no color support)")
     * }
     * ```
     */
    fun supportsAnsi(): Boolean = supportsAnsiEscapeCodes
}

/**
 * Platform-specific detection of ANSI escape sequence support.
 *
 * Implementation details:
 * - On Windows: Attempts to enable virtual terminal processing, falls back to TERM check
 * - On Unix: Checks that TERM is not "dumb", or returns true if TERM is unset
 *   (most Unix terminals support ANSI by default)
 *
 * @return `true` if ANSI escape sequences are supported, `false` otherwise
 */
internal expect fun detectAnsiSupport(): Boolean
