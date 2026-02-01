// port-lint: source event.rs
package io.github.kotlinmania.crossterm.event

/**
 * Represents a terminal event.
 *
 * Events can be read using the event reading functions or the EventStream.
 */
sealed class Event {
    /** The terminal gained focus */
    data object FocusGained : Event()

    /** The terminal lost focus */
    data object FocusLost : Event()

    /** A single key event with additional pressed modifiers */
    data class Key(val keyEvent: KeyEvent) : Event()

    /** A single mouse event with additional pressed modifiers */
    data class Mouse(val mouseEvent: MouseEvent) : Event()

    /** A string that was pasted into the terminal (requires bracketed paste) */
    data class Paste(val content: String) : Event()

    /** A resize event with new dimensions (columns, rows) */
    data class Resize(val columns: UShort, val rows: UShort) : Event()

    /** Returns true if the event is a key press event */
    fun isKeyPress(): Boolean = this is Key && keyEvent.kind == KeyEventKind.Press

    /** Returns true if the event is a key release event */
    fun isKeyRelease(): Boolean = this is Key && keyEvent.kind == KeyEventKind.Release

    /** Returns true if the event is a key repeat event */
    fun isKeyRepeat(): Boolean = this is Key && keyEvent.kind == KeyEventKind.Repeat

    /** Returns the key event if this is a Key event, otherwise null */
    fun asKeyEvent(): KeyEvent? = (this as? Key)?.keyEvent
}

/**
 * Represents a key event.
 */
data class KeyEvent(
    /** The key itself */
    val code: KeyCode,
    /** Additional key modifiers */
    val modifiers: KeyModifiers = KeyModifiers.NONE,
    /** Kind of event (Press, Release, Repeat) */
    val kind: KeyEventKind = KeyEventKind.Press,
    /** Keyboard state */
    val state: KeyEventState = KeyEventState.NONE
) {
    companion object {
        fun new(code: KeyCode, modifiers: KeyModifiers = KeyModifiers.NONE): KeyEvent =
            KeyEvent(code, modifiers, KeyEventKind.Press, KeyEventState.NONE)
    }

    /** Returns whether this is a press event */
    fun isPress(): Boolean = kind == KeyEventKind.Press

    /** Returns whether this is a release event */
    fun isRelease(): Boolean = kind == KeyEventKind.Release

    /** Returns whether this is a repeat event */
    fun isRepeat(): Boolean = kind == KeyEventKind.Repeat
}

/**
 * Represents a key.
 */
sealed class KeyCode {
    /** Backspace key */
    data object Backspace : KeyCode()
    /** Enter key */
    data object Enter : KeyCode()
    /** Left arrow key */
    data object Left : KeyCode()
    /** Right arrow key */
    data object Right : KeyCode()
    /** Up arrow key */
    data object Up : KeyCode()
    /** Down arrow key */
    data object Down : KeyCode()
    /** Home key */
    data object Home : KeyCode()
    /** End key */
    data object End : KeyCode()
    /** Page up key */
    data object PageUp : KeyCode()
    /** Page down key */
    data object PageDown : KeyCode()
    /** Tab key */
    data object Tab : KeyCode()
    /** Shift + Tab key */
    data object BackTab : KeyCode()
    /** Delete key */
    data object Delete : KeyCode()
    /** Insert key */
    data object Insert : KeyCode()
    /** Escape key */
    data object Esc : KeyCode()
    /** Caps Lock key */
    data object CapsLock : KeyCode()
    /** Scroll Lock key */
    data object ScrollLock : KeyCode()
    /** Num Lock key */
    data object NumLock : KeyCode()
    /** Print Screen key */
    data object PrintScreen : KeyCode()
    /** Pause key */
    data object Pause : KeyCode()
    /** Menu key */
    data object Menu : KeyCode()
    /** Keypad Begin key */
    data object KeypadBegin : KeyCode()
    /** Null */
    data object Null : KeyCode()

    /** Function key (F1-F12, etc.) */
    data class F(val number: UByte) : KeyCode()

    /** A character key */
    data class Char(val char: kotlin.Char) : KeyCode()

    /** A media key */
    data class Media(val mediaKeyCode: MediaKeyCode) : KeyCode()

    /** A modifier key */
    data class Modifier(val modifierKeyCode: ModifierKeyCode) : KeyCode()

    /** Returns true if this is the given function key */
    fun isFunctionKey(n: UByte): Boolean = this is F && number == n

    /** Returns true if this is the given character */
    fun isChar(c: kotlin.Char): Boolean = this is Char && char == c

    /** Returns the character if this is a Char, otherwise null */
    fun asChar(): kotlin.Char? = (this as? Char)?.char
}

/**
 * Key modifiers (Shift, Ctrl, Alt, etc.)
 */
value class KeyModifiers(val bits: UByte) {
    companion object {
        val NONE = KeyModifiers(0u)
        val SHIFT = KeyModifiers(0b0000_0001u)
        val CONTROL = KeyModifiers(0b0000_0010u)
        val ALT = KeyModifiers(0b0000_0100u)
        val SUPER = KeyModifiers(0b0000_1000u)
        val HYPER = KeyModifiers(0b0001_0000u)
        val META = KeyModifiers(0b0010_0000u)

        fun empty(): KeyModifiers = NONE
    }

    operator fun plus(other: KeyModifiers): KeyModifiers =
        KeyModifiers((bits.toInt() or other.bits.toInt()).toUByte())

    operator fun contains(other: KeyModifiers): Boolean =
        (bits.toInt() and other.bits.toInt()) == other.bits.toInt()

    fun isEmpty(): Boolean = bits == 0u.toUByte()
}

/**
 * The kind of key event.
 */
enum class KeyEventKind {
    Press,
    Repeat,
    Release
}

/**
 * Keyboard state.
 */
value class KeyEventState(val bits: UByte) {
    companion object {
        val NONE = KeyEventState(0u)
        val KEYPAD = KeyEventState(0b0000_0001u)
        val CAPS_LOCK = KeyEventState(0b0000_1000u)
        val NUM_LOCK = KeyEventState(0b0000_1000u)

        fun empty(): KeyEventState = NONE
    }

    fun isEmpty(): Boolean = bits == 0u.toUByte()
}

/**
 * Media key codes.
 */
enum class MediaKeyCode {
    Play,
    Pause,
    PlayPause,
    Reverse,
    Stop,
    FastForward,
    Rewind,
    TrackNext,
    TrackPrevious,
    Record,
    LowerVolume,
    RaiseVolume,
    MuteVolume
}

/**
 * Modifier key codes.
 */
enum class ModifierKeyCode {
    LeftShift,
    LeftControl,
    LeftAlt,
    LeftSuper,
    LeftHyper,
    LeftMeta,
    RightShift,
    RightControl,
    RightAlt,
    RightSuper,
    RightHyper,
    RightMeta,
    IsoLevel3Shift,
    IsoLevel5Shift
}

/**
 * Represents a mouse event.
 */
data class MouseEvent(
    /** The kind of mouse event */
    val kind: MouseEventKind,
    /** The column where the event occurred */
    val column: UShort,
    /** The row where the event occurred */
    val row: UShort,
    /** The key modifiers active during the event */
    val modifiers: KeyModifiers
)

/**
 * The kind of mouse event.
 */
sealed class MouseEventKind {
    data object Down : MouseEventKind()
    data object Up : MouseEventKind()
    data object Drag : MouseEventKind()
    data object Moved : MouseEventKind()
    data class ScrollDown(val amount: UShort = 1u) : MouseEventKind()
    data class ScrollUp(val amount: UShort = 1u) : MouseEventKind()
    data class ScrollLeft(val amount: UShort = 1u) : MouseEventKind()
    data class ScrollRight(val amount: UShort = 1u) : MouseEventKind()
}

/**
 * Mouse button.
 */
enum class MouseButton {
    Left,
    Right,
    Middle
}
