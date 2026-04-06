// port-lint: source event/source/unix/mio.rs
package io.github.kotlinmania.crossterm.event.source.unix

import io.github.kotlinmania.crossterm.event.Event
import io.github.kotlinmania.crossterm.event.InternalEvent
import io.github.kotlinmania.crossterm.event.PollTimeout
import io.github.kotlinmania.crossterm.event.source.EventSource
import io.github.kotlinmania.crossterm.event.sys.Waker
import io.github.kotlinmania.crossterm.event.sys.unix.waker.MioWaker
import kotlinx.cinterop.CArrayPointer
import kotlinx.cinterop.ExperimentalForeignApi

import kotlinx.cinterop.addressOf
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.convert
import kotlinx.cinterop.get
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.set
import kotlinx.cinterop.usePinned
import platform.posix.EAGAIN
import platform.posix.EINTR
import platform.posix.EWOULDBLOCK
import platform.posix.F_GETFL
import platform.posix.F_SETFL
import platform.posix.O_NONBLOCK
import platform.posix.POLLIN
import platform.posix.STDIN_FILENO
import platform.posix.close
import platform.posix.errno
import platform.posix.fcntl
import platform.posix.isatty
import platform.posix.open
import platform.posix.poll
import platform.posix.pollfd
import platform.posix.read
import kotlin.time.Duration

/**
 * Token value for the TTY file descriptor in poll operations.
 */
private const val TTY_TOKEN: Int = 0

/**
 * Token value for the signal (SIGWINCH) in poll operations.
 */
@Suppress("unused")
private const val SIGNAL_TOKEN: Int = 1

/**
 * Token value for the wake signal in poll operations (event-stream support).
 */
@Suppress("unused")
private const val WAKE_TOKEN: Int = 2

/**
 * Buffer size for reading from TTY.
 *
 * As noted in the original Rust implementation, reading more than 1,022 bytes
 * in a single read was not observed during testing on macOS/Linux, so a 1KB
 * buffer is sufficient.
 */
private const val TTY_BUFFER_SIZE: Int = 1024

/**
 * Unix internal event source using poll-based I/O multiplexing.
 *
 * This is the primary event source implementation for Unix platforms (macOS, Linux).
 * It uses poll() to wait for input on the TTY file descriptor and handles:
 * - Terminal input events (keyboard, mouse)
 * - Window resize signals (SIGWINCH)
 * - Wake signals for event-stream support
 *
 * The event source maintains a parser to handle multi-byte ANSI escape sequences
 * that may arrive across multiple read operations.
 *
 * This implementation corresponds to the mio-based event source in the original
 * Rust crossterm library, adapted to use POSIX poll() directly since Kotlin/Native
 * doesn't have a mio equivalent.
 *
 * @property ttyFd The file descriptor for the TTY input.
 * @property closeOnDrop Whether to close the file descriptor when the source is closed.
 */
@OptIn(ExperimentalForeignApi::class)
class MioInternalEventSource private constructor(
    private val ttyFd: Int,
    private val closeOnDrop: Boolean
) : EventSource, AutoCloseable {

    /**
     * Internal buffer for reading from TTY.
     */
    private val ttyBuffer = ByteArray(TTY_BUFFER_SIZE)

    /**
     * Parser for ANSI escape sequences.
     */
    private val parser = MioParser()

    /**
     * Waker for interrupting poll operations (event-stream support).
     *
     * MioWaker internally creates a pipe; use [MioWaker.pollFd] to get
     * the read end for poll() and [MioWaker.wake] to signal from another thread.
     */
    private val mioWaker: MioWaker? = try {
        MioWaker.new()
    } catch (_: IllegalStateException) {
        null
    }

    init {
        // Set TTY to non-blocking mode
        setNonBlocking(ttyFd)
    }

    companion object {
        /**
         * Creates a new [MioInternalEventSource] using the default TTY.
         *
         * This will use STDIN if it's a TTY, otherwise it will open /dev/tty.
         *
         * @return A new [MioInternalEventSource] instance.
         * @throws IllegalStateException if the TTY cannot be opened.
         */
        fun new(): MioInternalEventSource = fromTtyFd()

        /**
         * Creates a new [MioInternalEventSource] from a file descriptor.
         *
         * @param fd The file descriptor to use for input.
         * @param closeOnDrop Whether to close the file descriptor when done.
         * @return A new [MioInternalEventSource] instance.
         */
        fun fromFileDescriptor(fd: Int, closeOnDrop: Boolean = false): MioInternalEventSource =
            MioInternalEventSource(fd, closeOnDrop)

        /**
         * Creates a new [MioInternalEventSource] using the system TTY.
         *
         * Uses STDIN if it's a TTY, otherwise opens /dev/tty.
         *
         * @return A new [MioInternalEventSource] instance.
         * @throws IllegalStateException if no TTY is available.
         */
        private fun fromTtyFd(): MioInternalEventSource {
            return if (isatty(STDIN_FILENO) == 1) {
                MioInternalEventSource(STDIN_FILENO, closeOnDrop = false)
            } else {
                val fd = open("/dev/tty", platform.posix.O_RDWR)
                if (fd < 0) {
                    throw IllegalStateException("Failed to open /dev/tty: errno=$errno")
                }
                MioInternalEventSource(fd, closeOnDrop = true)
            }
        }

        /**
         * Sets a file descriptor to non-blocking mode.
         *
         * @param fd The file descriptor to modify.
         */
        private fun setNonBlocking(fd: Int) {
            val flags = fcntl(fd, F_GETFL)
            if (flags >= 0) {
                fcntl(fd, F_SETFL, flags or O_NONBLOCK)
            }
        }
    }

    /**
     * Tries to read an [InternalEvent] within the given duration.
     *
     * This method first checks if the parser has any buffered events from
     * previous reads. If not, it uses poll() to wait for input and processes
     * any incoming data.
     *
     * The method handles:
     * - TTY input: Reads and parses terminal input
     * - SIGWINCH signals: Returns resize events
     * - Wake signals: Returns null to indicate the poll was interrupted
     *
     * @param timeout The maximum duration to wait, or null to wait indefinitely.
     * @return The next available event, or null if the timeout elapsed or the
     *         poll was interrupted.
     * @throws Exception if an unrecoverable I/O error occurs.
     */
    override fun tryRead(timeout: Duration?): InternalEvent? {
        // Check if parser has buffered events
        parser.next()?.let { return it }

        val pollTimeout = PollTimeout.new(timeout)

        while (true) {
            val timeoutMs = pollTimeout.leftover()?.inWholeMilliseconds?.toInt() ?: -1

            val pollResult = doPoll(timeoutMs)

            if (pollResult < 0) {
                // Check for EINTR - retry the poll
                if (errno == EINTR) {
                    continue
                }
                throw IllegalStateException("Poll failed: errno=$errno")
            }

            if (pollResult == 0) {
                // Timeout - no events
                return null
            }

            // Process TTY input
            val ttyEvent = processTtyInput()
            if (ttyEvent != null) {
                return ttyEvent
            }

            // Check for wake signal
            mioWaker?.let { waker ->
                if (consumeWakeSignal(waker.pollFd())) {
                    // Poll was woken up - reset waker and throw interrupted error
                    waker.reset()
                    throw MioInterruptedException("Poll operation was woken up by Waker::wake")
                }
            }

            // Check timeout expiration
            if (pollTimeout.elapsed()) {
                return null
            }
        }
    }

    /**
     * Performs the poll system call with the configured file descriptors.
     *
     * @param timeoutMs The timeout in milliseconds, or -1 for infinite wait.
     * @return The number of file descriptors with events, 0 for timeout, or -1 on error.
     */
    private fun doPoll(timeoutMs: Int): Int {
        return memScoped {
            val wakeFd = mioWaker?.pollFd()
            val numFds = if (wakeFd != null) 2 else 1
            val fds: CArrayPointer<pollfd> = allocArray(numFds)

            // TTY file descriptor
            fds[TTY_TOKEN].fd = ttyFd
            fds[TTY_TOKEN].events = POLLIN.toShort()
            fds[TTY_TOKEN].revents = 0

            // Wake pipe read end (if available)
            if (wakeFd != null) {
                fds[1].fd = wakeFd
                fds[1].events = POLLIN.toShort()
                fds[1].revents = 0
            }

            poll(fds, numFds.convert(), timeoutMs)
        }
    }

    /**
     * Processes input from the TTY file descriptor.
     *
     * Reads available data in a loop until EWOULDBLOCK/EAGAIN is returned,
     * parsing each chunk through the event parser.
     *
     * @return The next parsed event, or null if no complete event is available yet.
     */
    private fun processTtyInput(): InternalEvent? {
        while (true) {
            val readResult = ttyBuffer.usePinned { pinned ->
                read(ttyFd, pinned.addressOf(0), TTY_BUFFER_SIZE.toULong())
            }

            if (readResult > 0) {
                val readCount = readResult.toInt()
                parser.advance(
                    ttyBuffer.copyOf(readCount),
                    more = readCount == TTY_BUFFER_SIZE
                )
            } else if (readResult < 0) {
                // Check for EWOULDBLOCK/EAGAIN - no more data available
                if (errno == EWOULDBLOCK || errno == EAGAIN) {
                    break
                }
                // Check for EINTR - retry
                if (errno == EINTR) {
                    continue
                }
                // Other error - stop reading
                break
            } else {
                // EOF
                break
            }

            // Check if parser has a complete event
            parser.next()?.let { return it }
        }

        // Return any event that might be in the parser
        return parser.next()
    }

    /**
     * Consumes any pending wake signals from the wake pipe.
     *
     * @param readFd The read end of the wake pipe.
     * @return true if a wake signal was consumed, false otherwise.
     */
    private fun consumeWakeSignal(readFd: Int): Boolean {
        val buffer = ByteArray(1)
        val result = buffer.usePinned { pinned ->
            read(readFd, pinned.addressOf(0), 1uL)
        }
        return result > 0
    }

    /**
     * Returns the waker for this event source.
     *
     * The waker can be used to interrupt a blocking [tryRead] call from
     * another thread, which is useful for event-stream support.
     *
     * @return The waker, or null if wake support is not available.
     */
    override fun waker(): Waker? = mioWaker?.let { waker ->
        // Adapter from MioWaker to source.Waker
        object : Waker {
            override fun wake() = waker.wake()
        }
    }

    /**
     * Closes this event source, releasing any resources.
     *
     * If the TTY file descriptor was opened by this source (not STDIN),
     * it will be closed. The wake pipe (if created) will also be closed.
     */
    override fun close() {
        if (closeOnDrop) {
            close(ttyFd)
        }
        mioWaker?.close()
    }
}

/**
 * Parser for ANSI escape sequences.
 *
 * This parser exists to:
 * - Mimic the anes parser interface
 * - Move the parsing logic out of the main event reading method
 *
 * The parser maintains a buffer for incomplete escape sequences and a queue
 * of parsed events. When data is advanced, it's processed byte-by-byte,
 * attempting to parse complete events. Events that can't be parsed yet
 * (incomplete sequences) are buffered, while unparseable sequences are
 * discarded.
 *
 * Buffer sizing notes from the original implementation:
 * - The sequence buffer (256 bytes) holds a single ANSI escape sequence
 * - The event queue (128 capacity) is based on the assumption that
 *   average ANSI sequences are ~8 bytes, so 1024/8 = 128 events max
 */
private class MioParser {
    /**
     * Buffer for incomplete ANSI escape sequences.
     *
     * This buffer holds a single escape sequence as it's being parsed.
     * 256 bytes should be sufficient for any known ANSI sequence.
     */
    private val buffer: MutableList<Byte> = ArrayList(256)

    /**
     * Queue of parsed internal events.
     *
     * Events are added to this queue as they're parsed and removed
     * via [next]. The capacity of 128 is based on the TTY buffer size
     * divided by the average escape sequence length.
     */
    private val internalEvents: ArrayDeque<InternalEvent> = ArrayDeque(128)

    /**
     * Returns the next available event, or null if none are available.
     *
     * @return The next event from the queue, or null.
     */
    fun next(): InternalEvent? = internalEvents.removeFirstOrNull()

    /**
     * Advances the parser with new input data.
     *
     * Processes each byte in the buffer, attempting to parse complete events.
     * The [more] flag indicates whether more data is immediately available,
     * which affects ambiguous sequence parsing (e.g., distinguishing Escape
     * key from Escape sequence start).
     *
     * @param data The new input bytes to process.
     * @param more Whether more input data is immediately available.
     */
    fun advance(data: ByteArray, more: Boolean) {
        for ((idx, byte) in data.withIndex()) {
            val moreAvailable = idx + 1 < data.size || more

            buffer.add(byte)

            val parseResult = mioParseEvent(buffer.toByteArray(), moreAvailable)

            when (parseResult) {
                is MioParseResult.Event -> {
                    internalEvents.addLast(parseResult.event)
                    buffer.clear()
                }
                is MioParseResult.Incomplete -> {
                    // Keep buffer and continue processing
                }
                is MioParseResult.Error -> {
                    // Unparseable sequence - clear and continue
                    buffer.clear()
                }
            }
        }
    }
}

/**
 * Result of attempting to parse an event from a byte buffer.
 */
private sealed class MioParseResult {
    /**
     * Successfully parsed an event.
     */
    data class Event(val event: InternalEvent) : MioParseResult()

    /**
     * Need more bytes to complete the sequence.
     */
    data object Incomplete : MioParseResult()

    /**
     * Failed to parse - sequence is invalid.
     */
    data object Error : MioParseResult()
}

/**
 * Parses an event from the given byte buffer.
 *
 * This is a simplified parser that handles basic terminal input.
 * A full implementation would handle all ANSI escape sequences as
 * defined in the Rust crossterm parse.rs file.
 *
 * @param buffer The bytes to parse.
 * @param inputAvailable Whether more input is immediately available.
 * @return The parse result indicating success, need for more data, or error.
 */
private fun mioParseEvent(buffer: ByteArray, inputAvailable: Boolean): MioParseResult {
    if (buffer.isEmpty()) {
        return MioParseResult.Incomplete
    }

    return when (buffer[0]) {
        0x1B.toByte() -> mioParseEscapeSequence(buffer, inputAvailable)
        0x0D.toByte() -> MioParseResult.Event(mioKeyEvent(io.github.kotlinmania.crossterm.event.KeyCode.Enter))
        0x09.toByte() -> MioParseResult.Event(mioKeyEvent(io.github.kotlinmania.crossterm.event.KeyCode.Tab))
        0x7F.toByte() -> MioParseResult.Event(mioKeyEvent(io.github.kotlinmania.crossterm.event.KeyCode.Backspace))
        in 0x01..0x1A -> {
            // Ctrl+A through Ctrl+Z
            val char = ('a'.code + buffer[0].toInt() - 0x01).toChar()
            MioParseResult.Event(
                InternalEvent.Event(
                    Event.Key(
                        io.github.kotlinmania.crossterm.event.KeyEvent(
                            io.github.kotlinmania.crossterm.event.KeyCode.Char(char),
                            io.github.kotlinmania.crossterm.event.KeyModifiers.CONTROL
                        )
                    )
                )
            )
        }
        0x00.toByte() -> {
            // Ctrl+Space
            MioParseResult.Event(
                InternalEvent.Event(
                    Event.Key(
                        io.github.kotlinmania.crossterm.event.KeyEvent(
                            io.github.kotlinmania.crossterm.event.KeyCode.Char(' '),
                            io.github.kotlinmania.crossterm.event.KeyModifiers.CONTROL
                        )
                    )
                )
            )
        }
        else -> mioParseUtf8Char(buffer)
    }
}

/**
 * Parses an escape sequence starting with ESC (0x1B).
 */
private fun mioParseEscapeSequence(buffer: ByteArray, inputAvailable: Boolean): MioParseResult {
    if (buffer.size == 1) {
        return if (inputAvailable) {
            // Might be start of escape sequence
            MioParseResult.Incomplete
        } else {
            // Just the Escape key
            MioParseResult.Event(mioKeyEvent(io.github.kotlinmania.crossterm.event.KeyCode.Esc))
        }
    }

    return when (buffer[1]) {
        0x5B.toByte() -> mioParseCsiSequence(buffer) // ESC [
        0x4F.toByte() -> mioParseSs3Sequence(buffer) // ESC O
        0x1B.toByte() -> MioParseResult.Event(mioKeyEvent(io.github.kotlinmania.crossterm.event.KeyCode.Esc))
        else -> {
            // Alt + key
            if (buffer.size >= 2) {
                val charResult = mioParseUtf8Char(buffer.sliceArray(1 until buffer.size))
                if (charResult is MioParseResult.Event) {
                    val innerEvent = charResult.event
                    if (innerEvent is InternalEvent.Event && innerEvent.event is Event.Key) {
                        val keyEvent = innerEvent.event.keyEvent
                        return MioParseResult.Event(
                            InternalEvent.Event(
                                Event.Key(
                                    keyEvent.copy(
                                        modifiers = keyEvent.modifiers + io.github.kotlinmania.crossterm.event.KeyModifiers.ALT
                                    )
                                )
                            )
                        )
                    }
                }
            }
            MioParseResult.Error
        }
    }
}

/**
 * Parses a CSI (Control Sequence Introducer) sequence.
 * These sequences start with ESC [ (0x1B 0x5B).
 */
private fun mioParseCsiSequence(buffer: ByteArray): MioParseResult {
    if (buffer.size < 3) {
        return MioParseResult.Incomplete
    }

    // Check for final byte (0x40-0x7E)
    val lastByte = buffer.last()
    if (lastByte !in 0x40..0x7E) {
        // Not a final byte yet - need more input
        if (buffer.size < 32) { // Reasonable max length
            return MioParseResult.Incomplete
        }
        return MioParseResult.Error
    }

    // Parse based on final byte
    return when (lastByte.toInt().toChar()) {
        'A' -> MioParseResult.Event(mioKeyEvent(io.github.kotlinmania.crossterm.event.KeyCode.Up))
        'B' -> MioParseResult.Event(mioKeyEvent(io.github.kotlinmania.crossterm.event.KeyCode.Down))
        'C' -> MioParseResult.Event(mioKeyEvent(io.github.kotlinmania.crossterm.event.KeyCode.Right))
        'D' -> MioParseResult.Event(mioKeyEvent(io.github.kotlinmania.crossterm.event.KeyCode.Left))
        'H' -> MioParseResult.Event(mioKeyEvent(io.github.kotlinmania.crossterm.event.KeyCode.Home))
        'F' -> MioParseResult.Event(mioKeyEvent(io.github.kotlinmania.crossterm.event.KeyCode.End))
        '~' -> mioParseCsiTildeSequence(buffer)
        else -> MioParseResult.Error
    }
}

/**
 * Parses CSI sequences ending with ~ (special keys).
 */
private fun mioParseCsiTildeSequence(buffer: ByteArray): MioParseResult {
    // Extract the number between [ and ~
    val numStr = buffer.sliceArray(2 until buffer.size - 1)
        .map { it.toInt().toChar() }
        .joinToString("")
        .takeWhile { it.isDigit() }

    val num = numStr.toIntOrNull() ?: return MioParseResult.Error

    return when (num) {
        1 -> MioParseResult.Event(mioKeyEvent(io.github.kotlinmania.crossterm.event.KeyCode.Home))
        2 -> MioParseResult.Event(mioKeyEvent(io.github.kotlinmania.crossterm.event.KeyCode.Insert))
        3 -> MioParseResult.Event(mioKeyEvent(io.github.kotlinmania.crossterm.event.KeyCode.Delete))
        4 -> MioParseResult.Event(mioKeyEvent(io.github.kotlinmania.crossterm.event.KeyCode.End))
        5 -> MioParseResult.Event(mioKeyEvent(io.github.kotlinmania.crossterm.event.KeyCode.PageUp))
        6 -> MioParseResult.Event(mioKeyEvent(io.github.kotlinmania.crossterm.event.KeyCode.PageDown))
        11 -> MioParseResult.Event(mioKeyEvent(io.github.kotlinmania.crossterm.event.KeyCode.F(1u)))
        12 -> MioParseResult.Event(mioKeyEvent(io.github.kotlinmania.crossterm.event.KeyCode.F(2u)))
        13 -> MioParseResult.Event(mioKeyEvent(io.github.kotlinmania.crossterm.event.KeyCode.F(3u)))
        14 -> MioParseResult.Event(mioKeyEvent(io.github.kotlinmania.crossterm.event.KeyCode.F(4u)))
        15 -> MioParseResult.Event(mioKeyEvent(io.github.kotlinmania.crossterm.event.KeyCode.F(5u)))
        17 -> MioParseResult.Event(mioKeyEvent(io.github.kotlinmania.crossterm.event.KeyCode.F(6u)))
        18 -> MioParseResult.Event(mioKeyEvent(io.github.kotlinmania.crossterm.event.KeyCode.F(7u)))
        19 -> MioParseResult.Event(mioKeyEvent(io.github.kotlinmania.crossterm.event.KeyCode.F(8u)))
        20 -> MioParseResult.Event(mioKeyEvent(io.github.kotlinmania.crossterm.event.KeyCode.F(9u)))
        21 -> MioParseResult.Event(mioKeyEvent(io.github.kotlinmania.crossterm.event.KeyCode.F(10u)))
        23 -> MioParseResult.Event(mioKeyEvent(io.github.kotlinmania.crossterm.event.KeyCode.F(11u)))
        24 -> MioParseResult.Event(mioKeyEvent(io.github.kotlinmania.crossterm.event.KeyCode.F(12u)))
        else -> MioParseResult.Error
    }
}

/**
 * Parses an SS3 (Single Shift 3) sequence.
 * These sequences start with ESC O (0x1B 0x4F).
 */
private fun mioParseSs3Sequence(buffer: ByteArray): MioParseResult {
    if (buffer.size < 3) {
        return MioParseResult.Incomplete
    }

    return when (buffer[2].toInt().toChar()) {
        'A' -> MioParseResult.Event(mioKeyEvent(io.github.kotlinmania.crossterm.event.KeyCode.Up))
        'B' -> MioParseResult.Event(mioKeyEvent(io.github.kotlinmania.crossterm.event.KeyCode.Down))
        'C' -> MioParseResult.Event(mioKeyEvent(io.github.kotlinmania.crossterm.event.KeyCode.Right))
        'D' -> MioParseResult.Event(mioKeyEvent(io.github.kotlinmania.crossterm.event.KeyCode.Left))
        'H' -> MioParseResult.Event(mioKeyEvent(io.github.kotlinmania.crossterm.event.KeyCode.Home))
        'F' -> MioParseResult.Event(mioKeyEvent(io.github.kotlinmania.crossterm.event.KeyCode.End))
        'P' -> MioParseResult.Event(mioKeyEvent(io.github.kotlinmania.crossterm.event.KeyCode.F(1u)))
        'Q' -> MioParseResult.Event(mioKeyEvent(io.github.kotlinmania.crossterm.event.KeyCode.F(2u)))
        'R' -> MioParseResult.Event(mioKeyEvent(io.github.kotlinmania.crossterm.event.KeyCode.F(3u)))
        'S' -> MioParseResult.Event(mioKeyEvent(io.github.kotlinmania.crossterm.event.KeyCode.F(4u)))
        else -> MioParseResult.Error
    }
}

/**
 * Parses a UTF-8 character from the buffer.
 */
private fun mioParseUtf8Char(buffer: ByteArray): MioParseResult {
    if (buffer.isEmpty()) {
        return MioParseResult.Incomplete
    }

    val firstByte = buffer[0].toInt() and 0xFF

    // Determine expected length based on first byte
    val expectedLength = when {
        firstByte and 0x80 == 0 -> 1      // ASCII (0xxxxxxx)
        firstByte and 0xE0 == 0xC0 -> 2   // 2-byte (110xxxxx)
        firstByte and 0xF0 == 0xE0 -> 3   // 3-byte (1110xxxx)
        firstByte and 0xF8 == 0xF0 -> 4   // 4-byte (11110xxx)
        else -> return MioParseResult.Error   // Invalid UTF-8 start byte
    }

    if (buffer.size < expectedLength) {
        return MioParseResult.Incomplete
    }

    return try {
        val charString = buffer.sliceArray(0 until expectedLength).decodeToString()
        if (charString.isNotEmpty()) {
            val char = charString[0]
            val modifiers = if (char.isUpperCase()) {
                io.github.kotlinmania.crossterm.event.KeyModifiers.SHIFT
            } else {
                io.github.kotlinmania.crossterm.event.KeyModifiers.NONE
            }
            MioParseResult.Event(
                InternalEvent.Event(
                    Event.Key(
                        io.github.kotlinmania.crossterm.event.KeyEvent(
                            io.github.kotlinmania.crossterm.event.KeyCode.Char(char),
                            modifiers
                        )
                    )
                )
            )
        } else {
            MioParseResult.Error
        }
    } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
        MioParseResult.Error
    }
}

/**
 * Helper function to create a simple key event.
 */
private fun mioKeyEvent(code: io.github.kotlinmania.crossterm.event.KeyCode): InternalEvent =
    InternalEvent.Event(
        Event.Key(
            io.github.kotlinmania.crossterm.event.KeyEvent(code)
        )
    )

/**
 * Custom InterruptedException for poll interruption.
 *
 * This exception is thrown when a poll operation is interrupted by
 * the waker, indicating that the caller should handle the interruption
 * appropriately (typically by returning null from tryRead).
 */
class MioInterruptedException(message: String) : Exception(message)
