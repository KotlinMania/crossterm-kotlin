package io.github.kotlinmania.crossterm

actual fun isatty(fd: Int): Boolean = System.console() != null