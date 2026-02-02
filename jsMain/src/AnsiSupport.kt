// port-lint: source ansi_support.rs
package io.github.kotlinmania.crossterm

/**
 * JavaScript implementation of ANSI support detection.
 *
 * In browser environments, there is no terminal concept, so ANSI
 * escape sequences are not applicable.
 *
 * In Node.js environments, ANSI support depends on the terminal
 * being used. For simplicity, this returns false in browser context.
 *
 * @return Always returns false in browser environments
 */
internal actual fun detectAnsiSupport(): Boolean {
    // In browser JS, there's no terminal concept
    // In Node.js, ANSI support would depend on the terminal
    return false
}
