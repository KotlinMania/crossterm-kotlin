// port-lint: source ansi_support.rs
package io.github.kotlinmania.crossterm

/**
 * Android implementation of ANSI support detection.
 *
 * On Android, standard apps don't run in a terminal context.
 * When running in Termux or similar terminal emulators, ANSI
 * escape sequences may be supported.
 *
 * This implementation returns false since standard Android apps
 * typically don't have terminal access. Apps running in Termux
 * would need additional native code to properly detect support.
 *
 * @return Always returns false on standard Android
 */
internal actual fun detectAnsiSupport(): Boolean {
    // Android apps typically don't run in a terminal context
    // Termux and similar apps would need native code to detect ANSI support
    return false
}
