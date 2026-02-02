// port-lint: source event/source/windows.rs
package io.github.kotlinmania.crossterm.event.source

import io.github.kotlinmania.crossterm.event.Event
import io.github.kotlinmania.crossterm.event.InternalEvent
import io.github.kotlinmania.crossterm.event.PollTimeout
import io.github.kotlinmania.crossterm.event.source.Waker
import kotlin.time.Duration

/**
 * Tracks which mouse buttons are currently pressed.
 *
 * Used to determine the correct mouse event type (button down vs. drag)
 * based on the current button state versus the previous state.
 */
data class MouseButtonsPressed(
    val left: Boolean = false,
    val right: Boolean = false,
    val middle: Boolean = false
)

/**
 * Windows-specific event source for reading terminal input.
 *
 * This implementation uses the Windows Console API to read input events
 * such as key presses, mouse events, and window resize events.
 *
 * The event source maintains a buffer for handling UTF-16 surrogate pairs
 * that span multiple input records, and tracks mouse button state for
 * proper handling of mouse drag events.
 *
 * ## Thread Safety
 *
 * This class is not thread-safe. Access should be synchronized externally,
 * typically through the global event reader lock.
 *
 * ## Platform Support
 *
 * This implementation is only available on Windows (mingw target).
 * On Unix platforms, use [UnixInternalEventSource] instead.
 *
 * @see EventSource
 * @see io.github.kotlinmania.crossterm.event.source.unix.MioUnixInternalEventSource
 */
class WindowsEventSource private constructor(
    private val console: Console,
    private val poll: WinApiPoll,
    private var surrogateBuffer: UShort?,
    private var mouseButtonsPressed: MouseButtonsPressed
) : EventSource {

    companion object {
        /**
         * Creates a new Windows event source.
         *
         * Initializes the console handle and polling mechanism for reading
         * Windows console input events.
         *
         * @return A new [WindowsEventSource] instance.
         * @throws Exception if the console handle cannot be obtained.
         */
        fun new(): WindowsEventSource {
            val console = Console.fromHandle(Handle.currentInHandle())
            return WindowsEventSource(
                console = console,
                poll = WinApiPoll.new(),
                surrogateBuffer = null,
                mouseButtonsPressed = MouseButtonsPressed()
            )
        }
    }

    /**
     * Tries to read an [InternalEvent] within the given duration.
     *
     * This method polls for Windows console input events and converts them
     * to crossterm events. It handles:
     * - Key events (including UTF-16 surrogate pair handling)
     * - Mouse events (with button state tracking for drag detection)
     * - Window resize events
     * - Focus gained/lost events
     *
     * The method loops until either a valid event is found or the timeout
     * elapses. Events that cannot be parsed (such as certain control key
     * combinations) are silently discarded.
     *
     * @param timeout `null` to block indefinitely until an event is available,
     *   or a [Duration] to block for the given timeout.
     * @return The event if one is available, or `null` if the timeout elapsed
     *   without an event becoming available.
     * @throws Exception if an error occurs while reading from the console.
     */
    override fun tryRead(timeout: Duration?): InternalEvent? {
        val pollTimeout = PollTimeout.new(timeout)

        while (true) {
            val eventReady = poll.poll(pollTimeout.leftover())

            if (eventReady == true) {
                val numberOfEvents = console.numberOfConsoleInputEvents()
                if (numberOfEvents != 0u) {
                    val inputRecord = console.readSingleInputEvent()

                    val event: Event? = when (inputRecord) {
                        is InputRecord.KeyEvent -> {
                            val keyResult = handleKeyEvent(inputRecord.record, surrogateBuffer)
                            when (keyResult) {
                                is KeyEventResult.Event -> {
                                    surrogateBuffer = null
                                    keyResult.toEvent()
                                }
                                is KeyEventResult.Surrogate -> {
                                    surrogateBuffer = keyResult.value
                                    null
                                }
                                null -> null
                            }
                        }
                        is InputRecord.MouseEvent -> {
                            val mouseEvent = handleMouseEvent(inputRecord.record, mouseButtonsPressed)
                            // Update button state for next event
                            mouseButtonsPressed = MouseButtonsPressed(
                                left = inputRecord.record.buttonState.leftButton(),
                                right = inputRecord.record.buttonState.rightButton(),
                                middle = inputRecord.record.buttonState.middleButton()
                            )
                            mouseEvent
                        }
                        is InputRecord.WindowBufferSizeEvent -> {
                            // Windows starts counting at 0, Unix at 1, add one to replicate Unix behavior.
                            val columns = ((inputRecord.record.size.x.toInt() + 1).coerceAtLeast(0)).toUShort()
                            val rows = ((inputRecord.record.size.y.toInt() + 1).coerceAtLeast(0)).toUShort()
                            Event.Resize(columns, rows)
                        }
                        is InputRecord.FocusEvent -> {
                            if (inputRecord.record.setFocus) {
                                Event.FocusGained
                            } else {
                                Event.FocusLost
                            }
                        }
                        else -> null
                    }

                    if (event != null) {
                        return InternalEvent.EventWrapper(event)
                    }
                }
            }

            if (pollTimeout.elapsed()) {
                return null
            }
        }
    }

    /**
     * Returns a [Waker] allowing to wake/force the [tryRead] method to return `null`.
     *
     * This is used for event-stream support to allow external signals to interrupt
     * a blocking poll operation. The waker can be used from another thread to
     * unblock a waiting read operation.
     *
     * @return A [Waker] instance for this event source, or `null` if waking is not supported.
     */
    override fun waker(): Waker? = poll.waker()
}

/**
 * Result type for key event parsing.
 *
 * Key events can result in either a complete event or a surrogate value
 * that needs to be combined with the next input to form a complete
 * Unicode character.
 */
internal sealed class KeyEventResult {
    /**
     * A complete key event.
     */
    data class Event(val event: io.github.kotlinmania.crossterm.event.KeyEvent) : KeyEventResult() {
        fun toEvent(): io.github.kotlinmania.crossterm.event.Event =
            io.github.kotlinmania.crossterm.event.Event.Key(event)
    }

    /**
     * A UTF-16 surrogate value that needs to be paired with the next surrogate.
     */
    data class Surrogate(val value: UShort) : KeyEventResult() {
        fun toEvent(): io.github.kotlinmania.crossterm.event.Event? = null
    }
}

/**
 * Handles a Windows key event record.
 *
 * This function processes a key event from the Windows Console API and
 * converts it to a crossterm key event. It handles UTF-16 surrogate pairs
 * that span multiple input records.
 *
 * @param keyEvent The key event record from the Windows Console API.
 * @param surrogateBuffer The buffered high surrogate from a previous event, if any.
 * @return The parsed key event result, or `null` if the event should be ignored.
 */
internal fun handleKeyEvent(keyEvent: KeyEventRecord, surrogateBuffer: UShort?): KeyEventResult? {
    val windowsKeyEvent = parseKeyEventRecord(keyEvent) ?: return null

    return when (windowsKeyEvent) {
        is WindowsKeyEvent.KeyEvent -> {
            // Discard any buffered surrogate value if another valid key event comes before the
            // next surrogate value.
            KeyEventResult.Event(windowsKeyEvent.event)
        }
        is WindowsKeyEvent.Surrogate -> {
            val char = handleSurrogate(surrogateBuffer, windowsKeyEvent.value)
            if (char != null) {
                val modifiers = keyEvent.controlKeyState.toKeyModifiers()
                KeyEventResult.Event(
                    io.github.kotlinmania.crossterm.event.KeyEvent.new(
                        io.github.kotlinmania.crossterm.event.KeyCode.Char(char),
                        modifiers
                    )
                )
            } else {
                KeyEventResult.Surrogate(windowsKeyEvent.value)
            }
        }
    }
}

/**
 * Internal representation of a Windows key event during parsing.
 */
internal sealed class WindowsKeyEvent {
    data class KeyEvent(val event: io.github.kotlinmania.crossterm.event.KeyEvent) : WindowsKeyEvent()
    data class Surrogate(val value: UShort) : WindowsKeyEvent()
}

/**
 * Handles UTF-16 surrogate pair combining.
 *
 * When the first surrogate is received, it's buffered. When the second
 * surrogate arrives, they are combined to form a complete Unicode character.
 *
 * @param surrogateBuffer The buffered high surrogate, if any.
 * @param newSurrogate The new surrogate value.
 * @return The complete character if both surrogates are now available, or `null` if buffering.
 */
internal fun handleSurrogate(surrogateBuffer: UShort?, newSurrogate: UShort): Char? {
    return if (surrogateBuffer != null) {
        // We have a buffered high surrogate and now received the low surrogate
        val highSurrogate = surrogateBuffer.toInt().toChar()
        val lowSurrogate = newSurrogate.toInt().toChar()

        // Decode the surrogate pair to a Unicode code point
        if (highSurrogate.isHighSurrogate() && lowSurrogate.isLowSurrogate()) {
            // Decode UTF-16 surrogate pair to code point using standard formula
            val codePoint = 0x10000 + ((highSurrogate.code - 0xD800) shl 10) + (lowSurrogate.code - 0xDC00)
            // Convert code point back to chars - for supplementary characters,
            // use the string representation and get the first char
            val chars = charArrayOf(highSurrogate, lowSurrogate)
            chars.concatToString().firstOrNull()
        } else {
            null
        }
    } else {
        // Buffer this surrogate for the next event
        null
    }
}

/**
 * Handles a Windows mouse event record.
 *
 * This function converts a Windows mouse event to a crossterm mouse event,
 * taking into account the previous button state to properly detect
 * button press, release, and drag events.
 *
 * @param mouseEvent The mouse event record from the Windows Console API.
 * @param buttonsPressed The previous mouse button state.
 * @return The parsed mouse event, or `null` if the event should be ignored.
 */
internal fun handleMouseEvent(
    mouseEvent: MouseEventRecord,
    buttonsPressed: MouseButtonsPressed
): Event? {
    return parseMouseEventRecord(mouseEvent, buttonsPressed)?.let { mouseEventData ->
        Event.Mouse(mouseEventData)
    }
}

// Re-export types from sys.windows module for use in this file.
// The actual types are defined in parse.kt with full implementations.
typealias KeyEventRecord = io.github.kotlinmania.crossterm.event.sys.windows.KeyEventRecord
typealias MouseEventRecord = io.github.kotlinmania.crossterm.event.sys.windows.MouseEventRecord
typealias ButtonState = io.github.kotlinmania.crossterm.event.sys.windows.ButtonState
typealias ControlKeyState = io.github.kotlinmania.crossterm.event.sys.windows.ControlKeyState
typealias Coord = io.github.kotlinmania.crossterm.event.sys.windows.Coord

/**
 * Windows console input record types.
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
 * Placeholder for Windows console handle wrapper.
 *
 * TODO: Implement with actual Windows API bindings.
 */
class Handle private constructor(private val value: Long) {
    companion object {
        fun currentInHandle(): Handle {
            // TODO: Implement with GetStdHandle(STD_INPUT_HANDLE)
            return Handle(0L)
        }
    }
}

/**
 * Placeholder for Windows console wrapper.
 *
 * TODO: Implement with actual Windows API bindings.
 */
class Console private constructor(private val handle: Handle) {
    companion object {
        fun fromHandle(handle: Handle): Console = Console(handle)
    }

    fun numberOfConsoleInputEvents(): UInt {
        // TODO: Implement with GetNumberOfConsoleInputEvents
        return 0u
    }

    fun readSingleInputEvent(): InputRecord {
        // TODO: Implement with ReadConsoleInput
        throw NotImplementedError("Console.readSingleInputEvent not yet implemented")
    }
}

/**
 * Windows API polling wrapper.
 *
 * TODO: Implement with actual Windows API bindings.
 */
class WinApiPoll private constructor(
    private val waker: io.github.kotlinmania.crossterm.event.sys.windows.Waker?
) {
    companion object {
        fun new(): WinApiPoll = WinApiPoll(waker = null)

        fun newWithWaker(): WinApiPoll = WinApiPoll(
            waker = io.github.kotlinmania.crossterm.event.sys.windows.Waker.new()
        )
    }

    fun poll(timeout: Duration?): Boolean? {
        // TODO: Implement with WaitForMultipleObjects
        return null
    }

    fun waker(): Waker? = waker
}

/**
 * Parses a Windows key event record into a crossterm key event.
 */
internal fun parseKeyEventRecord(keyEvent: KeyEventRecord): WindowsKeyEvent? {
    val result = io.github.kotlinmania.crossterm.event.sys.windows.parseKeyEventRecord(keyEvent)
    return when (result) {
        is io.github.kotlinmania.crossterm.event.sys.windows.WindowsKeyEvent.KeyEventWrapper ->
            WindowsKeyEvent.KeyEvent(result.event)
        is io.github.kotlinmania.crossterm.event.sys.windows.WindowsKeyEvent.Surrogate ->
            WindowsKeyEvent.Surrogate(result.value)
        null -> null
    }
}

/**
 * Parses a Windows mouse event record into a crossterm mouse event.
 */
internal fun parseMouseEventRecord(
    mouseEvent: MouseEventRecord,
    buttonsPressed: MouseButtonsPressed
): io.github.kotlinmania.crossterm.event.MouseEvent? {
    return io.github.kotlinmania.crossterm.event.sys.windows.parseMouseEventRecord(mouseEvent, buttonsPressed)
}
