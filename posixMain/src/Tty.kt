// port-lint: source tty.rs
package io.github.kotlinmania.crossterm

import platform.posix.isatty as posixIsatty

/**
 * On UNIX, the `isatty()` function returns true if a file
 * descriptor is a terminal.
 *
 * On Windows (mingw), the same POSIX `isatty()` function is available.
 *
 * @param fd The file descriptor to check
 * @return true if the file descriptor is a terminal, false otherwise
 */
actual fun isatty(fd: Int): Boolean {
    return posixIsatty(fd) == 1
}
