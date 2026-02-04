// port-lint: source event/source.rs
package io.github.kotlinmania.crossterm.event.source

import io.github.kotlinmania.crossterm.event.InternalEvent
import io.github.kotlinmania.crossterm.event.sys.Waker
import kotlin.time.Duration

/**
 * An interface for trying to read an [InternalEvent] within an optional [Duration].
 *
 * This is the platform-specific interface that provides the underlying
 * event reading mechanism. Platform implementations (Unix, Windows) provide
 * the actual I/O operations.
 *
 * This trait is implemented by platform-specific event sources:
 * - Unix: Uses file descriptor polling with mio or similar
 * - Windows: Uses the Windows Console API
 */
interface EventSource {
    /**
     * Tries to read an [InternalEvent] within the given duration.
     *
     * @param timeout `null` to block indefinitely until an event is available,
     *   or a [Duration] to block for the given timeout.
     * @return The event if one is available, or `null` if the timeout elapsed
     *   without an event becoming available.
     * @throws Exception if an error occurs while reading from the event source.
     */
    fun tryRead(timeout: Duration?): InternalEvent?

    /**
     * Returns a [Waker] allowing to wake/force the [tryRead] method to return `null`.
     *
     * This is used for event-stream support to allow external signals to interrupt
     * a blocking poll operation. The waker can be used from another thread to
     * unblock a waiting read operation.
     *
     * @return A [Waker] instance for this event source, or `null` if waking is not supported.
     */
    fun waker(): Waker? = null
}
