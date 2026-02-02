// port-lint: source tty.rs
package io.github.kotlinmania.crossterm

/**
 * WASM JavaScript implementation of isatty.
 *
 * In WASM/browser environments, there is no concept of TTY.
 *
 * @param fd The file descriptor to check (ignored in WASM)
 * @return Always returns false in WASM environments
 */
actual fun isatty(fd: Int): Boolean {
    // In WASM, there's no TTY concept
    return false
}
