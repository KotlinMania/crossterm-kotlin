// port-lint: source event/sys/windows/parse.rs
package io.github.kotlinmania.crossterm.event.sys.windows

import io.github.kotlinmania.crossterm.event.*
import io.github.kotlinmania.crossterm.event.source.MouseButtonsPressed

/**
 * Windows virtual key codes.
 *
 * These constants match the Windows API VK_* values from winuser.h.
 */
internal object VirtualKey {
    const val VK_BACK = 0x08
    const val VK_TAB = 0x09
    const val VK_RETURN = 0x0D
    const val VK_SHIFT = 0x10
    const val VK_CONTROL = 0x11
    const val VK_MENU = 0x12  // Alt key
    const val VK_ESCAPE = 0x1B
    const val VK_PRIOR = 0x21  // Page Up
    const val VK_NEXT = 0x22   // Page Down
    const val VK_END = 0x23
    const val VK_HOME = 0x24
    const val VK_LEFT = 0x25
    const val VK_UP = 0x26
    const val VK_RIGHT = 0x27
    const val VK_DOWN = 0x28
    const val VK_INSERT = 0x2D
    const val VK_DELETE = 0x2E
    const val VK_NUMPAD0 = 0x60
    const val VK_NUMPAD9 = 0x69
    const val VK_F1 = 0x70
    const val VK_F24 = 0x87
}

/**
 * Windows control key state flags.
 *
 * These constants match the Windows API values from wincon.h.
 */
internal object ControlKeyFlags {
    const val SHIFT_PRESSED = 0x0010u
    const val LEFT_CTRL_PRESSED = 0x0008u
    const val RIGHT_CTRL_PRESSED = 0x0004u
    const val LEFT_ALT_PRESSED = 0x0002u
    const val RIGHT_ALT_PRESSED = 0x0001u
    const val CAPSLOCK_ON = 0x0080u
}

/**
 * Windows mouse event flags.
 */
enum class EventFlags {
    PressOrRelease,
    DoubleClick,
    MouseMoved,
    MouseWheeled,
    MouseHwheeled
}

/**
 * Character case for keyboard layout handling.
 */
private enum class CharCase {
    LowerCase,
    UpperCase
}

/**
 * Internal representation of a Windows key event during parsing.
 */
internal sealed class WindowsKeyEvent {
    data class KeyEventWrapper(val event: KeyEvent) : WindowsKeyEvent()
    data class Surrogate(val value: UShort) : WindowsKeyEvent()
}

/**
 * Key event record from Windows Console API.
 *
 * This is the Kotlin representation of the Windows KEY_EVENT_RECORD structure.
 */
data class KeyEventRecord(
    /** Whether this is a key down event (true) or key release (false). */
    val keyDown: Boolean,
    /** Virtual key code (VK_* constant). */
    val virtualKeyCode: UShort,
    /** Hardware scan code of the key. */
    val virtualScanCode: UShort,
    /** UTF-16 character value. */
    val uChar: UShort,
    /** Control key state flags. */
    val controlKeyState: ControlKeyState
)

/**
 * Control key state from Windows key event.
 */
data class ControlKeyState(
    val flags: UInt
) {
    /**
     * Checks if the given state flags are set.
     */
    fun hasState(state: UInt): Boolean = (flags and state) != 0u

    /**
     * Converts the control key state to [KeyModifiers].
     */
    fun toKeyModifiers(): KeyModifiers {
        val shift = hasState(ControlKeyFlags.SHIFT_PRESSED)
        val alt = hasState(ControlKeyFlags.LEFT_ALT_PRESSED or ControlKeyFlags.RIGHT_ALT_PRESSED)
        val control = hasState(ControlKeyFlags.LEFT_CTRL_PRESSED or ControlKeyFlags.RIGHT_CTRL_PRESSED)

        var modifier = KeyModifiers.empty()

        if (shift) {
            modifier = modifier + KeyModifiers.SHIFT
        }
        if (control) {
            modifier = modifier + KeyModifiers.CONTROL
        }
        if (alt) {
            modifier = modifier + KeyModifiers.ALT
        }

        return modifier
    }
}

/**
 * Mouse event record from Windows Console API.
 *
 * This is the Kotlin representation of the Windows MOUSE_EVENT_RECORD structure.
 */
data class MouseEventRecord(
    /** Mouse position coordinates. */
    val mousePosition: Coord,
    /** Button state flags. */
    val buttonState: ButtonState,
    /** Control key state flags. */
    val controlKeyState: ControlKeyState,
    /** Event flags indicating the type of mouse event. */
    val eventFlags: EventFlags
)

/**
 * Coordinate type for mouse position.
 */
data class Coord(
    val x: Short,
    val y: Short
)

/**
 * Button state from Windows mouse event.
 */
data class ButtonState(
    val flags: UInt
) {
    /** Whether the left mouse button is pressed. */
    fun leftButton(): Boolean = (flags and 0x0001u) != 0u

    /** Whether the right mouse button is pressed. */
    fun rightButton(): Boolean = (flags and 0x0002u) != 0u

    /** Whether the middle mouse button is pressed. */
    fun middleButton(): Boolean = (flags and 0x0004u) != 0u

    /** Whether no button is pressed (release state). */
    fun releaseButton(): Boolean = flags == 0u

    /** Whether scrolling down. */
    fun scrollDown(): Boolean = (flags.toInt() and 0xFF000000.toInt()) < 0

    /** Whether scrolling up. */
    fun scrollUp(): Boolean = (flags.toInt() and 0xFF000000.toInt()) > 0

    /** Whether scrolling left. */
    fun scrollLeft(): Boolean = (flags.toInt() and 0xFF000000.toInt()) < 0

    /** Whether scrolling right. */
    fun scrollRight(): Boolean = (flags.toInt() and 0xFF000000.toInt()) > 0
}

/**
 * Handles a Windows mouse event.
 *
 * @param mouseEvent The mouse event record from the Windows Console API.
 * @param buttonsPressed The previous mouse button state.
 * @return The parsed event, or null if the event should be ignored.
 */
fun handleMouseEvent(
    mouseEvent: MouseEventRecord,
    buttonsPressed: MouseButtonsPressed
): Event? {
    val result = parseMouseEventRecord(mouseEvent, buttonsPressed)
    return result?.let { Event.Mouse(it) }
}

/**
 * Handles a Windows key event.
 *
 * @param keyEvent The key event record from the Windows Console API.
 * @param surrogateBuffer The buffered high surrogate from a previous event, if any.
 * @return A pair of (event, new surrogate buffer), where event may be null if buffering a surrogate.
 */
fun handleKeyEvent(
    keyEvent: KeyEventRecord,
    surrogateBuffer: UShort?
): Pair<Event?, UShort?> {
    val windowsKeyEvent = parseKeyEventRecord(keyEvent)
        ?: return Pair(null, surrogateBuffer)

    return when (windowsKeyEvent) {
        is WindowsKeyEvent.KeyEventWrapper -> {
            // Discard any buffered surrogate value if another valid key event comes before the
            // next surrogate value.
            Pair(Event.Key(windowsKeyEvent.event), null)
        }
        is WindowsKeyEvent.Surrogate -> {
            val char = handleSurrogate(surrogateBuffer, windowsKeyEvent.value)
            if (char != null) {
                val modifiers = keyEvent.controlKeyState.toKeyModifiers()
                val event = KeyEvent.new(KeyCode.Char(char), modifiers)
                Pair(Event.Key(event), null)
            } else {
                Pair(null, windowsKeyEvent.value)
            }
        }
    }
}

/**
 * Handles UTF-16 surrogate pair combining.
 *
 * When the first surrogate is received, it's buffered. When the second
 * surrogate arrives, they are combined to form a complete Unicode character.
 *
 * @param surrogateBuffer The buffered high surrogate, if any.
 * @param newSurrogate The new surrogate value.
 * @return The complete character if both surrogates are now available, or null if buffering.
 */
internal fun handleSurrogate(surrogateBuffer: UShort?, newSurrogate: UShort): Char? {
    return if (surrogateBuffer != null) {
        // We have a buffered high surrogate and now received the low surrogate
        val highSurrogate = surrogateBuffer.toInt().toChar()
        val lowSurrogate = newSurrogate.toInt().toChar()

        // Decode the surrogate pair
        val chars = charArrayOf(highSurrogate, lowSurrogate)
        try {
            val str = chars.concatToString()
            str.firstOrNull()
        } catch (e: Exception) {
            null
        }
    } else {
        // Buffer this surrogate for the next event
        null
    }
}

/**
 * Attempts to ensure a character has the desired case.
 *
 * If the character can be converted to the desired case as a single character,
 * returns the converted character. Otherwise, returns the original character.
 *
 * @param ch The character to convert.
 * @param desiredCase The desired case.
 * @return The character in the desired case, or the original if conversion produces multiple chars.
 */
private fun tryEnsureCharCase(ch: Char, desiredCase: CharCase): Char {
    return when (desiredCase) {
        CharCase.LowerCase -> if (ch.isUpperCase()) {
            val lower = ch.lowercaseChar()
            // Check if lowercasing produces a single character
            if (ch.lowercase().length == 1) lower else ch
        } else {
            ch
        }
        CharCase.UpperCase -> if (ch.isLowerCase()) {
            val upper = ch.uppercaseChar()
            // Check if uppercasing produces a single character
            if (ch.uppercase().length == 1) upper else ch
        } else {
            ch
        }
    }
}

/**
 * Attempts to return the character for a key event accounting for the user's keyboard layout.
 *
 * The returned character (if any) is capitalized (if applicable) based on shift and capslock state.
 * Returns null if the key doesn't map to a character or if it is a dead key.
 *
 * We use the *currently* active keyboard layout (if it can be determined). This layout may not
 * correspond to the keyboard layout that was active when the user typed their input, since console
 * applications get their input asynchronously from the terminal. By the time a console application
 * can process a key input, the user may have changed the active layout. In this case, the character
 * returned might not correspond to what the user expects, but there is no way for a console
 * application to know what the keyboard layout actually was for a key event, so this is our best
 * effort. If a console application processes input in a timely fashion, then it is unlikely that a
 * user has time to change their keyboard layout before a key event is processed.
 *
 * @param keyEvent The key event record.
 * @return The character for the key, or null if not applicable.
 */
internal fun getCharForKey(keyEvent: KeyEventRecord): Char? {
    // This function uses Windows API ToUnicodeEx to convert the virtual key code
    // to a character based on the current keyboard layout.
    //
    // In the actual Windows implementation, this would call:
    //   - GetForegroundWindow() to get the foreground window handle
    //   - GetWindowThreadProcessId() to get the thread ID
    //   - GetKeyboardLayout() to get the keyboard layout for that thread
    //   - ToUnicodeEx() to convert the virtual key to a character
    //
    // For now, this is a simplified implementation that relies on uChar when available.
    // A full implementation would use native Windows API calls.

    val isShiftPressed = keyEvent.controlKeyState.hasState(ControlKeyFlags.SHIFT_PRESSED)
    val isCapsLockOn = keyEvent.controlKeyState.hasState(ControlKeyFlags.CAPSLOCK_ON)
    val desiredCase = if (isShiftPressed xor isCapsLockOn) {
        CharCase.UpperCase
    } else {
        CharCase.LowerCase
    }

    // If uChar is a printable ASCII character, use it
    val uChar = keyEvent.uChar.toInt()
    if (uChar in 0x20..0x7E) {
        val ch = uChar.toChar()
        return tryEnsureCharCase(ch, desiredCase)
    }

    return null
}

/**
 * Parses a Windows key event record into a crossterm key event.
 *
 * @param keyEvent The key event record from the Windows Console API.
 * @return The parsed key event, or null if the event should be ignored.
 */
internal fun parseKeyEventRecord(keyEvent: KeyEventRecord): WindowsKeyEvent? {
    val modifiers = keyEvent.controlKeyState.toKeyModifiers()
    val virtualKeyCode = keyEvent.virtualKeyCode.toInt()

    // We normally ignore all key release events, but we will make an exception for an Alt key
    // release if it carries a u_char value, as this indicates an Alt code.
    val isAltCode = virtualKeyCode == VirtualKey.VK_MENU && !keyEvent.keyDown && keyEvent.uChar != 0u.toUShort()
    if (isAltCode) {
        val utf16 = keyEvent.uChar
        val utf16Int = utf16.toInt()
        return when {
            utf16Int in 0xD800..0xDFFF -> {
                WindowsKeyEvent.Surrogate(utf16)
            }
            else -> {
                // Interpret as Unicode scalar value
                val ch = utf16Int.toChar()
                val keyCode = KeyCode.Char(ch)
                val kind = if (keyEvent.keyDown) {
                    KeyEventKind.Press
                } else {
                    KeyEventKind.Release
                }
                val event = KeyEvent(keyCode, modifiers, kind)
                WindowsKeyEvent.KeyEventWrapper(event)
            }
        }
    }

    // Don't generate events for numpad key presses when they're producing Alt codes.
    val isNumpadNumericKey = virtualKeyCode in VirtualKey.VK_NUMPAD0..VirtualKey.VK_NUMPAD9
    val isOnlyAltModifier = KeyModifiers.ALT in modifiers &&
            KeyModifiers.SHIFT !in modifiers &&
            KeyModifiers.CONTROL !in modifiers
    if (isOnlyAltModifier && isNumpadNumericKey) {
        return null
    }

    val parseResult: KeyCode? = when (virtualKeyCode) {
        VirtualKey.VK_SHIFT, VirtualKey.VK_CONTROL, VirtualKey.VK_MENU -> null
        VirtualKey.VK_BACK -> KeyCode.Backspace
        VirtualKey.VK_ESCAPE -> KeyCode.Esc
        VirtualKey.VK_RETURN -> KeyCode.Enter
        in VirtualKey.VK_F1..VirtualKey.VK_F24 -> {
            KeyCode.F((keyEvent.virtualKeyCode.toInt() - 111).toUByte())
        }
        VirtualKey.VK_LEFT -> KeyCode.Left
        VirtualKey.VK_UP -> KeyCode.Up
        VirtualKey.VK_RIGHT -> KeyCode.Right
        VirtualKey.VK_DOWN -> KeyCode.Down
        VirtualKey.VK_PRIOR -> KeyCode.PageUp
        VirtualKey.VK_NEXT -> KeyCode.PageDown
        VirtualKey.VK_HOME -> KeyCode.Home
        VirtualKey.VK_END -> KeyCode.End
        VirtualKey.VK_DELETE -> KeyCode.Delete
        VirtualKey.VK_INSERT -> KeyCode.Insert
        VirtualKey.VK_TAB -> {
            if (KeyModifiers.SHIFT in modifiers) {
                KeyCode.BackTab
            } else {
                KeyCode.Tab
            }
        }
        else -> {
            val utf16 = keyEvent.uChar
            val utf16Int = utf16.toInt()
            when {
                utf16Int in 0x00..0x1F -> {
                    // Some key combinations generate either no u_char value or generate control
                    // codes. To deliver back a KeyCode::Char(...) event we want to know which
                    // character the key normally maps to on the user's keyboard layout.
                    // The keys that intentionally generate control codes (ESC, ENTER, TAB, etc.)
                    // are handled by their virtual key codes above.
                    getCharForKey(keyEvent)?.let { KeyCode.Char(it) }
                }
                utf16Int in 0xD800..0xDFFF -> {
                    return WindowsKeyEvent.Surrogate(utf16)
                }
                else -> {
                    // Interpret as Unicode scalar value
                    val ch = utf16Int.toChar()
                    KeyCode.Char(ch)
                }
            }
        }
    }

    if (parseResult != null) {
        val kind = if (keyEvent.keyDown) {
            KeyEventKind.Press
        } else {
            KeyEventKind.Release
        }
        val event = KeyEvent(parseResult, modifiers, kind)
        return WindowsKeyEvent.KeyEventWrapper(event)
    }

    return null
}

/**
 * Parses the 'y' position of a mouse event relative to the terminal window.
 *
 * The 'y' position of a mouse event or resize event is not relative to the window but absolute
 * to screen buffer. This means that when the mouse cursor is at the top left it will be
 * x: 0, y: 2295 (e.g. y = number of cells counting from the absolute buffer height) instead
 * of relative x: 0, y: 0 to the window.
 *
 * @param y The absolute y position.
 * @param windowTop The top of the terminal window in the screen buffer.
 * @return The relative y position.
 */
fun parseRelativeY(y: Short, windowTop: Short): Short {
    return (y - windowTop).toShort()
}

/**
 * Parses a Windows mouse event record into a crossterm mouse event.
 *
 * @param event The mouse event record from the Windows Console API.
 * @param buttonsPressed The previous mouse button state.
 * @return The parsed mouse event, or null if the event should be ignored.
 */
internal fun parseMouseEventRecord(
    event: MouseEventRecord,
    buttonsPressed: MouseButtonsPressed
): MouseEvent? {
    val modifiers = event.controlKeyState.toKeyModifiers()

    val xPos = event.mousePosition.x.toUShort()
    // Note: In actual Windows implementation, we would call parseRelativeY with
    // the screen buffer info's terminal window top. For now, we use the position directly.
    val yPos = event.mousePosition.y.toUShort()

    val buttonState = event.buttonState

    val kind: MouseEventKind? = when (event.eventFlags) {
        EventFlags.PressOrRelease, EventFlags.DoubleClick -> {
            when {
                buttonState.leftButton() && !buttonsPressed.left ->
                    MouseEventKind.Down
                !buttonState.leftButton() && buttonsPressed.left ->
                    MouseEventKind.Up
                buttonState.rightButton() && !buttonsPressed.right ->
                    MouseEventKind.Down
                !buttonState.rightButton() && buttonsPressed.right ->
                    MouseEventKind.Up
                buttonState.middleButton() && !buttonsPressed.middle ->
                    MouseEventKind.Down
                !buttonState.middleButton() && buttonsPressed.middle ->
                    MouseEventKind.Up
                else -> null
            }
        }
        EventFlags.MouseMoved -> {
            if (buttonState.releaseButton()) {
                MouseEventKind.Moved
            } else {
                MouseEventKind.Drag
            }
        }
        EventFlags.MouseWheeled -> {
            // Vertical scroll
            // from https://docs.microsoft.com/en-us/windows/console/mouse-event-record-str
            // if `button_state` is negative then the wheel was rotated backward, toward the user.
            when {
                buttonState.scrollDown() -> MouseEventKind.ScrollDown()
                buttonState.scrollUp() -> MouseEventKind.ScrollUp()
                else -> null
            }
        }
        EventFlags.MouseHwheeled -> {
            when {
                buttonState.scrollLeft() -> MouseEventKind.ScrollLeft()
                buttonState.scrollRight() -> MouseEventKind.ScrollRight()
                else -> null
            }
        }
    }

    return kind?.let { MouseEvent(it, xPos, yPos, modifiers) }
}
