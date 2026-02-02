// port-lint: source event/read.rs
package io.github.kotlinmania.crossterm.event

import io.github.kotlinmania.crossterm.event.source.EventSource
import io.github.kotlinmania.crossterm.event.source.Waker
import kotlin.time.Duration

/**
 * Can be used to read [InternalEvent]s.
 *
 * This reader maintains a queue of events and supports filtering operations
 * to find specific event types. It uses an [EventSource] to read events from
 * the underlying system.
 *
 * The reader maintains an internal queue of events and a separate buffer for
 * skipped events (those that don't match the current filter). This prevents
 * losing events when filtering and avoids infinite loops when re-evaluating
 * events.
 */
class InternalEventReader(
    private var source: EventSource? = null
) {
    private val events: ArrayDeque<InternalEvent> = ArrayDeque(32)
    private val skippedEvents: MutableList<InternalEvent> = ArrayList(32)

    companion object {
        /**
         * Creates a default [InternalEventReader] without an event source.
         *
         * The event source is platform-specific and will be initialized lazily
         * when events are first read. On Unix, this uses [UnixInternalEventSource],
         * and on Windows, this uses [WindowsEventSource].
         *
         * @return A new [InternalEventReader] instance.
         */
        fun default(): InternalEventReader = InternalEventReader(null)
    }

    /**
     * Returns a [Waker] allowing to wake/force the poll method to return false.
     *
     * This is used for event stream support where you need to interrupt
     * a blocking poll operation.
     *
     * @return The waker for this reader's event source.
     * @throws IllegalStateException if the reader source is not set.
     */
    fun waker(): Waker =
        requireNotNull(source?.waker()) { "reader source not set" }

    /**
     * Polls to check if there are any events matching the filter within the given timeout.
     *
     * This method first checks the internal event queue for any events that match
     * the filter. If no matching events are found, it reads from the event source
     * until either a matching event is found or the timeout elapses.
     *
     * Events that don't match the filter are buffered to prevent losing them,
     * and are added back to the queue when the poll operation completes.
     *
     * @param timeout The maximum duration to wait, or null to block indefinitely.
     * @param filter The filter to apply to events.
     * @return true if a matching event is available, false otherwise.
     * @throws IllegalStateException if the event source is not initialized.
     */
    fun poll(timeout: Duration?, filter: Filter): Boolean {
        // First check if there's already a matching event in the queue
        for (event in events) {
            if (filter.eval(event)) {
                return true
            }
        }

        val eventSource = source
            ?: throw IllegalStateException("Failed to initialize input reader")

        val pollTimeout = PollTimeout.new(timeout)

        while (true) {
            val maybeEvent: InternalEvent? = try {
                val event = eventSource.tryRead(pollTimeout.leftover())
                if (event != null) {
                    if (filter.eval(event)) {
                        event
                    } else {
                        skippedEvents.add(event)
                        null
                    }
                } else {
                    null
                }
            } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
                // Check for interrupted-like exceptions - return false instead of propagating
                // Note: In Kotlin common, we check for specific exception types that indicate interruption
                if (e.message?.contains("interrupted", ignoreCase = true) == true ||
                    e::class.simpleName == "InterruptedException"
                ) {
                    return false
                }
                throw e
            }

            if (pollTimeout.elapsed() || maybeEvent != null) {
                // Move skipped events back to the main queue
                events.addAll(skippedEvents)
                skippedEvents.clear()

                if (maybeEvent != null) {
                    events.addFirst(maybeEvent)
                    return true
                }

                return false
            }
        }
    }

    /**
     * Blocks the thread until a valid [InternalEvent] can be read.
     *
     * Internally, we use [tryRead], which buffers the events that do not fulfill the filter
     * conditions to prevent stalling the thread in an infinite loop.
     *
     * @param filter The filter to apply to events.
     * @return The first event that matches the filter.
     * @throws IllegalStateException if the event source is not initialized.
     */
    fun read(filter: Filter): InternalEvent {
        // blocks the thread until a valid event is found
        while (true) {
            tryRead(filter)?.let { return it }
            poll(null, filter)
        }
    }

    /**
     * Attempts to read the first valid [InternalEvent].
     *
     * This function checks all events in the queue, and stores events that do not match the
     * filter in a buffer to be added back to the queue after all items have been evaluated. We
     * must buffer non-fulfilling events because, if added directly back to the queue, they would
     * result in an infinite loop, rechecking events that have already been evaluated against the
     * filter.
     *
     * @param filter The filter to apply to events.
     * @return The first matching event, or null if no matching event is available.
     */
    fun tryRead(filter: Filter): InternalEvent? {
        // check all events, storing events that do not match the filter in the skipped buffer
        val skipped = mutableListOf<InternalEvent>()
        var result: InternalEvent? = null

        while (events.isNotEmpty()) {
            val event = events.removeFirst()
            if (filter.eval(event)) {
                result = event
                break
            }
            skipped.add(event)
        }

        // push all skipped events back to the event queue
        events.addAll(skipped)

        return result
    }

    /**
     * Sets the event source for this reader.
     *
     * @param eventSource The event source to use for reading events.
     */
    internal fun setSource(eventSource: EventSource) {
        source = eventSource
    }
}
