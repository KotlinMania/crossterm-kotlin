// port-lint: source terminal/sys/file_descriptor.rs
package io.github.kotlinmania.crossterm.terminal.sys

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.usePinned
import platform.posix.STDIN_FILENO
import platform.posix.close
import platform.posix.isatty
import platform.posix.open
import platform.posix.read
import platform.posix.O_RDWR

/**
 * A file descriptor wrapper.
 *
 * It allows to retrieve raw file descriptor, read from the file descriptor and
 * mainly it closes the file descriptor once closed.
 *
 * @property fd The raw file descriptor
 * @property closeOnDrop Whether to close the file descriptor when this object is closed
 */
@OptIn(ExperimentalForeignApi::class)
class FileDesc private constructor(
    private val fd: Int,
    private val closeOnDrop: Boolean
) : AutoCloseable {

    /**
     * Reads data from the file descriptor into the buffer.
     *
     * @param buffer The buffer to read into
     * @return The number of bytes read
     * @throws IllegalStateException if the read fails
     */
    fun read(buffer: ByteArray): Int {
        return buffer.usePinned { pinned ->
            val result = read(fd, pinned.addressOf(0), buffer.size.convert())
            if (result < 0) {
                throw IllegalStateException("Failed to read from file descriptor: errno=${platform.posix.errno}")
            }
            result.toInt()
        }
    }

    /**
     * Returns the underlying raw file descriptor.
     */
    fun rawFd(): Int = fd

    /**
     * Closes the file descriptor if closeOnDrop is true.
     */
    override fun close() {
        if (closeOnDrop) {
            // Note that errors are ignored when closing a file descriptor. The
            // reason for this is that if an error occurs we don't actually know if
            // the file descriptor was closed or not, and if we retried (for
            // something like EINTR), we might close another valid file descriptor
            // opened after we closed ours.
            close(fd)
        }
    }

    companion object {
        /**
         * Constructs a new `FileDesc` with the given raw file descriptor.
         *
         * @param fd Raw file descriptor
         * @param closeOnDrop Specify if the raw file descriptor should be closed
         *        once the `FileDesc` is closed
         * @return A new FileDesc instance
         */
        fun new(fd: Int, closeOnDrop: Boolean): FileDesc {
            return FileDesc(fd, closeOnDrop)
        }
    }
}

/**
 * Creates a file descriptor pointing to the standard input or `/dev/tty`.
 *
 * If standard input is a TTY, returns a file descriptor for stdin.
 * Otherwise, opens `/dev/tty` for reading and writing.
 *
 * @return A FileDesc pointing to the TTY
 * @throws IllegalStateException if /dev/tty cannot be opened
 */
@OptIn(ExperimentalForeignApi::class)
fun ttyFd(): FileDesc {
    return if (isatty(STDIN_FILENO) == 1) {
        FileDesc.new(STDIN_FILENO, closeOnDrop = false)
    } else {
        val fd = open("/dev/tty", O_RDWR)
        if (fd < 0) {
            throw IllegalStateException("Failed to open /dev/tty")
        }
        FileDesc.new(fd, closeOnDrop = true)
    }
}
