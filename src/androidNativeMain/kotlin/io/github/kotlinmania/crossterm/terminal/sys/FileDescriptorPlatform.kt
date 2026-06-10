// port-lint: source terminal/sys/file_descriptor.rs
@file:OptIn(kotlinx.cinterop.UnsafeNumber::class)

package io.github.kotlinmania.crossterm.terminal.sys

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.usePinned
import platform.posix.read
import platform.posix.ssize_t

/**
 * Android Native-specific implementation to read from a file descriptor.
 * On Android Native 32-bit, size_t is UInt and ssize_t is Int.
 * On Android Native 64-bit, size_t is ULong and ssize_t is Long.
 * The generated POSIX binding accepts the platform size type, so convert the buffer size at the call site.
 */
@OptIn(ExperimentalForeignApi::class)
internal actual fun readFromFd(
    fd: Int,
    buffer: ByteArray,
): Int =
    buffer.usePinned { pinned ->
        val result: ssize_t = read(fd, pinned.addressOf(0), buffer.size.convert())
        if (result < 0) {
            throw IllegalStateException("Failed to read from file descriptor: errno=${platform.posix.errno}")
        }
        ssizeToInt(result)
    }

private fun ssizeToInt(value: Number): Int = value.toInt()
