// port-lint: source event/sys/unix/waker/mio.rs
package io.github.kotlinmania.crossterm.event.sys.unix.waker

import kotlinx.atomicfu.locks.ReentrantLock
import kotlinx.atomicfu.locks.withLock
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.IntVar
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.convert
import kotlinx.cinterop.get
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.usePinned
import platform.posix.close
import platform.posix.pipe
import platform.posix.write

/**
 * Allows waking up a poll operation.
 *
 * This type wraps platform-specific wake mechanisms. On POSIX systems,
 * it uses a pipe to signal the polling thread - writing to the pipe's
 * write end causes poll() to return when waiting on the read end.
 *
 * This implementation corresponds to the Rust `mio::Waker`, which uses
 * platform-specific mechanisms (eventfd on Linux, kqueue on macOS/BSD)
 * under the hood. Since Kotlin/Native doesn't have mio, we use a simple
 * pipe-based approach that works across all POSIX platforms.
 *
 * Thread-safe: The internal pipe is protected by a mutex, allowing
 * [wake] to be called from any thread.
 */
@OptIn(ExperimentalForeignApi::class)
class MioWaker private constructor(
    private val readFd: Int,
    private val writeFd: Int
) : AutoCloseable {

    /**
     * Lock protecting the pipe file descriptors.
     *
     * This ensures thread-safe access when calling [wake] from
     * multiple threads concurrently.
     */
    private val lock = ReentrantLock()

    /**
     * Whether this waker has been closed.
     */
    private var closed = false

    companion object {
        /**
         * Token value for the waker in poll operations.
         *
         * In mio, each event source is associated with a token for identification.
         * When using this waker with poll(), register the [readFd] with this token
         * to identify wake events.
         */
        const val WAKE_TOKEN: Int = 2

        /**
         * Creates a new [Waker].
         *
         * In the original Rust implementation, this takes a mio Registry and Token
         * to register the waker with mio's Poll. In Kotlin/Native, we create a pipe
         * and the caller is responsible for registering the read end with their
         * poll mechanism.
         *
         * @return A new [MioWaker] instance.
         * @throws IllegalStateException if the pipe cannot be created.
         */
        fun new(): MioWaker {
            return memScoped {
                val pipeFds = allocArray<IntVar>(2)
                if (pipe(pipeFds) != 0) {
                    throw IllegalStateException("Failed to create wake pipe: errno=${platform.posix.errno}")
                }
                MioWaker(readFd = pipeFds[0], writeFd = pipeFds[1])
            }
        }
    }

    /**
     * Returns the file descriptor to poll on.
     *
     * Register this file descriptor with your poll mechanism (poll(), select(), etc.)
     * to receive wake notifications. When [wake] is called, this file descriptor
     * will become readable.
     *
     * @return The read end of the wake pipe.
     */
    fun pollFd(): Int = readFd

    /**
     * Wakes up the poll associated with this waker.
     *
     * Writes a single byte to the wake pipe, causing any poll operation
     * waiting on [pollFd] to return with readable status.
     *
     * Readiness is set to readable (equivalent to `Ready::readable()` in mio).
     *
     * This operation is thread-safe and can be called from any thread.
     *
     * @throws IllegalStateException if writing to the pipe fails.
     */
    fun wake() {
        lock.withLock {
            if (closed) {
                return
            }
            val buffer = byteArrayOf(1)
            buffer.usePinned { pinned ->
                val result = write(writeFd, pinned.addressOf(0), 1u.convert())
                if (result < 0) {
                    throw IllegalStateException("Failed to write to wake pipe: errno=${platform.posix.errno}")
                }
            }
        }
    }

    /**
     * Resets the state so the same waker can be reused.
     *
     * Note: the original Rust implementation returns `Ok(())` without doing anything.
     */
    fun reset() {
        // no-op
    }

    /**
     * Closes the waker, releasing the pipe file descriptors.
     *
     * After closing, [wake] and [reset] become no-ops.
     */
    override fun close() {
        lock.withLock {
            if (!closed) {
                close(readFd)
                close(writeFd)
                closed = true
            }
        }
    }
}
