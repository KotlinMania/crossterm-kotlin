// Platform-specific Windows event types
// Corresponds to types from crossterm_winapi crate
@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
package io.github.kotlinmania.crossterm.winapi

import io.github.kotlinmania.crossterm.event.KeyModifiers

/**
 * Windows mouse event flags.
 *
 * Corresponds to crossterm_winapi::EventFlags.
 */
enum class EventFlags {
    PressOrRelease,
    DoubleClick,
    MouseMoved,
    MouseWheeled,
    MouseHwheeled
}

/**
 * Key event record from Windows Console API.
 *
 * Corresponds to crossterm_winapi::KeyEventRecord.
 */
data class KeyEventRecord(
    val keyDown: Boolean,
    val virtualKeyCode: UShort,
    val virtualScanCode: UShort,
    val uChar: UShort,
    val controlKeyState: ControlKeyState
)

/**
 * Control key state from Windows key event.
 *
 * Corresponds to crossterm_winapi::ControlKeyState.
 */
data class ControlKeyState(
    val flags: UInt
) {
    companion object {
        const val SHIFT_PRESSED = 0x0010u
        const val LEFT_CTRL_PRESSED = 0x0008u
        const val RIGHT_CTRL_PRESSED = 0x0004u
        const val LEFT_ALT_PRESSED = 0x0002u
        const val RIGHT_ALT_PRESSED = 0x0001u
        const val CAPSLOCK_ON = 0x0080u
    }

    fun hasState(state: UInt): Boolean = (flags and state) != 0u

    fun toKeyModifiers(): KeyModifiers {
        val shift = hasState(SHIFT_PRESSED)
        val alt = hasState(LEFT_ALT_PRESSED or RIGHT_ALT_PRESSED)
        val control = hasState(LEFT_CTRL_PRESSED or RIGHT_CTRL_PRESSED)

        var modifier = KeyModifiers.empty()
        if (shift) modifier = modifier + KeyModifiers.SHIFT
        if (control) modifier = modifier + KeyModifiers.CONTROL
        if (alt) modifier = modifier + KeyModifiers.ALT

        return modifier
    }
}

/**
 * Mouse event record from Windows Console API.
 *
 * Corresponds to crossterm_winapi::MouseEvent.
 */
data class MouseEventRecord(
    val mousePosition: Coord,
    val buttonState: ButtonState,
    val controlKeyState: ControlKeyState,
    val eventFlags: EventFlags
)

/**
 * Coordinate position.
 */
data class Coord(
    val x: Short,
    val y: Short
)

/**
 * Mouse button state.
 */
data class ButtonState(
    val state: UInt
) {
    fun leftButton(): Boolean = (state and 0x0001u) != 0u
    fun rightButton(): Boolean = (state and 0x0002u) != 0u
    fun middleButton(): Boolean = (state and 0x0004u) != 0u
    fun releaseButton(): Boolean = state == 0u
    fun scrollDown(): Boolean = (state.toInt() and 0xFF000000.toInt()) < 0
    fun scrollUp(): Boolean = (state.toInt() and 0xFF000000.toInt()) > 0
    fun scrollLeft(): Boolean = (state.toInt() and 0xFF000000.toInt()) < 0
    fun scrollRight(): Boolean = (state.toInt() and 0xFF000000.toInt()) > 0
}
