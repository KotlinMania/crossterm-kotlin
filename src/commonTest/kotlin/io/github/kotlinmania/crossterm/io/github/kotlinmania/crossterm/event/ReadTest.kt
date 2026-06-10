// port-lint: tests event/read.rs
package io.github.kotlinmania.crossterm.event

import io.github.kotlinmania.crossterm.event.source.EventSource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Tests for [InternalEventReader].
 *
 * These tests correspond to the tests in the Rust event/read.rs module.
 * Several upstream tests are gated on `#[cfg(unix)]` because they rely on
 * `CursorPositionFilter`. In this Kotlin port the filter is platform-independent,
 * so the same tests run on every target.
 */
class ReadTest {
    /**
     * A filter that accepts all internal events.
     *
     * Mirrors the upstream `InternalEventFilter` test helper.
     */
    private object AcceptAllFilter : Filter {
        override fun eval(event: InternalEvent): Boolean = true
    }

    @Test
    fun testPollFailsWithoutEventSource() {
        val reader = InternalEventReader(source = null)

        assertFails { reader.poll(null, AcceptAllFilter) }
        assertFails { reader.poll(Duration.ZERO, AcceptAllFilter) }
        assertFails { reader.poll(10.seconds, AcceptAllFilter) }
    }

    @Test
    fun testPollReturnsTrueForMatchingEventInQueueAtFront() {
        val reader =
            InternalEventReader(
                source = null,
                initialEvents = listOf(InternalEvent.Event(Event.Resize(10u, 10u))),
            )

        assertTrue(reader.poll(null, AcceptAllFilter))
    }

    @Test
    fun testPollReturnsTrueForMatchingEventInQueueAtBack() {
        val reader =
            InternalEventReader(
                source = null,
                initialEvents =
                    listOf(
                        InternalEvent.Event(Event.Resize(10u, 10u)),
                        InternalEvent.CursorPosition(10u, 20u),
                    ),
            )

        assertTrue(reader.poll(null, CursorPositionFilter))
    }

    @Test
    fun testReadReturnsMatchingEventInQueueAtFront() {
        val event: InternalEvent = InternalEvent.Event(Event.Resize(10u, 10u))

        val reader =
            InternalEventReader(
                source = null,
                initialEvents = listOf(event),
            )

        assertEquals(event, reader.read(AcceptAllFilter))
    }

    @Test
    fun testReadReturnsMatchingEventInQueueAtBack() {
        val cursorEvent: InternalEvent = InternalEvent.CursorPosition(10u, 20u)

        val reader =
            InternalEventReader(
                source = null,
                initialEvents =
                    listOf(
                        InternalEvent.Event(Event.Resize(10u, 10u)),
                        cursorEvent,
                    ),
            )

        assertEquals(cursorEvent, reader.read(CursorPositionFilter))
    }

    @Test
    fun testReadDoesNotConsumeSkippedEvent() {
        val skippedEvent: InternalEvent = InternalEvent.Event(Event.Resize(10u, 10u))
        val cursorEvent: InternalEvent = InternalEvent.CursorPosition(10u, 20u)

        val reader =
            InternalEventReader(
                source = null,
                initialEvents = listOf(skippedEvent, cursorEvent),
            )

        assertEquals(cursorEvent, reader.read(CursorPositionFilter))
        assertEquals(skippedEvent, reader.read(AcceptAllFilter))
    }

    @Test
    fun testTryReadDoesNotConsumeSkippedEvent() {
        val skippedEvent: InternalEvent = InternalEvent.Event(Event.Resize(10u, 10u))
        val cursorEvent: InternalEvent = InternalEvent.CursorPosition(10u, 20u)

        val reader =
            InternalEventReader(
                source = null,
                initialEvents = listOf(skippedEvent, cursorEvent),
            )

        assertEquals(cursorEvent, assertNotNull(reader.tryRead(CursorPositionFilter)))
        assertEquals(skippedEvent, assertNotNull(reader.tryRead(AcceptAllFilter)))
    }

    @Test
    fun testPollTimeoutsIfSourceHasNoEvents() {
        val reader = InternalEventReader(source = FakeSource())

        assertFalse(reader.poll(Duration.ZERO, AcceptAllFilter))
    }

    @Test
    fun testPollReturnsTrueIfSourceHasAtLeastOneEvent() {
        val reader =
            InternalEventReader(
                source = FakeSource.withEvents(listOf(InternalEvent.Event(Event.Resize(10u, 10u)))),
            )

        assertTrue(reader.poll(null, AcceptAllFilter))
        assertTrue(reader.poll(Duration.ZERO, AcceptAllFilter))
    }

    @Test
    fun testReadsReturnsEventIfSourceHasAtLeastOneEvent() {
        val event: InternalEvent = InternalEvent.Event(Event.Resize(10u, 10u))

        val reader =
            InternalEventReader(
                source = FakeSource.withEvents(listOf(event)),
            )

        assertEquals(event, reader.read(AcceptAllFilter))
    }

    @Test
    fun testReadReturnsEventsIfSourceHasEvents() {
        val event: InternalEvent = InternalEvent.Event(Event.Resize(10u, 10u))

        val reader =
            InternalEventReader(
                source = FakeSource.withEvents(listOf(event, event, event)),
            )

        assertEquals(event, reader.read(AcceptAllFilter))
        assertEquals(event, reader.read(AcceptAllFilter))
        assertEquals(event, reader.read(AcceptAllFilter))
    }

    @Test
    fun testPollReturnsFalseAfterAllSourceEventsAreConsumed() {
        val event: InternalEvent = InternalEvent.Event(Event.Resize(10u, 10u))

        val reader =
            InternalEventReader(
                source = FakeSource.withEvents(listOf(event, event, event)),
            )

        assertEquals(event, reader.read(AcceptAllFilter))
        assertEquals(event, reader.read(AcceptAllFilter))
        assertEquals(event, reader.read(AcceptAllFilter))
        assertFalse(reader.poll(Duration.ZERO, AcceptAllFilter))
    }

    @Test
    fun testPollPropagatesError() {
        val reader = InternalEventReader(source = FakeSource.withError(emptyList()))

        assertFails { reader.poll(Duration.ZERO, AcceptAllFilter) }
    }

    @Test
    fun testReadPropagatesError() {
        val reader = InternalEventReader(source = FakeSource.withError(emptyList()))

        assertFails { reader.read(AcceptAllFilter) }
    }

    @Test
    fun testPollContinuesAfterError() {
        val event: InternalEvent = InternalEvent.Event(Event.Resize(10u, 10u))

        val reader =
            InternalEventReader(
                source = FakeSource.withError(listOf(event, event)),
            )

        assertEquals(event, reader.read(AcceptAllFilter))
        assertFails { reader.read(AcceptAllFilter) }
        assertTrue(reader.poll(Duration.ZERO, AcceptAllFilter))
    }

    @Test
    fun testReadContinuesAfterError() {
        val event: InternalEvent = InternalEvent.Event(Event.Resize(10u, 10u))

        val reader =
            InternalEventReader(
                source = FakeSource.withError(listOf(event, event)),
            )

        assertEquals(event, reader.read(AcceptAllFilter))
        assertFails { reader.read(AcceptAllFilter) }
        assertEquals(event, reader.read(AcceptAllFilter))
    }

    /**
     * A test [EventSource] that returns events from a queue and may emit a
     * single error before the final event is delivered.
     *
     * When constructed with [withError], the source carries an error that is
     * raised exactly once: when only one queued event remains, the next
     * [tryRead] throws instead of returning that event. After the error is
     * consumed, subsequent reads drain the remaining events normally and then
     * return `null` to signal a timeout.
     *
     * When constructed with [withEvents] or the default, no error is ever
     * raised; the source simply drains the queue and returns `null` thereafter.
     */
    private class FakeSource(
        private val events: ArrayDeque<InternalEvent> = ArrayDeque(),
        private var error: Throwable? = null,
    ) : EventSource {
        override fun tryRead(timeout: Duration?): InternalEvent? {
            // Return error if set in case there's just one remaining event
            if (events.size == 1) {
                error?.let {
                    error = null
                    throw it
                }
            }

            // Return all events from the queue
            if (events.isNotEmpty()) {
                return events.removeFirst()
            }

            // Return error if there're no more events
            error?.let {
                error = null
                throw it
            }

            // Timeout
            return null
        }

        companion object {
            fun withEvents(events: List<InternalEvent>): FakeSource = FakeSource(events = ArrayDeque(events), error = null)

            fun withError(events: List<InternalEvent>): FakeSource =
                FakeSource(
                    events = ArrayDeque(events),
                    error = RuntimeException("io error"),
                )
        }
    }
}
