// port-lint: source event/sys/unix/parse.rs
package io.github.kotlinmania.crossterm.event.sys.unix

import io.github.kotlinmania.crossterm.event.*
import io.github.kotlinmania.crossterm.terminal.sys.isRawModeEnabled

/**
 * Event parsing for Unix terminal input.
 *
 * This code parses ANSI escape sequences and other terminal input into events.
 *
 * Every function returns a [ParseResult]:
 * - [ParseResult.Success] -> we have an event, clear the buffer
 * - [ParseResult.Incomplete] -> wait for more bytes
 * - [ParseResult.Error] -> failed to parse event, clear the buffer
 */

/**
 * Result of attempting to parse an event from a byte buffer.
 */
sealed class ParseResult {
    /** Successfully parsed an event. */
    data class Success(val event: InternalEvent) : ParseResult()
    /** Need more bytes to complete parsing. */
    data object Incomplete : ParseResult()
    /** Failed to parse; the buffer should be cleared. */
    data object Error : ParseResult()
}

/**
 * Exception thrown when event parsing fails.
 */
class ParseEventException(message: String = "Could not parse an event.") : Exception(message)

/**
 * Creates a standard "could not parse event" error.
 */
private fun couldNotParseEventError(): ParseEventException = ParseEventException()

/**
 * Parses an event from the given buffer.
 *
 * @param buffer The input buffer containing terminal bytes.
 * @param inputAvailable Whether more input is available (affects escape key handling).
 * @return The parse result.
 */
fun parseEvent(buffer: ByteArray, inputAvailable: Boolean): ParseResult {
    if (buffer.isEmpty()) {
        return ParseResult.Incomplete
    }

    return when (buffer[0]) {
        0x1B.toByte() -> { // ESC
            if (buffer.size == 1) {
                if (inputAvailable) {
                    // Possible Esc sequence
                    ParseResult.Incomplete
                } else {
                    ParseResult.Success(InternalEvent.Event(Event.Key(KeyCode.Esc.toKeyEvent())))
                }
            } else {
                when (buffer[1]) {
                    'O'.code.toByte() -> {
                        if (buffer.size == 2) {
                            ParseResult.Incomplete
                        } else {
                            when (buffer[2]) {
                                'D'.code.toByte() -> ParseResult.Success(InternalEvent.Event(Event.Key(KeyCode.Left.toKeyEvent())))
                                'C'.code.toByte() -> ParseResult.Success(InternalEvent.Event(Event.Key(KeyCode.Right.toKeyEvent())))
                                'A'.code.toByte() -> ParseResult.Success(InternalEvent.Event(Event.Key(KeyCode.Up.toKeyEvent())))
                                'B'.code.toByte() -> ParseResult.Success(InternalEvent.Event(Event.Key(KeyCode.Down.toKeyEvent())))
                                'H'.code.toByte() -> ParseResult.Success(InternalEvent.Event(Event.Key(KeyCode.Home.toKeyEvent())))
                                'F'.code.toByte() -> ParseResult.Success(InternalEvent.Event(Event.Key(KeyCode.End.toKeyEvent())))
                                // F1-F4
                                in 'P'.code.toByte()..'S'.code.toByte() -> {
                                    val fNum = (1 + buffer[2] - 'P'.code.toByte()).toUByte()
                                    ParseResult.Success(InternalEvent.Event(Event.Key(KeyCode.F(fNum).toKeyEvent())))
                                }
                                else -> ParseResult.Error
                            }
                        }
                    }
                    '['.code.toByte() -> parseCsi(buffer)
                    0x1B.toByte() -> ParseResult.Success(InternalEvent.Event(Event.Key(KeyCode.Esc.toKeyEvent())))
                    else -> {
                        // Alt + key combination
                        val innerResult = parseEvent(buffer.sliceArray(1 until buffer.size), inputAvailable)
                        if (innerResult is ParseResult.Success) {
                            val event = innerResult.event
                            if (event is InternalEvent.Event && event.event is Event.Key) {
                                val keyEvent = event.event.keyEvent
                                val altKeyEvent = keyEvent.copy(
                                    modifiers = keyEvent.modifiers + KeyModifiers.ALT
                                )
                                ParseResult.Success(InternalEvent.Event(Event.Key(altKeyEvent)))
                            } else {
                                innerResult
                            }
                        } else {
                            innerResult
                        }
                    }
                }
            }
        }
        '\r'.code.toByte() -> ParseResult.Success(InternalEvent.Event(Event.Key(KeyCode.Enter.toKeyEvent())))
        // Issue #371: \n = 0xA, which is also the keycode for Ctrl+J. The only reason we get
        // newlines as input is because the terminal converts \r into \n for us. When we
        // enter raw mode, we disable that, so \n no longer has any meaning - it's better to
        // use Ctrl+J. Waiting to handle it here means it gets picked up later
        '\n'.code.toByte() -> {
            if (!isRawModeEnabled()) {
                ParseResult.Success(InternalEvent.Event(Event.Key(KeyCode.Enter.toKeyEvent())))
            } else {
                // In raw mode, \n is Ctrl+J
                ParseResult.Success(
                    InternalEvent.Event(
                        Event.Key(KeyEvent.new(KeyCode.Char('j'), KeyModifiers.CONTROL))
                    )
                )
            }
        }
        '\t'.code.toByte() -> ParseResult.Success(InternalEvent.Event(Event.Key(KeyCode.Tab.toKeyEvent())))
        0x7F.toByte() -> ParseResult.Success(InternalEvent.Event(Event.Key(KeyCode.Backspace.toKeyEvent())))
        in 0x01..0x1A -> {
            // Ctrl+A through Ctrl+Z
            val c = ('a'.code + (buffer[0] - 0x01)).toChar()
            ParseResult.Success(
                InternalEvent.Event(
                    Event.Key(KeyEvent.new(KeyCode.Char(c), KeyModifiers.CONTROL))
                )
            )
        }
        in 0x1C..0x1F -> {
            // Ctrl+4 through Ctrl+7
            val c = ('4'.code + (buffer[0] - 0x1C)).toChar()
            ParseResult.Success(
                InternalEvent.Event(
                    Event.Key(KeyEvent.new(KeyCode.Char(c), KeyModifiers.CONTROL))
                )
            )
        }
        0x00.toByte() -> {
            // Ctrl+Space (NUL)
            ParseResult.Success(
                InternalEvent.Event(
                    Event.Key(KeyEvent.new(KeyCode.Char(' '), KeyModifiers.CONTROL))
                )
            )
        }
        else -> {
            // Try to parse as UTF-8 character
            parseUtf8Char(buffer).fold(
                onSuccess = { maybeChar ->
                    if (maybeChar != null) {
                        val keyEvent = charCodeToEvent(KeyCode.Char(maybeChar))
                        ParseResult.Success(InternalEvent.Event(Event.Key(keyEvent)))
                    } else {
                        ParseResult.Incomplete
                    }
                },
                onFailure = { ParseResult.Error }
            )
        }
    }
}

/**
 * Converts a [KeyCode] to [KeyEvent], adding shift modifier for uppercase characters.
 */
private fun charCodeToEvent(code: KeyCode): KeyEvent {
    val modifiers = when (code) {
        is KeyCode.Char -> if (code.char.isUpperCase()) KeyModifiers.SHIFT else KeyModifiers.NONE
        else -> KeyModifiers.NONE
    }
    return KeyEvent.new(code, modifiers)
}

/**
 * Extension function to convert [KeyCode] to a simple [KeyEvent].
 */
private fun KeyCode.toKeyEvent(): KeyEvent = KeyEvent.new(this)

/**
 * Parses CSI (Control Sequence Introducer) sequences.
 *
 * @param buffer The input buffer starting with ESC [.
 * @return The parse result.
 */
fun parseCsi(buffer: ByteArray): ParseResult {
    require(buffer.size >= 2 && buffer[0] == 0x1B.toByte() && buffer[1] == '['.code.toByte()) {
        "Buffer must start with ESC ["
    }

    if (buffer.size == 2) {
        return ParseResult.Incomplete
    }

    return when (buffer[2]) {
        '['.code.toByte() -> {
            if (buffer.size == 3) {
                ParseResult.Incomplete
            } else {
                when (buffer[3]) {
                    // NOTE (@imdaveho): cannot find when this occurs;
                    // having another '[' after ESC[ not a likely scenario
                    in 'A'.code.toByte()..'E'.code.toByte() -> {
                        val fNum = (1 + buffer[3] - 'A'.code.toByte()).toUByte()
                        ParseResult.Success(InternalEvent.Event(Event.Key(KeyCode.F(fNum).toKeyEvent())))
                    }
                    else -> ParseResult.Error
                }
            }
        }
        'D'.code.toByte() -> ParseResult.Success(InternalEvent.Event(Event.Key(KeyCode.Left.toKeyEvent())))
        'C'.code.toByte() -> ParseResult.Success(InternalEvent.Event(Event.Key(KeyCode.Right.toKeyEvent())))
        'A'.code.toByte() -> ParseResult.Success(InternalEvent.Event(Event.Key(KeyCode.Up.toKeyEvent())))
        'B'.code.toByte() -> ParseResult.Success(InternalEvent.Event(Event.Key(KeyCode.Down.toKeyEvent())))
        'H'.code.toByte() -> ParseResult.Success(InternalEvent.Event(Event.Key(KeyCode.Home.toKeyEvent())))
        'F'.code.toByte() -> ParseResult.Success(InternalEvent.Event(Event.Key(KeyCode.End.toKeyEvent())))
        'Z'.code.toByte() -> {
            // Shift+Tab (BackTab)
            ParseResult.Success(
                InternalEvent.Event(
                    Event.Key(KeyEvent(KeyCode.BackTab, KeyModifiers.SHIFT, KeyEventKind.Press))
                )
            )
        }
        'M'.code.toByte() -> parseCsiNormalMouse(buffer)
        '<'.code.toByte() -> parseCsiSgrMouse(buffer)
        'I'.code.toByte() -> ParseResult.Success(InternalEvent.Event(Event.FocusGained))
        'O'.code.toByte() -> ParseResult.Success(InternalEvent.Event(Event.FocusLost))
        ';'.code.toByte() -> parseCsiModifierKeyCode(buffer)
        // P, Q, and S for compatibility with Kitty keyboard protocol,
        // as the 1 in 'CSI 1 P' etc. must be omitted if there are no
        // modifiers pressed:
        // https://sw.kovidgoyal.net/kitty/keyboard-protocol/#legacy-functional-keys
        'P'.code.toByte() -> ParseResult.Success(InternalEvent.Event(Event.Key(KeyCode.F(1u).toKeyEvent())))
        'Q'.code.toByte() -> ParseResult.Success(InternalEvent.Event(Event.Key(KeyCode.F(2u).toKeyEvent())))
        'S'.code.toByte() -> ParseResult.Success(InternalEvent.Event(Event.Key(KeyCode.F(4u).toKeyEvent())))
        '?'.code.toByte() -> {
            when (buffer.last()) {
                'u'.code.toByte() -> parseCsiKeyboardEnhancementFlags(buffer)
                'c'.code.toByte() -> parseCsiPrimaryDeviceAttributes(buffer)
                else -> ParseResult.Incomplete
            }
        }
        in '0'.code.toByte()..'9'.code.toByte() -> {
            // Numbered escape code
            if (buffer.size == 3) {
                ParseResult.Incomplete
            } else {
                // The final byte of a CSI sequence can be in the range 64-126, so
                // let's keep reading anything else.
                val lastByte = buffer.last()
                if (lastByte !in 64..126) {
                    ParseResult.Incomplete
                } else {
                    // Check for bracketed paste
                    if (buffer.size >= 6 && buffer.startsWith(byteArrayOf(0x1B, '['.code.toByte(), '2'.code.toByte(), '0'.code.toByte(), '0'.code.toByte(), '~'.code.toByte()))) {
                        parseCsiBracketedPaste(buffer)
                    } else {
                        when (lastByte.toInt().toChar()) {
                            'M' -> parseCsiRxvtMouse(buffer)
                            '~' -> parseCsiSpecialKeyCode(buffer)
                            'u' -> parseCsiUEncodedKeyCode(buffer)
                            'R' -> parseCsiCursorPosition(buffer)
                            else -> parseCsiModifierKeyCode(buffer)
                        }
                    }
                }
            }
        }
        else -> ParseResult.Error
    }
}

/**
 * Parses the next value from an iterator of strings.
 */
internal fun <T> nextParsed(iter: Iterator<String>, parser: (String) -> T?): Result<T> {
    return if (iter.hasNext()) {
        val value = parser(iter.next())
        if (value != null) {
            Result.success(value)
        } else {
            Result.failure(couldNotParseEventError())
        }
    } else {
        Result.failure(couldNotParseEventError())
    }
}

/**
 * Parses modifier and kind from an iterator.
 *
 * @return A pair of (modifier_mask, kind_code), or null if parsing fails.
 */
private fun modifierAndKindParsed(iter: Iterator<String>): Pair<UByte, UByte>? {
    if (!iter.hasNext()) return null

    val next = iter.next()
    val subParts = next.split(':')

    val modifierMask = subParts[0].toUByteOrNull() ?: return null
    val kindCode = if (subParts.size > 1) {
        subParts[1].toUByteOrNull() ?: 1u
    } else {
        1u
    }

    return Pair(modifierMask, kindCode)
}

/**
 * Parses CSI cursor position response.
 * ESC [ Cy ; Cx R
 *   Cy - cursor row number (starting from 1)
 *   Cx - cursor column number (starting from 1)
 */
fun parseCsiCursorPosition(buffer: ByteArray): ParseResult {
    require(buffer.startsWith(byteArrayOf(0x1B, '['.code.toByte()))) { "Buffer must start with ESC [" }
    require(buffer.last() == 'R'.code.toByte()) { "Buffer must end with R" }

    val s = try {
        buffer.decodeToString(2, buffer.size - 1)
    } catch (e: Exception) {
        return ParseResult.Error
    }

    val parts = s.split(';')
    if (parts.size != 2) return ParseResult.Error

    val y = ((parts[0].toUShortOrNull() ?: return ParseResult.Error) - 1u).toUShort()
    val x = ((parts[1].toUShortOrNull() ?: return ParseResult.Error) - 1u).toUShort()

    return ParseResult.Success(InternalEvent.CursorPosition(x, y))
}

/**
 * Parses CSI keyboard enhancement flags.
 * ESC [ ? flags u
 */
private fun parseCsiKeyboardEnhancementFlags(buffer: ByteArray): ParseResult {
    require(buffer.startsWith(byteArrayOf(0x1B, '['.code.toByte(), '?'.code.toByte()))) {
        "Buffer must start with ESC [ ?"
    }
    require(buffer.last() == 'u'.code.toByte()) { "Buffer must end with u" }

    if (buffer.size < 5) {
        return ParseResult.Incomplete
    }

    val bits = buffer[3] - '0'.code.toByte()
    var flags = KeyboardEnhancementFlags.NONE

    if (bits and 1 != 0) {
        flags = flags + KeyboardEnhancementFlags.DISAMBIGUATE_ESCAPE_CODES
    }
    if (bits and 2 != 0) {
        flags = flags + KeyboardEnhancementFlags.REPORT_EVENT_TYPES
    }
    if (bits and 4 != 0) {
        flags = flags + KeyboardEnhancementFlags.REPORT_ALTERNATE_KEYS
    }
    if (bits and 8 != 0) {
        flags = flags + KeyboardEnhancementFlags.REPORT_ALL_KEYS_AS_ESCAPE_CODES
    }
    // Note: REPORT_ASSOCIATED_TEXT is not yet supported by crossterm.
    // if (bits and 16 != 0) {
    //     flags = flags + KeyboardEnhancementFlags.REPORT_ASSOCIATED_TEXT
    // }

    return ParseResult.Success(InternalEvent.KeyboardEnhancementFlags(flags))
}

/**
 * Parses CSI primary device attributes.
 * ESC [ 64 ; attr1 ; attr2 ; ... ; attrn ; c
 *
 * This is a stub for parsing the primary device attributes. This response is not
 * exposed in the crossterm API so we don't need to parse the individual attributes yet.
 * See https://vt100.net/docs/vt510-rm/DA1.html
 */
@Suppress("UNUSED_PARAMETER")
private fun parseCsiPrimaryDeviceAttributes(buffer: ByteArray): ParseResult {
    return ParseResult.Success(InternalEvent.PrimaryDeviceAttributes)
}

/**
 * Parses modifier mask to [KeyModifiers].
 */
fun parseModifiers(mask: UByte): KeyModifiers {
    val modifierMask = if (mask > 0u) (mask - 1u).toInt() else 0
    var modifiers = KeyModifiers.NONE
    if (modifierMask and 1 != 0) modifiers = modifiers + KeyModifiers.SHIFT
    if (modifierMask and 2 != 0) modifiers = modifiers + KeyModifiers.ALT
    if (modifierMask and 4 != 0) modifiers = modifiers + KeyModifiers.CONTROL
    if (modifierMask and 8 != 0) modifiers = modifiers + KeyModifiers.SUPER
    if (modifierMask and 16 != 0) modifiers = modifiers + KeyModifiers.HYPER
    if (modifierMask and 32 != 0) modifiers = modifiers + KeyModifiers.META
    return modifiers
}

/**
 * Parses modifier mask to [KeyEventState].
 */
fun parseModifiersToState(mask: UByte): KeyEventState {
    val modifierMask = if (mask > 0u) (mask - 1u).toInt() else 0
    var state = KeyEventState.NONE
    if (modifierMask and 64 != 0) state = KeyEventState(state.bits or KeyEventState.CAPS_LOCK.bits)
    if (modifierMask and 128 != 0) state = KeyEventState(state.bits or KeyEventState.NUM_LOCK.bits)
    return state
}

/**
 * Parses key event kind from code.
 */
fun parseKeyEventKind(kind: UByte): KeyEventKind = when (kind.toInt()) {
    1 -> KeyEventKind.Press
    2 -> KeyEventKind.Repeat
    3 -> KeyEventKind.Release
    else -> KeyEventKind.Press
}

/**
 * Parses CSI modifier key code sequences.
 */
fun parseCsiModifierKeyCode(buffer: ByteArray): ParseResult {
    require(buffer.startsWith(byteArrayOf(0x1B, '['.code.toByte()))) { "Buffer must start with ESC [" }

    val s = try {
        buffer.decodeToString(2, buffer.size - 1)
    } catch (e: Exception) {
        return ParseResult.Error
    }

    val parts = s.split(';').toMutableList()

    // Skip the first part (usually "1" for arrow keys with modifiers)
    if (parts.isNotEmpty()) parts.removeAt(0)

    val (modifiers, kind) = if (parts.isNotEmpty()) {
        val modParts = parts[0].split(':')
        val modMask = modParts[0].toUByteOrNull() ?: 0u
        val kindCode = if (modParts.size > 1) modParts[1].toUByteOrNull() ?: 1u else 1u
        Pair(parseModifiers(modMask), parseKeyEventKind(kindCode))
    } else if (buffer.size > 3) {
        val modChar = buffer[buffer.size - 2].toInt().toChar()
        val modMask = modChar.digitToIntOrNull()?.toUByte() ?: 0u
        Pair(parseModifiers(modMask), KeyEventKind.Press)
    } else {
        Pair(KeyModifiers.NONE, KeyEventKind.Press)
    }

    val key = buffer.last()
    val keycode = when (key.toInt().toChar()) {
        'A' -> KeyCode.Up
        'B' -> KeyCode.Down
        'C' -> KeyCode.Right
        'D' -> KeyCode.Left
        'F' -> KeyCode.End
        'H' -> KeyCode.Home
        'P' -> KeyCode.F(1u)
        'Q' -> KeyCode.F(2u)
        'R' -> KeyCode.F(3u)
        'S' -> KeyCode.F(4u)
        else -> return ParseResult.Error
    }

    val inputEvent = Event.Key(KeyEvent(keycode, modifiers, kind))
    return ParseResult.Success(InternalEvent.Event(inputEvent))
}

/**
 * Translates a functional key codepoint to [KeyCode] and [KeyEventState].
 */
private fun translateFunctionalKeyCode(codepoint: UInt): Pair<KeyCode, KeyEventState>? {
    // Keypad keys
    val keypadCode = when (codepoint.toInt()) {
        57399 -> KeyCode.Char('0')
        57400 -> KeyCode.Char('1')
        57401 -> KeyCode.Char('2')
        57402 -> KeyCode.Char('3')
        57403 -> KeyCode.Char('4')
        57404 -> KeyCode.Char('5')
        57405 -> KeyCode.Char('6')
        57406 -> KeyCode.Char('7')
        57407 -> KeyCode.Char('8')
        57408 -> KeyCode.Char('9')
        57409 -> KeyCode.Char('.')
        57410 -> KeyCode.Char('/')
        57411 -> KeyCode.Char('*')
        57412 -> KeyCode.Char('-')
        57413 -> KeyCode.Char('+')
        57414 -> KeyCode.Enter
        57415 -> KeyCode.Char('=')
        57416 -> KeyCode.Char(',')
        57417 -> KeyCode.Left
        57418 -> KeyCode.Right
        57419 -> KeyCode.Up
        57420 -> KeyCode.Down
        57421 -> KeyCode.PageUp
        57422 -> KeyCode.PageDown
        57423 -> KeyCode.Home
        57424 -> KeyCode.End
        57425 -> KeyCode.Insert
        57426 -> KeyCode.Delete
        57427 -> KeyCode.KeypadBegin
        else -> null
    }

    if (keypadCode != null) {
        return Pair(keypadCode, KeyEventState.KEYPAD)
    }

    // Other special keys
    val specialCode = when (codepoint.toInt()) {
        57358 -> KeyCode.CapsLock
        57359 -> KeyCode.ScrollLock
        57360 -> KeyCode.NumLock
        57361 -> KeyCode.PrintScreen
        57362 -> KeyCode.Pause
        57363 -> KeyCode.Menu
        57376 -> KeyCode.F(13u)
        57377 -> KeyCode.F(14u)
        57378 -> KeyCode.F(15u)
        57379 -> KeyCode.F(16u)
        57380 -> KeyCode.F(17u)
        57381 -> KeyCode.F(18u)
        57382 -> KeyCode.F(19u)
        57383 -> KeyCode.F(20u)
        57384 -> KeyCode.F(21u)
        57385 -> KeyCode.F(22u)
        57386 -> KeyCode.F(23u)
        57387 -> KeyCode.F(24u)
        57388 -> KeyCode.F(25u)
        57389 -> KeyCode.F(26u)
        57390 -> KeyCode.F(27u)
        57391 -> KeyCode.F(28u)
        57392 -> KeyCode.F(29u)
        57393 -> KeyCode.F(30u)
        57394 -> KeyCode.F(31u)
        57395 -> KeyCode.F(32u)
        57396 -> KeyCode.F(33u)
        57397 -> KeyCode.F(34u)
        57398 -> KeyCode.F(35u)
        57428 -> KeyCode.Media(MediaKeyCode.Play)
        57429 -> KeyCode.Media(MediaKeyCode.Pause)
        57430 -> KeyCode.Media(MediaKeyCode.PlayPause)
        57431 -> KeyCode.Media(MediaKeyCode.Reverse)
        57432 -> KeyCode.Media(MediaKeyCode.Stop)
        57433 -> KeyCode.Media(MediaKeyCode.FastForward)
        57434 -> KeyCode.Media(MediaKeyCode.Rewind)
        57435 -> KeyCode.Media(MediaKeyCode.TrackNext)
        57436 -> KeyCode.Media(MediaKeyCode.TrackPrevious)
        57437 -> KeyCode.Media(MediaKeyCode.Record)
        57438 -> KeyCode.Media(MediaKeyCode.LowerVolume)
        57439 -> KeyCode.Media(MediaKeyCode.RaiseVolume)
        57440 -> KeyCode.Media(MediaKeyCode.MuteVolume)
        57441 -> KeyCode.Modifier(ModifierKeyCode.LeftShift)
        57442 -> KeyCode.Modifier(ModifierKeyCode.LeftControl)
        57443 -> KeyCode.Modifier(ModifierKeyCode.LeftAlt)
        57444 -> KeyCode.Modifier(ModifierKeyCode.LeftSuper)
        57445 -> KeyCode.Modifier(ModifierKeyCode.LeftHyper)
        57446 -> KeyCode.Modifier(ModifierKeyCode.LeftMeta)
        57447 -> KeyCode.Modifier(ModifierKeyCode.RightShift)
        57448 -> KeyCode.Modifier(ModifierKeyCode.RightControl)
        57449 -> KeyCode.Modifier(ModifierKeyCode.RightAlt)
        57450 -> KeyCode.Modifier(ModifierKeyCode.RightSuper)
        57451 -> KeyCode.Modifier(ModifierKeyCode.RightHyper)
        57452 -> KeyCode.Modifier(ModifierKeyCode.RightMeta)
        57453 -> KeyCode.Modifier(ModifierKeyCode.IsoLevel3Shift)
        57454 -> KeyCode.Modifier(ModifierKeyCode.IsoLevel5Shift)
        else -> null
    }

    if (specialCode != null) {
        return Pair(specialCode, KeyEventState.NONE)
    }

    return null
}

/**
 * Parses CSI u-encoded key codes (Kitty keyboard protocol / CSI u / fixterms).
 *
 * This function parses `CSI ... u` sequences. These are sequences defined in either
 * the `CSI u` (a.k.a. "Fix Keyboard Input on Terminals - Please",
 * https://www.leonerd.org.uk/hacks/fixterms/)
 * or Kitty Keyboard Protocol (https://sw.kovidgoyal.net/kitty/keyboard-protocol/)
 * specifications.
 */
fun parseCsiUEncodedKeyCode(buffer: ByteArray): ParseResult {
    require(buffer.startsWith(byteArrayOf(0x1B, '['.code.toByte()))) { "Buffer must start with ESC [" }
    require(buffer.last() == 'u'.code.toByte()) { "Buffer must end with u" }

    // This CSI sequence is a tuple of semicolon-separated numbers.
    val s = try {
        buffer.decodeToString(2, buffer.size - 1)
    } catch (e: Exception) {
        return ParseResult.Error
    }

    val parts = s.split(';').toMutableList()

    // In `CSI u`, this is parsed as:
    //     CSI codepoint ; modifiers u
    //     codepoint: ASCII Dec value
    //
    // The Kitty Keyboard Protocol extends this with optional components that can be
    // enabled progressively. The full sequence is parsed as:
    //     CSI unicode-key-code:alternate-key-codes ; modifiers:event-type ; text-as-codepoints u
    val codepointParts = (parts.removeFirstOrNull() ?: return ParseResult.Error).split(':')
    val codepoint = codepointParts[0].toUIntOrNull() ?: return ParseResult.Error

    var modifiers = KeyModifiers.NONE
    var kind = KeyEventKind.Press
    var stateFromModifiers = KeyEventState.NONE

    if (parts.isNotEmpty()) {
        val modParts = parts[0].split(':')
        val modMask = modParts[0].toUByteOrNull() ?: 0u
        modifiers = parseModifiers(modMask)
        kind = if (modParts.size > 1) parseKeyEventKind(modParts[1].toUByteOrNull() ?: 1u) else KeyEventKind.Press
        stateFromModifiers = parseModifiersToState(modMask)
    }

    val (keycode, stateFromKeycode) = run {
        val functional = translateFunctionalKeyCode(codepoint)
        if (functional != null) {
            functional
        } else {
            val c = Char(codepoint.toInt())
            val code = when (c) {
                '\u001B' -> KeyCode.Esc
                '\r' -> KeyCode.Enter
                '\n' -> if (!isRawModeEnabled()) KeyCode.Enter else KeyCode.Char('j') // Ctrl+J handled elsewhere
                '\t' -> if (KeyModifiers.SHIFT in modifiers) KeyCode.BackTab else KeyCode.Tab
                '\u007F' -> KeyCode.Backspace
                else -> KeyCode.Char(c)
            }
            Pair(code, KeyEventState.NONE)
        }
    }

    var finalKeycode = keycode
    var finalModifiers = modifiers

    // Add modifier flags for modifier keys
    if (keycode is KeyCode.Modifier) {
        when (keycode.modifierKeyCode) {
            ModifierKeyCode.LeftAlt, ModifierKeyCode.RightAlt ->
                finalModifiers = finalModifiers + KeyModifiers.ALT
            ModifierKeyCode.LeftControl, ModifierKeyCode.RightControl ->
                finalModifiers = finalModifiers + KeyModifiers.CONTROL
            ModifierKeyCode.LeftShift, ModifierKeyCode.RightShift ->
                finalModifiers = finalModifiers + KeyModifiers.SHIFT
            ModifierKeyCode.LeftSuper, ModifierKeyCode.RightSuper ->
                finalModifiers = finalModifiers + KeyModifiers.SUPER
            ModifierKeyCode.LeftHyper, ModifierKeyCode.RightHyper ->
                finalModifiers = finalModifiers + KeyModifiers.HYPER
            ModifierKeyCode.LeftMeta, ModifierKeyCode.RightMeta ->
                finalModifiers = finalModifiers + KeyModifiers.META
            else -> { /* No additional modifier */ }
        }
    }

    // When the "report alternate keys" flag is enabled in the Kitty Keyboard Protocol
    // and the terminal sends a keyboard event containing shift, the sequence will
    // contain an additional codepoint separated by a ':' character which contains
    // the shifted character according to the keyboard layout.
    if (KeyModifiers.SHIFT in modifiers && codepointParts.size > 1) {
        val shiftedCodepoint = codepointParts[1].toUIntOrNull()
        if (shiftedCodepoint != null) {
            val shiftedChar = Char(shiftedCodepoint.toInt())
            finalKeycode = KeyCode.Char(shiftedChar)
            // Remove SHIFT modifier since we've applied the shifted character
            finalModifiers = KeyModifiers((finalModifiers.bits.toInt() and KeyModifiers.SHIFT.bits.toInt().inv()).toUByte())
        }
    }

    val state = KeyEventState((stateFromKeycode.bits.toInt() or stateFromModifiers.bits.toInt()).toUByte())
    val inputEvent = Event.Key(KeyEvent(finalKeycode, finalModifiers, kind, state))
    return ParseResult.Success(InternalEvent.Event(inputEvent))
}

/**
 * Parses CSI special key codes (tilde sequences).
 * ESC [ number ~
 */
fun parseCsiSpecialKeyCode(buffer: ByteArray): ParseResult {
    require(buffer.startsWith(byteArrayOf(0x1B, '['.code.toByte()))) { "Buffer must start with ESC [" }
    require(buffer.last() == '~'.code.toByte()) { "Buffer must end with ~" }

    val s = try {
        buffer.decodeToString(2, buffer.size - 1)
    } catch (e: Exception) {
        return ParseResult.Error
    }

    val parts = s.split(';').toMutableList()

    // This CSI sequence can be a list of semicolon-separated numbers.
    val first = parts.removeFirstOrNull()?.toUByteOrNull() ?: return ParseResult.Error

    var modifiers = KeyModifiers.NONE
    var kind = KeyEventKind.Press
    var state = KeyEventState.NONE

    if (parts.isNotEmpty()) {
        val modParts = parts[0].split(':')
        val modMask = modParts[0].toUByteOrNull() ?: 0u
        modifiers = parseModifiers(modMask)
        kind = if (modParts.size > 1) parseKeyEventKind(modParts[1].toUByteOrNull() ?: 1u) else KeyEventKind.Press
        state = parseModifiersToState(modMask)
    }

    val keycode = when (first.toInt()) {
        1, 7 -> KeyCode.Home
        2 -> KeyCode.Insert
        3 -> KeyCode.Delete
        4, 8 -> KeyCode.End
        5 -> KeyCode.PageUp
        6 -> KeyCode.PageDown
        in 11..15 -> KeyCode.F((first.toInt() - 10).toUByte())
        in 17..21 -> KeyCode.F((first.toInt() - 11).toUByte())
        in 23..26 -> KeyCode.F((first.toInt() - 12).toUByte())
        in 28..29 -> KeyCode.F((first.toInt() - 15).toUByte())
        in 31..34 -> KeyCode.F((first.toInt() - 17).toUByte())
        else -> return ParseResult.Error
    }

    val inputEvent = Event.Key(KeyEvent(keycode, modifiers, kind, state))
    return ParseResult.Success(InternalEvent.Event(inputEvent))
}

/**
 * Parses CSI rxvt mouse encoding.
 * ESC [ Cb ; Cx ; Cy ; M
 */
fun parseCsiRxvtMouse(buffer: ByteArray): ParseResult {
    require(buffer.startsWith(byteArrayOf(0x1B, '['.code.toByte()))) { "Buffer must start with ESC [" }
    require(buffer.last() == 'M'.code.toByte()) { "Buffer must end with M" }

    val s = try {
        buffer.decodeToString(2, buffer.size - 1)
    } catch (e: Exception) {
        return ParseResult.Error
    }

    val parts = s.split(';')
    if (parts.size != 3) return ParseResult.Error

    val cbRaw = parts[0].toIntOrNull() ?: return ParseResult.Error
    val cb = cbRaw - 32
    if (cb < 0) return ParseResult.Error

    val (kind, modifiers) = parseCb(cb.toUByte())

    val cx = ((parts[1].toUShortOrNull() ?: return ParseResult.Error) - 1u).toUShort()
    val cy = ((parts[2].toUShortOrNull() ?: return ParseResult.Error) - 1u).toUShort()

    return ParseResult.Success(
        InternalEvent.Event(
            Event.Mouse(MouseEvent(kind, cx, cy, modifiers))
        )
    )
}

/**
 * Parses CSI normal mouse encoding (X10 compatibility mode).
 * ESC [ M CB Cx Cy (6 characters only).
 */
fun parseCsiNormalMouse(buffer: ByteArray): ParseResult {
    require(buffer.startsWith(byteArrayOf(0x1B, '['.code.toByte(), 'M'.code.toByte()))) {
        "Buffer must start with ESC [ M"
    }

    if (buffer.size < 6) {
        return ParseResult.Incomplete
    }

    val cbRaw = buffer[3].toInt() - 32
    if (cbRaw < 0) return ParseResult.Error

    val (kind, modifiers) = parseCb(cbRaw.toUByte())

    // See http://www.xfree86.org/current/ctlseqs.html#Mouse%20Tracking
    // The upper left character position on the terminal is denoted as 1,1.
    // Subtract 1 to keep it synced with cursor
    val cx = (buffer[4].toInt() and 0xFF).coerceAtLeast(32) - 32 - 1
    val cy = (buffer[5].toInt() and 0xFF).coerceAtLeast(32) - 32 - 1

    return ParseResult.Success(
        InternalEvent.Event(
            Event.Mouse(MouseEvent(kind, cx.toUShort(), cy.toUShort(), modifiers))
        )
    )
}

/**
 * Parses CSI SGR mouse encoding (extended mode).
 * ESC [ < Cb ; Cx ; Cy (;) (M or m)
 */
fun parseCsiSgrMouse(buffer: ByteArray): ParseResult {
    require(buffer.startsWith(byteArrayOf(0x1B, '['.code.toByte(), '<'.code.toByte()))) {
        "Buffer must start with ESC [ <"
    }

    val lastByte = buffer.last()
    if (lastByte != 'm'.code.toByte() && lastByte != 'M'.code.toByte()) {
        return ParseResult.Incomplete
    }

    val s = try {
        buffer.decodeToString(3, buffer.size - 1)
    } catch (e: Exception) {
        return ParseResult.Error
    }

    val parts = s.split(';')
    if (parts.size != 3) return ParseResult.Error

    val cb = parts[0].toUByteOrNull() ?: return ParseResult.Error
    val (kind, modifiers) = parseCb(cb)

    // See http://www.xfree86.org/current/ctlseqs.html#Mouse%20Tracking
    // The upper left character position on the terminal is denoted as 1,1.
    // Subtract 1 to keep it synced with cursor
    val cx = ((parts[1].toUShortOrNull() ?: return ParseResult.Error) - 1u).toUShort()
    val cy = ((parts[2].toUShortOrNull() ?: return ParseResult.Error) - 1u).toUShort()

    // When button 3 in Cb is used to represent mouse release, you can't tell which button was
    // released. SGR mode solves this by having the sequence end with a lowercase m if it's a
    // button release and an uppercase M if it's a button press.
    val finalKind = if (lastByte == 'm'.code.toByte()) {
        when (kind) {
            MouseEventKind.Down -> MouseEventKind.Up
            else -> kind
        }
    } else {
        kind
    }

    return ParseResult.Success(
        InternalEvent.Event(
            Event.Mouse(MouseEvent(finalKind, cx, cy, modifiers))
        )
    )
}

/**
 * Parses the mouse button/event code byte (Cb).
 *
 * Cb is the byte of a mouse input that contains the button being used, the key modifiers being
 * held and whether the mouse is dragging or not.
 *
 * Bit layout of cb, from low to high:
 * - button number (bits 0-1)
 * - shift (bit 2)
 * - meta/alt (bit 3)
 * - control (bit 4)
 * - mouse is dragging (bit 5)
 * - button number (bits 6-7)
 */
private fun parseCb(cb: UByte): Pair<MouseEventKind, KeyModifiers> {
    val cbInt = cb.toInt()
    val buttonNumber = (cbInt and 0b0000_0011) or ((cbInt and 0b1100_0000) shr 4)
    val dragging = (cbInt and 0b0010_0000) == 0b0010_0000

    val kind = when (buttonNumber to dragging) {
        0 to false -> MouseEventKind.Down
        1 to false -> MouseEventKind.Down
        2 to false -> MouseEventKind.Down
        0 to true -> MouseEventKind.Drag
        1 to true -> MouseEventKind.Drag
        2 to true -> MouseEventKind.Drag
        3 to false -> MouseEventKind.Up
        3 to true, 4 to true, 5 to true -> MouseEventKind.Moved
        4 to false -> MouseEventKind.ScrollUp()
        5 to false -> MouseEventKind.ScrollDown()
        6 to false -> MouseEventKind.ScrollLeft()
        7 to false -> MouseEventKind.ScrollRight()
        else -> return Pair(MouseEventKind.Down, KeyModifiers.NONE) // Unsupported button
    }

    var modifiers = KeyModifiers.NONE
    if (cbInt and 0b0000_0100 != 0) modifiers = modifiers + KeyModifiers.SHIFT
    if (cbInt and 0b0000_1000 != 0) modifiers = modifiers + KeyModifiers.ALT
    if (cbInt and 0b0001_0000 != 0) modifiers = modifiers + KeyModifiers.CONTROL

    return Pair(kind, modifiers)
}

/**
 * Parses CSI bracketed paste.
 * ESC [ 2 0 0 ~ pasted text ESC [ 2 0 1 ~
 */
fun parseCsiBracketedPaste(buffer: ByteArray): ParseResult {
    require(buffer.startsWith(byteArrayOf(0x1B, '['.code.toByte(), '2'.code.toByte(), '0'.code.toByte(), '0'.code.toByte(), '~'.code.toByte()))) {
        "Buffer must start with ESC [ 200 ~"
    }

    // Find the end marker ESC [ 201 ~
    val endMarker = byteArrayOf(0x1B, '['.code.toByte(), '2'.code.toByte(), '0'.code.toByte(), '1'.code.toByte(), '~'.code.toByte())

    var endIdx = -1
    for (i in 6 until buffer.size - 5) {
        if (buffer.sliceArray(i until i + 6).contentEquals(endMarker)) {
            endIdx = i
            break
        }
    }

    if (endIdx == -1) {
        return ParseResult.Incomplete
    }

    val paste = try {
        buffer.decodeToString(6, endIdx)
    } catch (e: Exception) {
        return ParseResult.Error
    }

    return ParseResult.Success(InternalEvent.Event(Event.Paste(paste)))
}

/**
 * Parses a UTF-8 character from the buffer.
 *
 * @return A Result containing the parsed character (or null if more bytes needed), or an error.
 */
fun parseUtf8Char(buffer: ByteArray): Result<Char?> {
    if (buffer.isEmpty()) {
        return Result.success(null)
    }

    return try {
        val decoded = buffer.decodeToString()
        val ch = decoded.firstOrNull()
        if (ch != null) {
            Result.success(ch)
        } else {
            Result.failure(couldNotParseEventError())
        }
    } catch (e: Exception) {
        // from_utf8 failed, but we have to check if we need more bytes for code point
        // and if all the bytes we have are valid
        val firstByte = buffer[0].toInt() and 0xFF

        val requiredBytes = when {
            // https://en.wikipedia.org/wiki/UTF-8#Description
            firstByte in 0x00..0x7F -> 1  // 0xxxxxxx
            firstByte in 0xC0..0xDF -> 2  // 110xxxxx 10xxxxxx
            firstByte in 0xE0..0xEF -> 3  // 1110xxxx 10xxxxxx 10xxxxxx
            firstByte in 0xF0..0xF7 -> 4  // 11110xxx 10xxxxxx 10xxxxxx 10xxxxxx
            firstByte in 0x80..0xBF || firstByte in 0xF8..0xFF -> {
                return Result.failure(couldNotParseEventError())
            }
            else -> return Result.failure(couldNotParseEventError())
        }

        // More than 1 byte, check continuation bytes for 10xxxxxx pattern
        if (requiredBytes > 1 && buffer.size > 1) {
            for (i in 1 until minOf(buffer.size, requiredBytes)) {
                val b = buffer[i].toInt() and 0xFF
                if ((b and 0b1100_0000) != 0b1000_0000) {
                    return Result.failure(couldNotParseEventError())
                }
            }
        }

        if (buffer.size < requiredBytes) {
            // All bytes look good so far, but we need more of them
            Result.success(null)
        } else {
            Result.failure(couldNotParseEventError())
        }
    }
}

// Extension function for ByteArray to check if it starts with a prefix
private fun ByteArray.startsWith(prefix: ByteArray): Boolean {
    if (this.size < prefix.size) return false
    for (i in prefix.indices) {
        if (this[i] != prefix[i]) return false
    }
    return true
}
