// port-lint: source ansi_support.rs
package io.github.kotlinmania.crossterm

import io.github.kotlinmania.crossterm.style.types.getEnvironmentVariable

object AnsiSupport {
    private val supportsAnsiEscapeCodes: Boolean by lazy {
        detectAnsiSupport()
    }

    fun supportsAnsi(): Boolean = supportsAnsiEscapeCodes
}

/**
 * Detects ANSI escape sequence support.
 *
 * On Windows: attempts to enable virtual terminal processing first,
 * then falls back to checking the TERM environment variable.
 * On all other platforms: checks that TERM is not "dumb".
 */
internal fun detectAnsiSupport(): Boolean {
    if (enableVtProcessing()) return true
    val term = getEnvironmentVariable("TERM")
    return term == null || term != "dumb"
}

/**
 * Enables virtual terminal processing on Windows.
 * Returns true if successful (or if not on Windows, where it's a no-op that returns true).
 * Returns false only on Windows when VT processing cannot be enabled.
 */
internal expect fun enableVtProcessing(): Boolean
