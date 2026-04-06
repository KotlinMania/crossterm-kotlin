// port-lint: source event/source/windows.rs
@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
package io.github.kotlinmania.crossterm.event.source

import io.github.kotlinmania.crossterm.event.Event
import io.github.kotlinmania.crossterm.event.InternalEvent
import io.github.kotlinmania.crossterm.event.PollTimeout
import io.github.kotlinmania.crossterm.event.sys.Waker
import io.github.kotlinmania.crossterm.event.sys.windows.MouseButtonsPressed
import io.github.kotlinmania.crossterm.event.sys.windows.handleKeyEvent
import io.github.kotlinmania.crossterm.event.sys.windows.handleMouseEvent
import io.github.kotlinmania.crossterm.event.sys.windows.WinApiPoll
import io.github.kotlinmania.crossterm.winapi.Console
import io.github.kotlinmania.crossterm.winapi.Handle
import io.github.kotlinmania.crossterm.winapi.InputRecord
import kotlin.time.Duration

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
 * On Unix platforms, use [io.github.kotlinmania.crossterm.event.source.unix.MioInternalEventSource] instead.
 *
 * @see EventSource
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
            val console = Console.from(Handle.currentInHandle())
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
                val number = console.numberOfConsoleInputEvents()
                if (number != 0u) {
                    val inputRecord = console.readSingleInputEvent()

                    val event: Event? = when (inputRecord) {
                        is InputRecord.KeyEvent -> {
                            val (keyEvent, newSurrogate) = handleKeyEvent(inputRecord.record, surrogateBuffer)
                            surrogateBuffer = newSurrogate
                            keyEvent
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
                        return InternalEvent.Event(event)
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
