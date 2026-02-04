// Platform-specific Windows console wrapper
// Corresponds to crossterm_winapi::Console
@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
package io.github.kotlinmania.crossterm.winapi

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.windows.DWORDVar
import platform.windows.FOCUS_EVENT
import platform.windows.GetNumberOfConsoleInputEvents
import platform.windows.INPUT_RECORD
import platform.windows.KEY_EVENT
import platform.windows.MENU_EVENT
import platform.windows.MOUSE_EVENT
import platform.windows.ReadConsoleInputW
import platform.windows.WINDOW_BUFFER_SIZE_EVENT

/**
 * Windows console input record types.
 *
 * Corresponds to crossterm_winapi::InputRecord in Rust.
 */
sealed class InputRecord {
    data class KeyEvent(val record: KeyEventRecord) : InputRecord()
    data class MouseEvent(val record: MouseEventRecord) : InputRecord()
    data class WindowBufferSizeEvent(val record: WindowBufferSizeRecord) : InputRecord()
    data class FocusEvent(val record: FocusEventRecord) : InputRecord()
    data object MenuEvent : InputRecord()
}

/**
 * Window buffer size event record.
 */
data class WindowBufferSizeRecord(
    val size: Coord
)

/**
 * Focus event record.
 */
data class FocusEventRecord(
    val setFocus: Boolean
)

/**
 * Windows console wrapper.
 *
 * This corresponds to crossterm_winapi::Console in Rust.
 */
class Console private constructor(private val handle: Handle) {
    companion object {
        /**
         * Creates a Console from a Handle.
         *
         * Corresponds to `Console::from(handle)` in Rust.
         */
        fun from(handle: Handle): Console = Console(handle)
    }

    /**
     * Gets the number of pending console input events.
     *
     * Corresponds to `Console::number_of_console_input_events()` in Rust.
     */
    fun numberOfConsoleInputEvents(): UInt = memScoped {
        val events = alloc<DWORDVar>()
        val result = GetNumberOfConsoleInputEvents(handle.value, events.ptr)
        if (result == 0) {
            throw IllegalStateException("GetNumberOfConsoleInputEvents failed")
        }
        events.value
    }

    /**
     * Reads a single input event from the console.
     *
     * Corresponds to `Console::read_single_input_event()` in Rust.
     */
    fun readSingleInputEvent(): InputRecord {
        memScoped {
            val record = alloc<INPUT_RECORD>()
            val read = alloc<DWORDVar>()
            if (ReadConsoleInputW(handle.value, record.ptr, 1u, read.ptr) == 0) {
                throw IllegalStateException("ReadConsoleInputW failed")
            }

            return when (record.EventType.toInt()) {
                KEY_EVENT -> {
                    val key = record.Event.KeyEvent!!
                    val keyRecord = KeyEventRecord(
                        keyDown = key.bKeyDown != 0,
                        virtualKeyCode = key.wVirtualKeyCode.toUShort(),
                        virtualScanCode = key.wVirtualScanCode.toUShort(),
                        uChar = key.uChar.UnicodeChar.toUShort(),
                        controlKeyState = ControlKeyState(key.dwControlKeyState.toUInt())
                    )
                    InputRecord.KeyEvent(keyRecord)
                }
                MOUSE_EVENT -> {
                    val mouse = record.Event.MouseEvent!!
                    val mouseRecord = MouseEventRecord(
                        mousePosition = Coord(mouse.dwMousePosition.X, mouse.dwMousePosition.Y),
                        buttonState = ButtonState(mouse.dwButtonState.toUInt()),
                        controlKeyState = ControlKeyState(mouse.dwControlKeyState.toUInt()),
                        eventFlags = when (mouse.dwEventFlags.toInt()) {
                            0 -> EventFlags.PressOrRelease
                            0x0002 -> EventFlags.MouseMoved
                            0x0004 -> EventFlags.DoubleClick
                            0x0008 -> EventFlags.MouseWheeled
                            0x0010 -> EventFlags.MouseHwheeled
                            else -> EventFlags.PressOrRelease
                        }
                    )
                    InputRecord.MouseEvent(mouseRecord)
                }
                WINDOW_BUFFER_SIZE_EVENT -> {
                    val size = record.Event.WindowBufferSizeEvent!!
                    InputRecord.WindowBufferSizeEvent(
                        WindowBufferSizeRecord(Coord(size.dwSize.X, size.dwSize.Y))
                    )
                }
                FOCUS_EVENT -> {
                    val focus = record.Event.FocusEvent!!
                    InputRecord.FocusEvent(FocusEventRecord(focus.bSetFocus != 0))
                }
                MENU_EVENT -> InputRecord.MenuEvent
                else -> InputRecord.MenuEvent
            }
        }
    }
}
