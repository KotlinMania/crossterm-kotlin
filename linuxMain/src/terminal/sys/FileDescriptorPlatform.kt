// port-lint: source terminal/sys/file_descriptor.rs
package io.github.kotlinmania.crossterm.terminal.sys

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.posix.read
import platform.posix.ssize_t

/**
 * Linux-specific implementation to read from a file descriptor.
 * On Linux, size_t is ULong.
 */
@OptIn(ExperimentalForeignApi::class)
internal actual fun readFromFd(fd: Int, buffer: ByteArray): Int {
    return buffer.usePinned { pinned ->
        val result: ssize_t = read(fd, pinned.addressOf(0), buffer.size.toULong())
        if (result < 0) {
            throw IllegalStateException("Failed to read from file descriptor: errno=${platform.posix.errno}")
        }
        result.toInt()
    }
}
