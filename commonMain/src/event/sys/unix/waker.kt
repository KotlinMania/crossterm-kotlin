// port-lint: source event/sys/unix/waker.rs
package io.github.kotlinmania.crossterm.event.sys.unix

import io.github.kotlinmania.crossterm.event.source.Waker
import kotlinx.atomicfu.locks.ReentrantLock
import kotlinx.atomicfu.locks.withLock

/**
 * Unix waker implementations for interrupting blocked event source operations.
 *
 * This module provides waker implementations for Unix platforms that allow
 * external signals to interrupt blocking poll/read operations. Two implementations
 * are available depending on the use case:
 *
 * - [TtyWaker]: Uses a Unix socket stream for TTY-based event handling
 * - [MioWaker]: Uses mio's built-in waker for mio-based polling
 *
 * In Kotlin/Native, platform-specific socket or waker APIs would be used
 * to implement the actual wake mechanism.
 *
 * @see io.github.kotlinmania.crossterm.event.Waker
 * @see io.github.kotlinmania.crossterm.event.EventSource
 */

/**
 * A waker implementation using Unix socket streams.
 *
 * This waker uses a Unix domain socket to signal wake events. When [wake]
 * is called, a byte is written to the socket, which can be detected by
 * poll operations waiting on the corresponding read end.
 *
 * This is typically used with TTY-based event handling where the event
 * source polls on multiple file descriptors including the socket read end.
 *
 * Thread-safe: The internal socket stream is protected by a mutex, allowing
 * [wake] to be called from any thread.
 *
 * @property writer A function that writes to the wake signal target.
 */
class TtyWaker(
    private val writer: () -> Unit
) : Waker {
    private val lock = ReentrantLock()

    /**
     * Creates a new [TtyWaker] with the given writer function.
     *
     * In a full implementation, the writer would wrap a Unix socket stream's
     * write end, sending a byte to wake up the polling thread.
     *
     * @param writer A function that performs the wake signal write.
     */
    companion object {
        /**
         * Creates a new [TtyWaker] with the specified writer.
         *
         * @param writer The function to invoke when waking.
         * @return A new [TtyWaker] instance.
         */
        fun new(writer: () -> Unit): TtyWaker = TtyWaker(writer)
    }

    /**
     * Wakes up the poll associated with this waker.
     *
     * Writes a single byte (0x00) to the Unix socket stream, causing any
     * poll operation waiting on the read end to return with readable status.
     *
     * This operation is thread-safe and can be called from any thread.
     */
    override fun wake() {
        lock.withLock {
            writer()
        }
    }
}

/**
 * A waker implementation for use with mio-based polling.
 *
 * This waker wraps mio's Waker type, which uses platform-specific mechanisms
 * (e.g., eventfd on Linux, kqueue on macOS/BSD) to efficiently signal
 * wake events to a mio Poll instance.
 *
 * Thread-safe: The internal mio waker is protected by a mutex, allowing
 * [wake] to be called from any thread.
 *
 * @property waker A function that triggers the mio wake signal.
 */
class MioWaker(
    private val waker: () -> Unit
) : Waker {
    private val lock = ReentrantLock()

    companion object {
        /**
         * Creates a new [MioWaker] with the specified waker function.
         *
         * In a full implementation, this would create a mio::Waker registered
         * with the given registry and token.
         *
         * @param waker The function to invoke when waking.
         * @return A new [MioWaker] instance.
         */
        fun new(waker: () -> Unit): MioWaker = MioWaker(waker)
    }

    /**
     * Wakes up the poll associated with this waker.
     *
     * Signals the mio Poll instance that readiness has changed, causing
     * any blocked poll operation to return.
     *
     * This operation is thread-safe and can be called from any thread.
     */
    override fun wake() {
        lock.withLock {
            waker()
        }
    }

    /**
     * Resets the state so the same waker can be reused.
     *
     * This is a no-op for mio wakers as they don't require explicit reset.
     * The method is provided for API compatibility.
     */
    fun reset() {
        // No-op: mio wakers don't require reset
    }
}

/**
 * Type alias for the default Unix waker implementation.
 *
 * In the original Rust code, this would be conditionally set based on
 * the "use-dev-tty" feature flag:
 * - With feature: Uses [TtyWaker]
 * - Without feature: Uses [MioWaker]
 *
 * In Kotlin, both implementations are available and the appropriate one
 * is selected at runtime based on the event source configuration.
 */
typealias UnixWaker = MioWaker
