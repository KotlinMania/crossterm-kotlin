// port-lint: source ansi_support.rs
package io.github.kotlinmania.crossterm

/**
 * WASM WASI implementation of ANSI support detection.
 *
 * In WASM/WASI environments, there is no terminal concept,
 * so ANSI escape sequences are not applicable.
 *
 * @return Always returns false in WASM environments
 */
internal actual fun detectAnsiSupport(): Boolean {
    // In WASM, there's no terminal concept
    return false
}
