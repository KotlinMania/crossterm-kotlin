// port-lint: ignore
package io.github.kotlinmania.crossterm.terminal.sys

import kotlinx.cinterop.CValuesRef
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.posix.ssize_t

// External declaration to work around different size_t types on different Apple platforms
@OptIn(ExperimentalForeignApi::class)
internal external fun read(__fd: Int, __buf: CValuesRef<*>?, __nbytes: ULong): ssize_t

/**
 * Apple-specific implementation to read from a file descriptor.
 * On Apple 64-bit platforms (iOS, macOS, tvOS 64-bit), size_t is ULong and ssize_t is Long.
 * On Apple 32-bit platforms (watchOS 32-bit), size_t is UInt and ssize_t is Int.
 * We use ULong as the common type and let the compiler handle conversion.
 */
@OptIn(ExperimentalForeignApi::class)
internal actual fun readFromFd(fd: Int, buffer: ByteArray): Int {
    return buffer.usePinned { pinned ->
        val result = read(fd, pinned.addressOf(0), buffer.size.toULong())
        if (result < 0) {
            throw IllegalStateException("Failed to read from file descriptor: errno=${platform.posix.errno}")
        }
        // On 32-bit platforms, result is already Int
        // On 64-bit platforms, result is Long
        @Suppress("USELESS_CAST")  // Suppressed because it's needed on 64-bit platforms
        (result as Number).toInt()
    }
}
