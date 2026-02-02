// port-lint: source event/timeout.rs
package io.github.kotlinmania.crossterm.event

import kotlin.time.Duration
import kotlin.time.TimeSource

/**
 * Keeps track of the elapsed time since the moment the polling started.
 *
 * This class is used to manage timeout behavior when polling for events,
 * tracking how much time has passed and how much time remains before timeout.
 */
class PollTimeout private constructor(
    private val timeout: Duration?,
    private val start: TimeSource.Monotonic.ValueTimeMark
) {
    companion object {
        /**
         * Constructs a new [PollTimeout] with the given optional [Duration].
         *
         * @param timeout The maximum duration to wait, or null to wait indefinitely.
         * @return A new [PollTimeout] instance.
         */
        fun new(timeout: Duration?): PollTimeout =
            PollTimeout(timeout, TimeSource.Monotonic.markNow())

        /**
         * Creates a [PollTimeout] with a simulated elapsed time for testing purposes.
         *
         * @param timeout The timeout duration.
         * @param alreadyElapsed The duration that has "already elapsed" before the timeout starts.
         * @return A new [PollTimeout] instance that behaves as if time has already passed.
         */
        internal fun forTesting(timeout: Duration?, alreadyElapsed: Duration): PollTimeout {
            // Create a mark in the past by marking now and subtracting the elapsed time
            val pastMark = TimeSource.Monotonic.markNow() - alreadyElapsed
            return PollTimeout(timeout, pastMark)
        }
    }

    /**
     * Returns whether the timeout has elapsed.
     *
     * It always returns `false` if the initial timeout was set to `null`.
     *
     * @return true if the timeout has elapsed, false otherwise.
     */
    fun elapsed(): Boolean =
        timeout?.let { start.elapsedNow() >= it } ?: false

    /**
     * Returns the timeout leftover (initial timeout duration - elapsed duration).
     *
     * @return The remaining duration until the timeout expires, or null if no timeout was set.
     *         Returns [Duration.ZERO] if the timeout has already elapsed.
     */
    fun leftover(): Duration? =
        timeout?.let { t ->
            val elapsed = start.elapsedNow()
            if (elapsed >= t) {
                Duration.ZERO
            } else {
                t - elapsed
            }
        }
}
