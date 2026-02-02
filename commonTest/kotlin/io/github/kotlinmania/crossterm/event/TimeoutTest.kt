// port-lint: tests event/timeout.rs
package io.github.kotlinmania.crossterm.event

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * Tests for [PollTimeout].
 *
 * These tests correspond to the tests in the Rust event/timeout.rs module.
 */
class TimeoutTest {

    @Test
    fun testTimeoutWithoutDurationDoesNotHaveLeftover() {
        val timeout = PollTimeout.new(null)
        assertNull(timeout.leftover())
    }

    @Test
    fun testTimeoutWithoutDurationNeverElapses() {
        val timeout = PollTimeout.new(null)
        assertFalse(timeout.elapsed())
    }

    @Test
    fun testTimeoutElapses() {
        val timeoutMillis = 100L

        // Create a timeout that has already elapsed (200ms elapsed, 100ms timeout)
        val timeout = PollTimeout.forTesting(
            timeout = timeoutMillis.milliseconds,
            alreadyElapsed = (2 * timeoutMillis).milliseconds
        )

        assertTrue(timeout.elapsed())
    }

    @Test
    fun testElapsedTimeoutHasZeroLeftover() {
        val timeoutMillis = 100L

        // Create a timeout that has already elapsed
        val timeout = PollTimeout.forTesting(
            timeout = timeoutMillis.milliseconds,
            alreadyElapsed = (2 * timeoutMillis).milliseconds
        )

        assertTrue(timeout.elapsed())
        val leftover = timeout.leftover()
        assertNotNull(leftover)
        assertEquals(0.milliseconds, leftover)
    }

    @Test
    fun testNotElapsedTimeoutHasPositiveLeftover() {
        val timeout = PollTimeout.new(60.seconds)

        assertFalse(timeout.elapsed())
        val leftover = timeout.leftover()
        assertNotNull(leftover)
        assertTrue(leftover > 0.seconds)
    }
}
