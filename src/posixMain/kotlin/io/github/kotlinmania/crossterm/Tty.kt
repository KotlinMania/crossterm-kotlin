// port-lint: source tty.rs
@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package io.github.kotlinmania.crossterm

import kotlinx.cinterop.ExperimentalForeignApi
import platform.posix.isatty as posixIsatty

actual fun isatty(fd: Int): Boolean = posixIsatty(fd) == 1
