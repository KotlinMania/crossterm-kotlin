// port-lint: source terminal/sys/file_descriptor.rs
package io.github.kotlinmania.crossterm.terminal.sys

import kotlinx.cinterop.ExperimentalForeignApi

/**
 * Platform-specific implementation to read from a file descriptor.
 * Different POSIX platforms have different size_t types (ULong on some, UInt on others).
 */
@OptIn(ExperimentalForeignApi::class)
internal expect fun readFromFd(
    fd: Int,
    buffer: ByteArray,
): Int
