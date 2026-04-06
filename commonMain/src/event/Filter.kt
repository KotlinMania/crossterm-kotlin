// port-lint: source event/filter.rs
package io.github.kotlinmania.crossterm.event

/**
 * Interface for filtering an [InternalEvent].
 */
interface Filter {
    /**
     * Returns whether the given event fulfills the filter.
     *
     * @param event The internal event to evaluate.
     * @return true if the event passes the filter, false otherwise.
     */
    fun eval(event: InternalEvent): Boolean
}

/**
 * A filter that matches cursor position events.
 */
data object CursorPositionFilter : Filter {
    override fun eval(event: InternalEvent): Boolean =
        event is InternalEvent.CursorPosition
}

/**
 * A filter that matches keyboard enhancement flags events.
 *
 * This filter checks for either a [InternalEvent.KeyboardEnhancementFlags] response or a
 * [InternalEvent.PrimaryDeviceAttributes] response. If we receive the
 * [InternalEvent.PrimaryDeviceAttributes] response but not [InternalEvent.KeyboardEnhancementFlags],
 * the terminal does not support
 * progressive keyboard enhancement.
 */
data object KeyboardEnhancementFlagsFilter : Filter {
    override fun eval(event: InternalEvent): Boolean =
        event is InternalEvent.KeyboardEnhancementFlags ||
            event is InternalEvent.PrimaryDeviceAttributes
}

/**
 * A filter that matches primary device attributes events.
 */
data object PrimaryDeviceAttributesFilter : Filter {
    override fun eval(event: InternalEvent): Boolean =
        event is InternalEvent.PrimaryDeviceAttributes
}

/**
 * A filter that matches public [Event]s.
 */
data object EventFilter : Filter {
    override fun eval(event: InternalEvent): Boolean =
        event is InternalEvent.Event
}

/**
 * A filter that matches all internal events.
 *
 * This is primarily useful for testing.
 */
internal data object InternalEventFilter : Filter {
    override fun eval(event: InternalEvent): Boolean = true
}
