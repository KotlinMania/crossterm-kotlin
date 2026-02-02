// port-lint: source tty.rs
package io.github.kotlinmania.crossterm

/**
 * JavaScript implementation of isatty.
 *
 * In browser environments, there is no concept of TTY.
 * In Node.js, we could potentially use process.stdout.isTTY,
 * but for simplicity this always returns false.
 *
 * @param fd The file descriptor to check (ignored in JS)
 * @return Always returns false in browser environments
 */
actual fun isatty(fd: Int): Boolean {
    // In browser JS, there's no TTY concept
    // In Node.js, you would use process.stdout.isTTY
    return false
}
