// port-lint: source event/stream.rs

package io.github.kotlinmania.crossterm.event

/**
 * The result of reading a terminal event.
 *
 * Replaces `Result<Event>` in the public API so that the error type is not
 * a `Throwable` subclass — Swift Export cannot bridge
 * `kotlin.Result<Event>` across the ObjC boundary (gap #3).
 */
sealed class EventResult {
    /**
     * A successfully read event.
     */
    data class Ok(
        val event: Event,
    ) : EventResult()

    /**
     * An error that occurred while reading events.
     *
     * [EventError] is a plain data class, not a [Throwable] subclass,
     * so it crosses the Swift Export bridge without dragging in the
     * `Throwable.getStackTrace()` → `Array` hazard.
     */
    data class Err(
        val error: EventError,
    ) : EventResult()
}

/**
 * A non-throwable error from event reading.
 *
 * Carries the same information as an [Exception] (message, optional cause
 * description) but does not extend [Throwable], making it safe for the
 * Swift Export bridge.
 */
data class EventError(
    val message: String,
    val cause: String? = null,
)
