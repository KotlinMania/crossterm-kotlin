// port-lint: source event.rs
/**
 * # Event
 *
 * The `event` module provides the functionality to read keyboard, mouse, and terminal resize
 * events.
 *
 * - [read] returns an [Event] immediately if one is available, or blocks until one is available.
 * - [poll] lets you check whether an [Event] is available within the given period of time.
 *   In other words, whether a subsequent call to [read] will block or not.
 *
 * It is not allowed to call these functions from different threads or combine them with
 * `EventStream`. You are allowed to either:
 *
 * - use [read] and [poll] on any, but the same, thread
 * - or use `EventStream`
 *
 * Make sure to enable raw mode in order for keyboard events to work properly.
 *
 * ## Mouse and Focus Events
 *
 * Mouse and focus events are not enabled by default. You have to enable them with
 * [EnableMouseCapture] and [EnableFocusChange].
 *
 * ## Examples
 *
 * Blocking read:
 * ```kotlin
 * while (true) {
 *     when (val event = read()) {
 *         Event.FocusGained -> println("FocusGained")
 *         Event.FocusLost -> println("FocusLost")
 *         is Event.Key -> println(event.keyEvent)
 *         is Event.Mouse -> println(event.mouseEvent)
 *         is Event.Paste -> println(event.content)
 *         is Event.Resize -> println("New size ${event.columns}x${event.rows}")
 *     }
 * }
 * ```
 *
 * Non-blocking read:
 * ```kotlin
 * while (true) {
 *     if (poll(kotlin.time.Duration.parse("100ms"))) {
 *         println(read())
 *     } else {
 *         // Timeout expired and no Event is available.
 *     }
 * }
 * ```
 */
package io.github.kotlinmania.crossterm.event

import io.github.kotlinmania.crossterm.Command
import io.github.kotlinmania.crossterm.csi
import kotlin.time.Duration

/**
 * Represents special flags that tell compatible terminals to add extra information to keyboard events.
 *
 * See <https://sw.kovidgoyal.net/kitty/keyboard-protocol/#progressive-enhancement> for more information.
 *
 * Alternate keys and Unicode codepoints are not yet supported by crossterm.
 */
data class KeyboardEnhancementFlags(val bits: UByte) {
    companion object {
        val NONE = KeyboardEnhancementFlags(0u)

        /**
         * Represent Escape and modified keys using CSI-u sequences, so they can be unambiguously read.
         */
        val DISAMBIGUATE_ESCAPE_CODES = KeyboardEnhancementFlags(0b0000_0001u)

        /**
         * Add extra events with [KeyEvent.kind] set to [KeyEventKind.Repeat] or [KeyEventKind.Release]
         * when keys are autorepeated or released.
         */
        val REPORT_EVENT_TYPES = KeyboardEnhancementFlags(0b0000_0010u)

        /**
         * Send alternate keycodes in addition to the base keycode.
         *
         * See <https://sw.kovidgoyal.net/kitty/keyboard-protocol/#key-codes>.
         */
        val REPORT_ALTERNATE_KEYS = KeyboardEnhancementFlags(0b0000_0100u)

        /**
         * Represent all keyboard events as CSI-u sequences. This is required to get repeat/release events
         * for plain-text keys.
         */
        val REPORT_ALL_KEYS_AS_ESCAPE_CODES = KeyboardEnhancementFlags(0b0000_1000u)

        fun empty(): KeyboardEnhancementFlags = NONE
    }

    fun bits(): UByte = bits

    operator fun plus(other: KeyboardEnhancementFlags): KeyboardEnhancementFlags =
        KeyboardEnhancementFlags((bits.toInt() or other.bits.toInt()).toUByte())

    operator fun contains(other: KeyboardEnhancementFlags): Boolean =
        (bits.toInt() and other.bits.toInt()) == other.bits.toInt()
}

/**
 * Checks if there is an [Event] available.
 *
 * Returns `true` if an [Event] is available, otherwise it returns `false`.
 *
 * `true` guarantees that subsequent call to [read] won't block.
 *
 * @param timeout Maximum waiting time for event availability.
 *
 * Example:
 * ```kotlin
 * val isEventAvailable = poll(kotlin.time.Duration.ZERO)
 * ```
 */
fun poll(timeout: Duration): Boolean =
    io.github.kotlinmania.crossterm.event.poll(timeout, EventFilter)

/**
 * Reads a single [Event].
 *
 * This function blocks until an [Event] is available. Combine it with [poll] to get non-blocking reads.
 *
 * Example:
 * ```kotlin
 * while (true) {
 *     println(read())
 * }
 * ```
 */
fun read(): Event {
    return when (val internalEvent = io.github.kotlinmania.crossterm.event.read(EventFilter)) {
        is InternalEvent.Event -> internalEvent.event
        else -> error("read(EventFilter) returned non-Event internal event: $internalEvent")
    }
}

/**
 * Attempts to read a single [Event] without blocking the thread.
 *
 * If no event is found, `null` is returned.
 *
 * Example:
 * ```kotlin
 * while (true) {
 *     if (poll(kotlin.time.Duration.parse("100ms"))) {
 *         while (true) {
 *             val event = tryRead() ?: break
 *             println(event)
 *         }
 *     }
 * }
 * ```
 */
fun tryRead(): Event? {
    return when (val internalEvent = io.github.kotlinmania.crossterm.event.tryRead(EventFilter)) {
        null -> null
        is InternalEvent.Event -> internalEvent.event
        else -> error("tryRead(EventFilter) returned non-Event internal event: $internalEvent")
    }
}

/**
 * A command that enables mouse event capturing.
 *
 * Mouse events can be captured with [read]/[poll].
 */
data object EnableMouseCapture : Command {
    override fun writeAnsi(writer: Appendable) {
        writer.append(
            csi("?1000h") +
                csi("?1002h") +
                csi("?1003h") +
                csi("?1015h") +
                csi("?1006h")
        )
    }
}

/**
 * A command that disables mouse event capturing.
 *
 * Mouse events can be captured with [read]/[poll].
 */
data object DisableMouseCapture : Command {
    override fun writeAnsi(writer: Appendable) {
        writer.append(
            csi("?1006l") +
                csi("?1015l") +
                csi("?1003l") +
                csi("?1002l") +
                csi("?1000l")
        )
    }
}

/**
 * A command that enables focus event emission.
 *
 * Focus events can be captured with [read]/[poll].
 */
data object EnableFocusChange : Command {
    override fun writeAnsi(writer: Appendable) {
        writer.append(csi("?1004h"))
    }
}

/**
 * A command that disables focus event emission.
 */
data object DisableFocusChange : Command {
    override fun writeAnsi(writer: Appendable) {
        writer.append(csi("?1004l"))
    }
}

/**
 * A command that enables bracketed paste mode.
 */
data object EnableBracketedPaste : Command {
    override fun writeAnsi(writer: Appendable) {
        writer.append(csi("?2004h"))
    }
}

/**
 * A command that disables bracketed paste mode.
 */
data object DisableBracketedPaste : Command {
    override fun writeAnsi(writer: Appendable) {
        writer.append(csi("?2004l"))
    }
}

/**
 * A command that enables extra kinds of keyboard events (kitty keyboard protocol).
 */
data class PushKeyboardEnhancementFlags(val flags: KeyboardEnhancementFlags) : Command {
    override fun writeAnsi(writer: Appendable) {
        writer.append(csi(">"))
        writer.append(flags.bits().toString())
        writer.append("u")
    }
}

/**
 * A command that disables extra kinds of keyboard events.
 */
data object PopKeyboardEnhancementFlags : Command {
    override fun writeAnsi(writer: Appendable) {
        writer.append(csi("<1u"))
    }
}

/**
 * Represents an event.
 *
 * Events can be read using the event reading functions or `EventStream`.
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

    /**
     * Returns `true` if the event is a key press event.
     *
     * This is useful for waiting for any key press event, regardless of the key that was pressed.
     *
     * Returns `false` for key release and repeat events, as well as for non-key events.
     */
    fun isKeyPress(): Boolean = this is Key && keyEvent.kind == KeyEventKind.Press

    /** Returns `true` if the event is a key release event. */
    fun isKeyRelease(): Boolean = this is Key && keyEvent.kind == KeyEventKind.Release

    /** Returns `true` if the event is a key repeat event. */
    fun isKeyRepeat(): Boolean = this is Key && keyEvent.kind == KeyEventKind.Repeat

    /**
     * Returns the key event if this is a key event, otherwise `null`.
     *
     * This is a convenience method that makes apps that only care about key events easier to
     * write.
     */
    fun asKeyEvent(): KeyEvent? = (this as? Key)?.keyEvent

    /**
     * Returns the key event if this is a key press event, otherwise `null`.
     *
     * This is a convenience method that makes apps that only care about key press events, and not
     * key release or repeat events, easier to write.
     */
    fun asKeyPressEvent(): KeyEvent? = when (this) {
        is Key -> if (isKeyPress()) keyEvent else null
        else -> null
    }

    /** Returns the key event if this is a key release event, otherwise `null`. */
    fun asKeyReleaseEvent(): KeyEvent? = when (this) {
        is Key -> if (isKeyRelease()) keyEvent else null
        else -> null
    }

    /** Returns the key event if this is a key repeat event, otherwise `null`. */
    fun asKeyRepeatEvent(): KeyEvent? = when (this) {
        is Key -> if (isKeyRepeat()) keyEvent else null
        else -> null
    }

    /**
     * Returns the mouse event if this is a mouse event, otherwise `null`.
     *
     * This is a convenience method that makes code which only cares about mouse events easier to
     * write.
     */
    fun asMouseEvent(): MouseEvent? = (this as? Mouse)?.mouseEvent

    /**
     * Returns the pasted string if this is a paste event, otherwise `null`.
     *
     * This is a convenience method that makes code which only cares about paste events easier to
     * write.
     */
    fun asPasteEvent(): String? = (this as? Paste)?.content

    /**
     * Returns the size as a pair if this is a resize event, otherwise `null`.
     *
     * This is a convenience method that makes code which only cares about resize events easier to
     * write.
     */
    fun asResizeEvent(): Pair<UShort, UShort>? = when (this) {
        is Resize -> Pair(columns, rows)
        else -> null
    }
}

// Platform-specific display strings in Rust are selected via `#[cfg(...)]` inside `impl Display`.
// Kotlin Multiplatform uses `expect`/`actual` as the closest analogue of Rust cfg blocks.
internal expect fun keyCodeBackspaceDisplayName(): String
internal expect fun keyCodeDeleteDisplayName(): String
internal expect fun keyCodeEnterDisplayName(): String

internal expect fun keyModifiersControlDisplayName(): String
internal expect fun keyModifiersAltDisplayName(): String
internal expect fun keyModifiersSuperDisplayName(): String

internal expect fun modifierKeyCodeLeftControlDisplayName(): String
internal expect fun modifierKeyCodeLeftAltDisplayName(): String
internal expect fun modifierKeyCodeLeftSuperDisplayName(): String
internal expect fun modifierKeyCodeRightControlDisplayName(): String
internal expect fun modifierKeyCodeRightAltDisplayName(): String
internal expect fun modifierKeyCodeRightSuperDisplayName(): String

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

        fun from(code: KeyCode): KeyEvent =
            KeyEvent(code, KeyModifiers.empty(), KeyEventKind.Press, KeyEventState.empty())

        fun newWithKind(
            code: KeyCode,
            modifiers: KeyModifiers = KeyModifiers.NONE,
            kind: KeyEventKind
        ): KeyEvent = KeyEvent(code, modifiers, kind, KeyEventState.NONE)

        fun newWithKindAndState(
            code: KeyCode,
            modifiers: KeyModifiers = KeyModifiers.NONE,
            kind: KeyEventKind,
            state: KeyEventState
        ): KeyEvent = KeyEvent(code, modifiers, kind, state)
    }

    /** Returns whether this is a press event */
    fun isPress(): Boolean = kind == KeyEventKind.Press

    /** Returns whether this is a release event */
    fun isRelease(): Boolean = kind == KeyEventKind.Release

    /** Returns whether this is a repeat event */
    fun isRepeat(): Boolean = kind == KeyEventKind.Repeat

    private fun normalizeCase(): KeyEvent {
        val keyCode = code
        if (keyCode !is KeyCode.Char) {
            return this
        }

        val char = keyCode.char
        return when {
            char.isAsciiUppercase() -> {
                if (modifiers.contains(KeyModifiers.SHIFT)) {
                    this
                } else {
                    copy(modifiers = modifiers + KeyModifiers.SHIFT)
                }
            }
            modifiers.contains(KeyModifiers.SHIFT) -> copy(code = KeyCode.Char(char.toAsciiUppercase()))
            else -> this
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is KeyEvent) {
            return false
        }

        val lhs = normalizeCase()
        val rhs = other.normalizeCase()
        return lhs.code == rhs.code &&
            lhs.modifiers == rhs.modifiers &&
            lhs.kind == rhs.kind &&
            lhs.state == rhs.state
    }

    override fun hashCode(): Int {
        val normalized = normalizeCase()
        var result = normalized.code.hashCode()
        result = 31 * result + normalized.modifiers.hashCode()
        result = 31 * result + normalized.kind.hashCode()
        result = 31 * result + normalized.state.hashCode()
        return result
    }
}

/**
 * Represents a key.
 */
sealed class KeyCode {
    /** Backspace key */
    object Backspace : KeyCode()
    /** Enter key */
    object Enter : KeyCode()
    /** Left arrow key */
    object Left : KeyCode()
    /** Right arrow key */
    object Right : KeyCode()
    /** Up arrow key */
    object Up : KeyCode()
    /** Down arrow key */
    object Down : KeyCode()
    /** Home key */
    object Home : KeyCode()
    /** End key */
    object End : KeyCode()
    /** Page up key */
    object PageUp : KeyCode()
    /** Page down key */
    object PageDown : KeyCode()
    /** Tab key */
    object Tab : KeyCode()
    /** Shift + Tab key */
    object BackTab : KeyCode()
    /** Delete key */
    object Delete : KeyCode()
    /** Insert key */
    object Insert : KeyCode()
    /** Escape key */
    object Esc : KeyCode()
    /** Caps Lock key */
    object CapsLock : KeyCode()
    /** Scroll Lock key */
    object ScrollLock : KeyCode()
    /** Num Lock key */
    object NumLock : KeyCode()
    /** Print Screen key */
    object PrintScreen : KeyCode()
    /** Pause key */
    object Pause : KeyCode()
    /** Menu key */
    object Menu : KeyCode()
    /** Keypad Begin key */
    object KeypadBegin : KeyCode()
    /** Null */
    object Null : KeyCode()

    /** Function key (F1-F12, etc.) */
    data class F(val number: UByte) : KeyCode() {
        override fun toString(): String = super.toString()
    }

    /** A character key */
    data class Char(val char: kotlin.Char) : KeyCode() {
        override fun toString(): String = super.toString()
    }

    /** A media key */
    data class Media(val mediaKeyCode: MediaKeyCode) : KeyCode() {
        override fun toString(): String = super.toString()
    }

    /** A modifier key */
    data class Modifier(val modifierKeyCode: ModifierKeyCode) : KeyCode() {
        override fun toString(): String = super.toString()
    }

    /** Returns true if this is the given function key */
    fun isFunctionKey(n: UByte): Boolean = this is F && number == n

    /** Returns true if this is the given character */
    fun isChar(c: kotlin.Char): Boolean = this is Char && char == c

    /** Returns the character if this is a Char, otherwise null */
    fun asChar(): kotlin.Char? = (this as? Char)?.char

    /** Returns true if this is the given media key. */
    fun isMediaKey(media: MediaKeyCode): Boolean = this is Media && mediaKeyCode == media

    /** Returns true if this is the given modifier key. */
    fun isModifier(modifier: ModifierKeyCode): Boolean = this is Modifier && modifierKeyCode == modifier

    override fun toString(): String =
        when (this) {
            Backspace -> keyCodeBackspaceDisplayName()
            Enter -> keyCodeEnterDisplayName()
            Left -> "Left"
            Right -> "Right"
            Up -> "Up"
            Down -> "Down"
            Home -> "Home"
            End -> "End"
            PageUp -> "Page Up"
            PageDown -> "Page Down"
            Tab -> "Tab"
            BackTab -> "Back Tab"
            Delete -> keyCodeDeleteDisplayName()
            Insert -> "Insert"
            Esc -> "Esc"
            CapsLock -> "Caps Lock"
            ScrollLock -> "Scroll Lock"
            NumLock -> "Num Lock"
            PrintScreen -> "Print Screen"
            Pause -> "Pause"
            Menu -> "Menu"
            KeypadBegin -> "Begin"
            Null -> "Null"
            is F -> "F$number"
            is Char -> if (char == ' ') "Space" else char.toString()
            is Media -> mediaKeyCode.toString()
            is Modifier -> modifierKeyCode.toString()
        }
}

/**
 * Key modifiers (Shift, Ctrl, Alt, etc.)
 */
data class KeyModifiers(val bits: UByte) {
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

    override fun toString(): String {
        val parts = mutableListOf<String>()
        if (contains(SHIFT)) {
            parts.add("Shift")
        }
        if (contains(CONTROL)) {
            parts.add(keyModifiersControlDisplayName())
        }
        if (contains(ALT)) {
            parts.add(keyModifiersAltDisplayName())
        }
        if (contains(SUPER)) {
            parts.add(keyModifiersSuperDisplayName())
        }
        if (contains(HYPER)) {
            parts.add("Hyper")
        }
        if (contains(META)) {
            parts.add("Meta")
        }
        return parts.joinToString("+")
    }
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
data class KeyEventState(val bits: UByte) {
    companion object {
        val NONE = KeyEventState(0u)
        val KEYPAD = KeyEventState(0b0000_0001u)
        val CAPS_LOCK = KeyEventState(0b0000_0010u)
        val NUM_LOCK = KeyEventState(0b0000_0100u)

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
    MuteVolume;

    override fun toString(): String =
        when (this) {
            Play -> "Play"
            Pause -> "Pause"
            PlayPause -> "Play/Pause"
            Reverse -> "Reverse"
            Stop -> "Stop"
            FastForward -> "Fast Forward"
            Rewind -> "Rewind"
            TrackNext -> "Next Track"
            TrackPrevious -> "Previous Track"
            Record -> "Record"
            LowerVolume -> "Lower Volume"
            RaiseVolume -> "Raise Volume"
            MuteVolume -> "Mute Volume"
        }
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
    IsoLevel5Shift;

    override fun toString(): String =
        when (this) {
            LeftShift -> "Left Shift"
            LeftHyper -> "Left Hyper"
            LeftMeta -> "Left Meta"
            RightShift -> "Right Shift"
            RightHyper -> "Right Hyper"
            RightMeta -> "Right Meta"
            IsoLevel3Shift -> "Iso Level 3 Shift"
            IsoLevel5Shift -> "Iso Level 5 Shift"
            LeftControl -> modifierKeyCodeLeftControlDisplayName()
            LeftAlt -> modifierKeyCodeLeftAltDisplayName()
            LeftSuper -> modifierKeyCodeLeftSuperDisplayName()
            RightControl -> modifierKeyCodeRightControlDisplayName()
            RightAlt -> modifierKeyCodeRightAltDisplayName()
            RightSuper -> modifierKeyCodeRightSuperDisplayName()
        }
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
    data class Down(val button: MouseButton) : MouseEventKind()
    data class Up(val button: MouseButton) : MouseEventKind()
    data class Drag(val button: MouseButton) : MouseEventKind()
    data object Moved : MouseEventKind()
    data object ScrollDown : MouseEventKind()
    data object ScrollUp : MouseEventKind()
    data object ScrollLeft : MouseEventKind()
    data object ScrollRight : MouseEventKind()
}

/**
 * Mouse button.
 */
enum class MouseButton {
    Left,
    Right,
    Middle
}

private fun Char.isAsciiUppercase(): Boolean = code in 'A'.code..'Z'.code

private fun Char.toAsciiUppercase(): Char =
    if (code in 'a'.code..'z'.code) {
        (code - ('a'.code - 'A'.code)).toChar()
    } else {
        this
    }
