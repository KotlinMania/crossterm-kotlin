// port-lint: source event/sys/unix/waker/tty.rs
package io.github.kotlinmania.crossterm.event.sys.unix.waker

import io.github.kotlinmania.crossterm.event.sys.Waker
import kotlinx.atomicfu.locks.ReentrantLock
import kotlinx.atomicfu.locks.withLock
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.usePinned
import platform.posix.write

/**
 * Allows waking up the EventSource's tryRead() method.
 *
 * This waker wraps a Unix socket stream (the write end of a socketpair)
 * and uses it to signal wake events. When [wake] is called, a single byte
 * is written to the stream, causing any poll operation waiting on the
 * corresponding read end to return with readable status.
 *
 * This implementation corresponds to the Rust crossterm `tty.rs` waker,
 * which is used when the `use-dev-tty` feature flag is enabled.
 *
 * Thread-safe: The internal stream is protected by a mutex, allowing
 * [wake] to be called from any thread.
 *
 * @property writerFd The file descriptor for the write end of the Unix socket pair.
 */
@OptIn(ExperimentalForeignApi::class)
class TtyWaker private constructor(
    private val writerFd: Int
) : Waker {

    /**
     * Lock protecting the writer file descriptor.
     *
     * This ensures thread-safe access when calling [wake] from
     * multiple threads concurrently.
     */
    private val lock = ReentrantLock()

    companion object {
        /**
         * Creates a new [TtyWaker] with the given Unix stream writer.
         *
         * In the original Rust implementation, this takes a `UnixStream`
         * which is typically the write end of a `UnixStream::pair()`.
         * In Kotlin/Native, we take the raw file descriptor of the
         * socket's write end.
         *
         * @param writerFd The file descriptor for the write end of the socket pair.
         * @return A new [TtyWaker] instance.
         */
        fun new(writerFd: Int): TtyWaker = TtyWaker(writerFd)
    }

    /**
     * Wakes up the Poll associated with this waker.
     *
     * Writes a single byte (0x00) to the Unix socket stream, causing any
     * poll operation waiting on the read end to return with readable status.
     *
     * Readiness is set to readable (equivalent to `Ready::readable()` in Rust).
     *
     * This operation is thread-safe and can be called from any thread.
     *
     * @throws IllegalStateException if writing to the stream fails.
     */
    override fun wake() {
        lock.withLock {
            val buffer = byteArrayOf(0)
            buffer.usePinned { pinned ->
                val result = write(writerFd, pinned.addressOf(0), 1u.convert())
                if (result < 0) {
                    throw IllegalStateException("Failed to write to wake stream: errno=${platform.posix.errno}")
                }
            }
        }
    }
}
