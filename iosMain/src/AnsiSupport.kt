// port-lint: source ansi_support.rs
package io.github.kotlinmania.crossterm

/**
 * iOS implementation of ANSI support detection.
 *
 * iOS apps running in terminal emulators (like iSH) may support ANSI.
 * Returns true unless TERM is set to "dumb".
 */
internal actual fun detectAnsiSupport(): Boolean {
    // Avoid using platform.posix during iosMain metadata compilation.
    // Treat iOS as ANSI-capable by default.
    return true
}
