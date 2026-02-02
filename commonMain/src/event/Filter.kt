// port-lint: source event/filter.rs
package io.github.kotlinmania.crossterm.event

/**
 * Keyboard enhancement flags for the Kitty keyboard protocol.
 *
 * See [Progressive Enhancement](https://sw.kovidgoyal.net/kitty/keyboard-protocol/#progressive-enhancement)
 * for more information.
 *
 * Alternate keys and Unicode codepoints are not yet supported by crossterm.
 */
@kotlin.jvm.JvmInline
value class KeyboardEnhancementFlags(val bits: UByte) {
    companion object {
        /** Empty flags. */
        val NONE = KeyboardEnhancementFlags(0u)

        /**
         * Represent Escape and modified keys using CSI-u sequences, so they can be unambiguously
         * read.
         */
        val DISAMBIGUATE_ESCAPE_CODES = KeyboardEnhancementFlags(0b0000_0001u)

        /**
         * Add extra events with [KeyEvent.kind] set to [KeyEventKind.Repeat] or
         * [KeyEventKind.Release] when keys are autorepeated or released.
         */
        val REPORT_EVENT_TYPES = KeyboardEnhancementFlags(0b0000_0010u)

        /**
         * Send [alternate keycodes](https://sw.kovidgoyal.net/kitty/keyboard-protocol/#key-codes)
         * in addition to the base keycode. The alternate keycode overrides the base keycode in
         * resulting [KeyEvent]s.
         */
        val REPORT_ALTERNATE_KEYS = KeyboardEnhancementFlags(0b0000_0100u)

        /**
         * Represent all keyboard events as CSI-u sequences. This is required to get repeat/release
         * events for plain-text keys.
         */
        val REPORT_ALL_KEYS_AS_ESCAPE_CODES = KeyboardEnhancementFlags(0b0000_1000u)

        // Note: REPORT_ASSOCIATED_TEXT (0b0001_0000) is not yet supported by crossterm.

        /** Returns empty flags. */
        fun empty(): KeyboardEnhancementFlags = NONE
    }

    /** Combines two flag sets using bitwise OR. */
    operator fun plus(other: KeyboardEnhancementFlags): KeyboardEnhancementFlags =
        KeyboardEnhancementFlags((bits.toInt() or other.bits.toInt()).toUByte())

    /** Checks if this flag set contains all flags from another set. */
    operator fun contains(other: KeyboardEnhancementFlags): Boolean =
        (bits.toInt() and other.bits.toInt()) == other.bits.toInt()

    /** Returns true if no flags are set. */
    fun isEmpty(): Boolean = bits == 0u.toUByte()
}

/**
 * An internal event.
 *
 * Encapsulates publicly available [Event] with additional internal
 * events that shouldn't be publicly available to the crate users.
 */
sealed class InternalEvent {
    /** A public event. */
    data class EventWrapper(val event: Event) : InternalEvent()

    /** A cursor position (column, row). */
    data class CursorPosition(val column: UShort, val row: UShort) : InternalEvent()

    /** The progressive keyboard enhancement flags enabled by the terminal. */
    data class KeyboardEnhancementFlagsEvent(val flags: KeyboardEnhancementFlags) : InternalEvent()

    /** Attributes and architectural class of the terminal. */
    data object PrimaryDeviceAttributes : InternalEvent()
}

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
 * This filter checks for either a [InternalEvent.KeyboardEnhancementFlagsEvent] response or
 * a [InternalEvent.PrimaryDeviceAttributes] response. If we receive the PrimaryDeviceAttributes
 * response but not KeyboardEnhancementFlags, the terminal does not support
 * progressive keyboard enhancement.
 */
data object KeyboardEnhancementFlagsFilter : Filter {
    override fun eval(event: InternalEvent): Boolean =
        event is InternalEvent.KeyboardEnhancementFlagsEvent ||
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
        event is InternalEvent.EventWrapper
}

/**
 * A filter that matches all internal events.
 *
 * This is primarily useful for testing.
 */
internal data object InternalEventFilter : Filter {
    override fun eval(event: InternalEvent): Boolean = true
}
