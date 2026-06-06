// port-lint: source terminal/sys/unix.rs
package io.github.kotlinmania.crossterm.terminal.sys

import io.github.kotlinmania.crossterm.terminal.WindowSize
import kotlinx.cinterop.ExperimentalForeignApi

/**
 * Android Native-specific implementation to get window size.
 * Note: ioctl is hard-deprecated on Android Native, and terminal operations
 * are not meaningful on Android apps anyway. This throws an exception.
 */
@OptIn(ExperimentalForeignApi::class)
internal actual fun windowSizeViaIoctl(fd: Int): WindowSize =
    throw UnsupportedOperationException("Terminal window size queries are not supported on Android Native")
