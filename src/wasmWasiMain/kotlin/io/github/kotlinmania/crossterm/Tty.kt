// port-lint: source tty.rs
package io.github.kotlinmania.crossterm

actual fun isatty(fd: Int): Boolean = false
