// port-lint: tests event/filter.rs
package io.github.kotlinmania.crossterm.event

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for event filters.
 *
 * These tests correspond to the tests in the Rust event/filter.rs module.
 * Note: The Rust tests are Unix-only (#[cfg(unix)]), but these Kotlin tests
 * run on all platforms since the filter logic is platform-independent.
 */
class FilterTest {

    /**
     * A filter that accepts all internal events.
     * Used for testing purposes.
     */
    private class InternalEventFilter : Filter {
        override fun eval(event: InternalEvent): Boolean = true
    }

    @Test
    fun testCursorPositionFilterFiltersCursorPosition() {
        assertFalse(CursorPositionFilter.eval(InternalEvent.Event(Event.Resize(10u, 10u))))
        assertTrue(CursorPositionFilter.eval(InternalEvent.CursorPosition(0u, 0u)))
    }

    @Test
    fun testKeyboardEnhancementStatusFilterFiltersKeyboardEnhancementStatus() {
        assertFalse(KeyboardEnhancementFlagsFilter.eval(InternalEvent.Event(Event.Resize(10u, 10u))))
        assertTrue(
            KeyboardEnhancementFlagsFilter.eval(
                InternalEvent.KeyboardEnhancementFlags(KeyboardEnhancementFlags.DISAMBIGUATE_ESCAPE_CODES)
            )
        )
        assertTrue(KeyboardEnhancementFlagsFilter.eval(InternalEvent.PrimaryDeviceAttributes))
    }

    @Test
    fun testPrimaryDeviceAttributesFilterFiltersPrimaryDeviceAttributes() {
        assertFalse(PrimaryDeviceAttributesFilter.eval(InternalEvent.Event(Event.Resize(10u, 10u))))
        assertTrue(PrimaryDeviceAttributesFilter.eval(InternalEvent.PrimaryDeviceAttributes))
    }

    @Test
    fun testEventFilterFiltersEvents() {
        assertTrue(EventFilter.eval(InternalEvent.Event(Event.Resize(10u, 10u))))
        assertFalse(EventFilter.eval(InternalEvent.CursorPosition(0u, 0u)))
    }

    @Test
    fun testEventFilterFiltersInternalEvents() {
        val filter = InternalEventFilter()
        assertTrue(filter.eval(InternalEvent.Event(Event.Resize(10u, 10u))))
        assertTrue(filter.eval(InternalEvent.CursorPosition(0u, 0u)))
    }
}
