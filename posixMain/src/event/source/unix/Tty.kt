// port-lint: source event/source/unix/tty.rs
package io.github.kotlinmania.crossterm.event.source.unix

import io.github.kotlinmania.crossterm.event.*
import io.github.kotlinmania.crossterm.event.source.EventSource
import io.github.kotlinmania.crossterm.event.source.Waker
import io.github.kotlinmania.crossterm.event.sys.unix.TtyWaker
import io.github.kotlinmania.crossterm.terminal.sys.isRawModeEnabled
import io.github.kotlinmania.crossterm.terminal.sys.pollWrapper
import io.github.kotlinmania.crossterm.terminal.sys.size
import kotlinx.cinterop.*
import platform.posix.*
import kotlin.time.Duration

/**
 * Buffer size for reading from TTY.
 *
 * Testing on macOS/Linux showed that reading more than 1,022 bytes
 * at a time was not possible, so 1,024 bytes is sufficient and
 * avoids unnecessary allocations.
 */
private const val TTY_BUFFER_SIZE = 1024

/**
 * Holds a prototypical Waker and a receiver we can wait on when doing select().
 *
 * This is used for event-stream support to allow external signals to interrupt
 * a blocking poll operation.
 *
 * @property receiverFd The file descriptor for the read end of the wake pipe.
 * @property waker The waker that writes to the wake pipe.
 */
private class WakePipe(
    val receiverFd: Int,
    val waker: TtyWaker
) {
    companion object {
        /**
         * Creates a new [WakePipe] using a Unix socket pair.
         *
         * @return A new [WakePipe] instance.
         * @throws IllegalStateException if the socket pair cannot be created.
         */
        @OptIn(ExperimentalForeignApi::class)
        fun new(): WakePipe {
            val (receiverFd, senderFd) = nonblockingUnixPair()
            return WakePipe(
                receiverFd = receiverFd,
                waker = TtyWaker.new {
                    // Write a single byte to wake up the poll
                    memScoped {
                        val buf = allocArray<ByteVar>(1)
                        buf[0] = 0
                        write(senderFd, buf, 1u)
                    }
                }
            )
        }
    }
}

/**
 * Creates a non-blocking Unix socket pair.
 *
 * @return A pair of (receiver, sender) file descriptors.
 * @throws IllegalStateException if the socket pair cannot be created.
 */
@OptIn(ExperimentalForeignApi::class)
private fun nonblockingUnixPair(): Pair<Int, Int> = memScoped {
    val fds = allocArray<IntVar>(2)
    if (socketpair(AF_UNIX, SOCK_STREAM, 0, fds) != 0) {
        throw IllegalStateException("Failed to create Unix socket pair: ${strerror(errno)?.toKString()}")
    }
    val receiverFd = fds[0]
    val senderFd = fds[1]

    // Set both ends to non-blocking
    setNonblocking(receiverFd)
    setNonblocking(senderFd)

    Pair(receiverFd, senderFd)
}

/**
 * Sets a file descriptor to non-blocking mode.
 *
 * @param fd The file descriptor to modify.
 * @throws IllegalStateException if the operation fails.
 */
@OptIn(ExperimentalForeignApi::class)
private fun setNonblocking(fd: Int) {
    val flags = fcntl(fd, F_GETFL)
    if (flags == -1) {
        throw IllegalStateException("Failed to get file descriptor flags: ${strerror(errno)?.toKString()}")
    }
    if (fcntl(fd, F_SETFL, flags or O_NONBLOCK) == -1) {
        throw IllegalStateException("Failed to set non-blocking mode: ${strerror(errno)?.toKString()}")
    }
}

/**
 * TTY-based internal event source for reading terminal events.
 *
 * This event source uses poll() directly (without the mio library) to wait
 * for input on the TTY file descriptor. It handles SIGWINCH signals for
 * terminal resize events using a Unix socket pair.
 *
 * This implementation is an alternative to [UnixInternalEventSource] from Mio.kt,
 * corresponding to Rust crossterm's `use-dev-tty` feature flag behavior.
 *
 * The implementation maintains an internal parser that buffers bytes and
 * parses ANSI escape sequences into [InternalEvent]s.
 *
 * @property ttyFd The file descriptor for the TTY input.
 * @property closeOnDrop Whether to close the TTY fd when this source is closed.
 */
@OptIn(ExperimentalForeignApi::class)
class TtyInternalEventSource private constructor(
    private val ttyFd: Int,
    private val closeOnDrop: Boolean
) : EventSource, AutoCloseable {

    private val parser = TtyParser()
    private val ttyBuffer = ByteArray(TTY_BUFFER_SIZE)
    private val winchSignalReceiverFd: Int
    private val winchSenderFd: Int
    private val wakePipe: WakePipe?

    init {
        // Set up SIGWINCH signal handler using a socket pair
        val (receiverFd, senderFd) = nonblockingUnixPair()
        winchSignalReceiverFd = receiverFd
        winchSenderFd = senderFd

        // Register signal handler for SIGWINCH
        registerSigwinchHandler(senderFd)

        // Set TTY to non-blocking mode
        setNonblocking(ttyFd)

        // Set up wake pipe for event-stream support
        wakePipe = try {
            WakePipe.new()
        } catch (e: Exception) {
            null
        }
    }

    companion object {
        /**
         * Creates a new [TtyInternalEventSource] using the default TTY.
         *
         * @return A new [TtyInternalEventSource] instance.
         * @throws IllegalStateException if the TTY cannot be opened.
         */
        fun new(): TtyInternalEventSource {
            val (fd, closeOnDrop) = ttyFileDescriptor()
            return fromFileDescriptor(fd, closeOnDrop)
        }

        /**
         * Creates a new [TtyInternalEventSource] from an existing file descriptor.
         *
         * @param inputFd The file descriptor to use for input.
         * @param closeOnDrop Whether to close the fd when done.
         * @return A new [TtyInternalEventSource] instance.
         */
        internal fun fromFileDescriptor(inputFd: Int, closeOnDrop: Boolean = false): TtyInternalEventSource {
            return TtyInternalEventSource(inputFd, closeOnDrop)
        }
    }

    /**
     * Tries to read an [InternalEvent] within the given duration.
     *
     * This method uses poll() to wait for input on the TTY, SIGWINCH signal,
     * or wake pipe. Events are parsed from the input buffer and returned
     * one at a time.
     *
     * @param timeout The maximum duration to wait, or null to wait indefinitely.
     * @return The event if one is available, or null if the timeout elapsed.
     * @throws Exception if an error occurs while reading.
     */
    override fun tryRead(timeout: Duration?): InternalEvent? {
        val pollTimeout = PollTimeout.new(timeout)

        memScoped {
            val numFds = if (wakePipe != null) 3 else 2
            val fds = allocArray<pollfd>(numFds)

            // TTY input
            fds[0].fd = ttyFd
            fds[0].events = POLLIN.toShort()
            fds[0].revents = 0

            // SIGWINCH signal receiver
            fds[1].fd = winchSignalReceiverFd
            fds[1].events = POLLIN.toShort()
            fds[1].revents = 0

            // Wake pipe (if available)
            if (wakePipe != null) {
                fds[2].fd = wakePipe.receiverFd
                fds[2].events = POLLIN.toShort()
                fds[2].revents = 0
            }

            while (true) {
                val leftover = pollTimeout.leftover()
                if (leftover != null && leftover == Duration.ZERO) {
                    break
                }

                // Check if there are buffered events from the last read
                parser.next()?.let { return it }

                // Calculate poll timeout in milliseconds
                val timeoutMs = leftover?.inWholeMilliseconds?.toInt() ?: -1

                val pollResult = pollWrapper(fds, numFds, timeoutMs)

                when {
                    pollResult < 0 -> {
                        if (errno == EINTR) {
                            // Interrupted by signal, retry
                            continue
                        }
                        throw IllegalStateException("Poll failed: ${strerror(errno)?.toKString()}")
                    }
                    pollResult == 0 -> {
                        // Timeout elapsed
                        break
                    }
                }

                // Check TTY input
                if (fds[0].revents.toInt() and POLLIN != 0) {
                    while (true) {
                        val readCount = readComplete(ttyFd, ttyBuffer)
                        if (readCount > 0) {
                            parser.advance(ttyBuffer, readCount, readCount == TTY_BUFFER_SIZE)
                        }

                        parser.next()?.let { return it }

                        if (readCount == 0) {
                            break
                        }
                    }
                }

                // Check SIGWINCH signal
                if (fds[1].revents.toInt() and POLLIN != 0) {
                    // Drain the pipe
                    val drainBuf = ByteArray(1024)
                    while (readComplete(winchSignalReceiverFd, drainBuf) != 0) {
                        // Keep draining
                    }

                    // Get new terminal size and return resize event
                    val newSize = size()
                    return InternalEvent.EventWrapper(
                        Event.Resize(newSize.first, newSize.second)
                    )
                }

                // Check wake pipe
                if (wakePipe != null && fds[2].revents.toInt() and POLLIN != 0) {
                    // Drain the pipe
                    val drainBuf = ByteArray(1024)
                    while (readComplete(wakePipe.receiverFd, drainBuf) != 0) {
                        // Keep draining
                    }

                    // Return interrupted error to signal wake
                    throw TtyInterruptedException("Poll operation was woken up by Waker.wake")
                }
            }
        }

        return null
    }

    /**
     * Returns a [Waker] allowing to wake/force the [tryRead] method to return.
     *
     * @return A [Waker] instance for this event source, or null if waking is not supported.
     */
    override fun waker(): Waker? = wakePipe?.waker?.let { tw ->
        // Adapter from TtyWaker (event.Waker) to source.Waker
        object : Waker {
            override fun wake() = tw.wake()
        }
    }

    /**
     * Closes this event source, releasing any resources.
     */
    override fun close() {
        if (closeOnDrop) {
            close(ttyFd)
        }
        close(winchSignalReceiverFd)
        close(winchSenderFd)
        wakePipe?.let {
            close(it.receiverFd)
        }
    }
}

/**
 * Reads from a non-blocking file descriptor until the buffer is full or it would block.
 *
 * Similar to `std::io::Read::read_to_end`, except this function only fills
 * the given buffer and does not read beyond that.
 *
 * @param fd The file descriptor to read from.
 * @param buf The buffer to fill.
 * @return The number of bytes read.
 */
@OptIn(ExperimentalForeignApi::class)
private fun readComplete(fd: Int, buf: ByteArray): Int {
    while (true) {
        buf.usePinned { pinned ->
            val result = read(fd, pinned.addressOf(0), buf.size.toULong())

            when {
                result > 0 -> return result.toInt()
                result == 0L -> return 0
                else -> {
                    when (errno) {
                        EWOULDBLOCK, EAGAIN -> return 0
                        EINTR -> { /* Retry on interrupt */ }
                        else -> throw IllegalStateException("Read failed: ${strerror(errno)?.toKString()}")
                    }
                }
            }
        }
    }
}

/**
 * Gets the TTY file descriptor.
 *
 * Uses STDIN if it's a TTY, otherwise tries to open /dev/tty.
 *
 * @return A pair of (fd, closeOnDrop).
 * @throws IllegalStateException if no TTY is available.
 */
@OptIn(ExperimentalForeignApi::class)
private fun ttyFileDescriptor(): Pair<Int, Boolean> {
    return if (isatty(STDIN_FILENO) == 1) {
        Pair(STDIN_FILENO, false)
    } else {
        // Try to open /dev/tty
        val fd = open("/dev/tty", O_RDONLY or O_NONBLOCK)
        if (fd == -1) {
            throw IllegalStateException("Failed to open /dev/tty: ${strerror(errno)?.toKString()}")
        }
        Pair(fd, true)
    }
}

// Global storage for SIGWINCH sender fd (required for signal handler)
private var sigwinchSenderFd: Int = -1

/**
 * Registers a SIGWINCH signal handler that writes to the given file descriptor.
 *
 * Note: This approach follows the Rust crossterm pattern using signal_hook's pipe mechanism.
 * The signal handler writes a byte to wake up the poll() when SIGWINCH is received.
 *
 * @param senderFd The file descriptor to write to when SIGWINCH is received.
 */
@OptIn(ExperimentalForeignApi::class)
private fun registerSigwinchHandler(senderFd: Int) {
    // Store the sender fd in a global for the signal handler
    sigwinchSenderFd = senderFd

    // Set up signal handler
    signal(SIGWINCH, staticCFunction<Int, Unit> { _ ->
        // Write a byte to the sender fd to notify of resize
        // Note: This is async-signal-safe as write() is on the safe list
        val buf = ByteArray(1) { 0 }
        buf.usePinned { pinned ->
            write(sigwinchSenderFd, pinned.addressOf(0), 1u)
        }
    })
}

/**
 * Custom exception for interrupted poll operations in TTY event source.
 *
 * This exception is thrown when a poll operation is interrupted by
 * the waker, indicating that the caller should handle the interruption
 * appropriately (typically by returning null from tryRead).
 */
class TtyInterruptedException(message: String) : Exception(message)

// ============================================================================
// TTY Parser Implementation
// ============================================================================

/**
 * Parses terminal input bytes into [InternalEvent]s.
 *
 * This parser exists for two reasons:
 * - Mimic anes Parser interface
 * - Move the advancing, parsing logic out of the `tryRead` method
 *
 * The parser maintains a buffer for accumulating bytes that form an ANSI
 * escape sequence, and a queue of parsed events.
 */
private class TtyParser {
    // Buffer for a single ANSI escape sequence (256 bytes should be enough)
    private val buffer = mutableListOf<Byte>()

    // Queue of parsed events
    // TTY_BUFFER_SIZE / 8 = 128 events (assuming average sequence length of 8 bytes)
    private val internalEvents = ArrayDeque<InternalEvent>(128)

    /**
     * Advances the parser with new input bytes.
     *
     * @param inputBuffer The input buffer containing new bytes.
     * @param count The number of bytes to process.
     * @param more Whether more input is available (affects escape key handling).
     */
    fun advance(inputBuffer: ByteArray, count: Int, more: Boolean) {
        for (idx in 0 until count) {
            val byte = inputBuffer[idx]
            val moreAvailable = idx + 1 < count || more

            buffer.add(byte)

            val result = ttyParseEvent(buffer.toByteArray(), moreAvailable)
            when (result) {
                is TtyParseResult.Success -> {
                    internalEvents.addLast(result.event)
                    buffer.clear()
                }
                is TtyParseResult.Incomplete -> {
                    // Keep buffering for more input
                    if (buffer.size > 256) {
                        // Buffer too large, clear and continue
                        buffer.clear()
                    }
                }
                is TtyParseResult.Error -> {
                    // Couldn't parse, clear buffer
                    buffer.clear()
                }
            }
        }
    }

    /**
     * Returns the next parsed event, if available.
     *
     * @return The next event, or null if no events are available.
     */
    fun next(): InternalEvent? = internalEvents.removeFirstOrNull()
}

/**
 * Result of attempting to parse an event from a byte buffer.
 */
private sealed class TtyParseResult {
    data class Success(val event: InternalEvent) : TtyParseResult()
    data object Incomplete : TtyParseResult()
    data object Error : TtyParseResult()
}

// ============================================================================
// Event Parsing Functions
// ============================================================================

/**
 * Parses a terminal event from the given buffer.
 *
 * The parsing follows the standard return conventions:
 * - Returns Success with the event if successfully parsed
 * - Returns Incomplete if more bytes are needed
 * - Returns Error if the buffer cannot be parsed (buffer should be cleared)
 *
 * @param buffer The input buffer.
 * @param inputAvailable Whether more input is available.
 * @return The parse result.
 */
private fun ttyParseEvent(buffer: ByteArray, inputAvailable: Boolean): TtyParseResult {
    if (buffer.isEmpty()) {
        return TtyParseResult.Incomplete
    }

    return when (buffer[0]) {
        0x1B.toByte() -> { // ESC
            if (buffer.size == 1) {
                if (inputAvailable) {
                    // Possible escape sequence, wait for more
                    TtyParseResult.Incomplete
                } else {
                    // Just the Escape key
                    TtyParseResult.Success(keyEvent(KeyCode.Esc))
                }
            } else {
                when (buffer[1]) {
                    'O'.code.toByte() -> ttyParseEscO(buffer)
                    '['.code.toByte() -> ttyParseCsi(buffer)
                    0x1B.toByte() -> {
                        // ESC ESC -> just Escape
                        TtyParseResult.Success(keyEvent(KeyCode.Esc))
                    }
                    else -> {
                        // Alt + key
                        val innerResult = ttyParseEvent(buffer.sliceArray(1 until buffer.size), inputAvailable)
                        if (innerResult is TtyParseResult.Success) {
                            val event = innerResult.event
                            if (event is InternalEvent.EventWrapper && event.event is Event.Key) {
                                val altKeyEvent = event.event.keyEvent.copy(
                                    modifiers = event.event.keyEvent.modifiers + KeyModifiers.ALT
                                )
                                TtyParseResult.Success(InternalEvent.EventWrapper(Event.Key(altKeyEvent)))
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
        '\r'.code.toByte() -> {
            TtyParseResult.Success(keyEvent(KeyCode.Enter))
        }
        '\n'.code.toByte() -> {
            // In raw mode, \n is Ctrl+J. Outside raw mode, it's Enter.
            if (!isRawModeEnabled()) {
                TtyParseResult.Success(keyEvent(KeyCode.Enter))
            } else {
                // Ctrl+J
                TtyParseResult.Success(
                    InternalEvent.EventWrapper(
                        Event.Key(KeyEvent.new(KeyCode.Char('j'), KeyModifiers.CONTROL))
                    )
                )
            }
        }
        '\t'.code.toByte() -> {
            TtyParseResult.Success(keyEvent(KeyCode.Tab))
        }
        0x7F.toByte() -> { // DEL
            TtyParseResult.Success(keyEvent(KeyCode.Backspace))
        }
        in 0x01..0x1A -> {
            // Ctrl+A through Ctrl+Z
            val c = ('a'.code + (buffer[0] - 0x01)).toChar()
            TtyParseResult.Success(
                InternalEvent.EventWrapper(
                    Event.Key(KeyEvent.new(KeyCode.Char(c), KeyModifiers.CONTROL))
                )
            )
        }
        in 0x1C..0x1F -> {
            // Ctrl+4 through Ctrl+7
            val c = ('4'.code + (buffer[0] - 0x1C)).toChar()
            TtyParseResult.Success(
                InternalEvent.EventWrapper(
                    Event.Key(KeyEvent.new(KeyCode.Char(c), KeyModifiers.CONTROL))
                )
            )
        }
        0x00.toByte() -> {
            // Ctrl+Space
            TtyParseResult.Success(
                InternalEvent.EventWrapper(
                    Event.Key(KeyEvent.new(KeyCode.Char(' '), KeyModifiers.CONTROL))
                )
            )
        }
        else -> {
            // Try to parse as UTF-8 character
            ttyParseUtf8Char(buffer)
        }
    }
}

/**
 * Parses ESC O sequences (SS3 - Single Shift Three).
 */
private fun ttyParseEscO(buffer: ByteArray): TtyParseResult {
    if (buffer.size < 3) {
        return TtyParseResult.Incomplete
    }

    return when (buffer[2]) {
        'D'.code.toByte() -> TtyParseResult.Success(keyEvent(KeyCode.Left))
        'C'.code.toByte() -> TtyParseResult.Success(keyEvent(KeyCode.Right))
        'A'.code.toByte() -> TtyParseResult.Success(keyEvent(KeyCode.Up))
        'B'.code.toByte() -> TtyParseResult.Success(keyEvent(KeyCode.Down))
        'H'.code.toByte() -> TtyParseResult.Success(keyEvent(KeyCode.Home))
        'F'.code.toByte() -> TtyParseResult.Success(keyEvent(KeyCode.End))
        in 'P'.code.toByte()..'S'.code.toByte() -> {
            // F1-F4
            val fNum = (1 + buffer[2] - 'P'.code.toByte()).toUByte()
            TtyParseResult.Success(keyEvent(KeyCode.F(fNum)))
        }
        else -> TtyParseResult.Error
    }
}

/**
 * Parses CSI (Control Sequence Introducer) sequences.
 */
private fun ttyParseCsi(buffer: ByteArray): TtyParseResult {
    if (buffer.size < 3) {
        return TtyParseResult.Incomplete
    }

    return when (buffer[2]) {
        '['.code.toByte() -> {
            // CSI [ - Linux console F1-F5
            if (buffer.size < 4) {
                TtyParseResult.Incomplete
            } else {
                when (buffer[3]) {
                    in 'A'.code.toByte()..'E'.code.toByte() -> {
                        val fNum = (1 + buffer[3] - 'A'.code.toByte()).toUByte()
                        TtyParseResult.Success(keyEvent(KeyCode.F(fNum)))
                    }
                    else -> TtyParseResult.Error
                }
            }
        }
        'D'.code.toByte() -> TtyParseResult.Success(keyEvent(KeyCode.Left))
        'C'.code.toByte() -> TtyParseResult.Success(keyEvent(KeyCode.Right))
        'A'.code.toByte() -> TtyParseResult.Success(keyEvent(KeyCode.Up))
        'B'.code.toByte() -> TtyParseResult.Success(keyEvent(KeyCode.Down))
        'H'.code.toByte() -> TtyParseResult.Success(keyEvent(KeyCode.Home))
        'F'.code.toByte() -> TtyParseResult.Success(keyEvent(KeyCode.End))
        'Z'.code.toByte() -> {
            // Shift+Tab
            TtyParseResult.Success(
                InternalEvent.EventWrapper(
                    Event.Key(KeyEvent(KeyCode.BackTab, KeyModifiers.SHIFT, KeyEventKind.Press))
                )
            )
        }
        'M'.code.toByte() -> ttyParseCsiNormalMouse(buffer)
        '<'.code.toByte() -> ttyParseCsiSgrMouse(buffer)
        'I'.code.toByte() -> TtyParseResult.Success(InternalEvent.EventWrapper(Event.FocusGained))
        'O'.code.toByte() -> TtyParseResult.Success(InternalEvent.EventWrapper(Event.FocusLost))
        ';'.code.toByte() -> ttyParseCsiModifierKeyCode(buffer)
        'P'.code.toByte() -> TtyParseResult.Success(keyEvent(KeyCode.F(1u)))
        'Q'.code.toByte() -> TtyParseResult.Success(keyEvent(KeyCode.F(2u)))
        'S'.code.toByte() -> TtyParseResult.Success(keyEvent(KeyCode.F(4u)))
        '?'.code.toByte() -> {
            val lastByte = buffer.last()
            when (lastByte) {
                'u'.code.toByte() -> ttyParseCsiKeyboardEnhancementFlags(buffer)
                'c'.code.toByte() -> ttyParseCsiPrimaryDeviceAttributes(buffer)
                else -> TtyParseResult.Incomplete
            }
        }
        in '0'.code.toByte()..'9'.code.toByte() -> {
            // Numbered escape code
            if (buffer.size < 4) {
                TtyParseResult.Incomplete
            } else {
                val lastByte = buffer.last()
                // Final byte must be in range 64-126
                if (lastByte !in 64..126) {
                    TtyParseResult.Incomplete
                } else {
                    // Check for bracketed paste
                    if (buffer.size >= 6 && buffer.sliceArray(0..5).contentEquals(
                            byteArrayOf(0x1B, '['.code.toByte(), '2'.code.toByte(), '0'.code.toByte(), '0'.code.toByte(), '~'.code.toByte())
                        )) {
                        ttyParseCsiBracketedPaste(buffer)
                    } else {
                        when (lastByte.toInt().toChar()) {
                            'M' -> ttyParseCsiRxvtMouse(buffer)
                            '~' -> ttyParseCsiSpecialKeyCode(buffer)
                            'u' -> ttyParseCsiUEncodedKeyCode(buffer)
                            'R' -> ttyParseCsiCursorPosition(buffer)
                            else -> ttyParseCsiModifierKeyCode(buffer)
                        }
                    }
                }
            }
        }
        else -> TtyParseResult.Error
    }
}

/**
 * Creates a simple key event wrapper.
 */
private fun keyEvent(code: KeyCode): InternalEvent =
    InternalEvent.EventWrapper(Event.Key(KeyEvent.new(code)))

/**
 * Parses CSI cursor position response.
 * ESC [ Cy ; Cx R
 */
private fun ttyParseCsiCursorPosition(buffer: ByteArray): TtyParseResult {
    val str = buffer.decodeToString(2, buffer.size - 1)
    val parts = str.split(';')
    if (parts.size != 2) {
        return TtyParseResult.Error
    }
    val y = (parts[0].toIntOrNull() ?: return TtyParseResult.Error) - 1
    val x = (parts[1].toIntOrNull() ?: return TtyParseResult.Error) - 1
    return TtyParseResult.Success(InternalEvent.CursorPosition(x.toUShort(), y.toUShort()))
}

/**
 * Parses CSI keyboard enhancement flags.
 * ESC [ ? flags u
 */
private fun ttyParseCsiKeyboardEnhancementFlags(buffer: ByteArray): TtyParseResult {
    if (buffer.size < 5) {
        return TtyParseResult.Incomplete
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

    return TtyParseResult.Success(InternalEvent.KeyboardEnhancementFlagsEvent(flags))
}

/**
 * Parses CSI primary device attributes.
 */
@Suppress("UNUSED_PARAMETER")
private fun ttyParseCsiPrimaryDeviceAttributes(buffer: ByteArray): TtyParseResult {
    return TtyParseResult.Success(InternalEvent.PrimaryDeviceAttributes)
}

/**
 * Parses modifier mask to [KeyModifiers].
 */
private fun ttyParseModifiers(mask: Int): KeyModifiers {
    val m = (mask - 1).coerceAtLeast(0)
    var modifiers = KeyModifiers.NONE
    if (m and 1 != 0) modifiers = modifiers + KeyModifiers.SHIFT
    if (m and 2 != 0) modifiers = modifiers + KeyModifiers.ALT
    if (m and 4 != 0) modifiers = modifiers + KeyModifiers.CONTROL
    if (m and 8 != 0) modifiers = modifiers + KeyModifiers.SUPER
    if (m and 16 != 0) modifiers = modifiers + KeyModifiers.HYPER
    if (m and 32 != 0) modifiers = modifiers + KeyModifiers.META
    return modifiers
}

/**
 * Parses key event kind from code.
 */
private fun ttyParseKeyEventKind(kind: Int): KeyEventKind = when (kind) {
    1 -> KeyEventKind.Press
    2 -> KeyEventKind.Repeat
    3 -> KeyEventKind.Release
    else -> KeyEventKind.Press
}

/**
 * Parses CSI modifier key code sequences.
 */
private fun ttyParseCsiModifierKeyCode(buffer: ByteArray): TtyParseResult {
    val str = buffer.decodeToString(2, buffer.size - 1)
    val parts = str.split(';')

    var modifiers = KeyModifiers.NONE
    var kind = KeyEventKind.Press

    if (parts.size > 1) {
        val modParts = parts[1].split(':')
        val modMask = modParts[0].toIntOrNull() ?: 0
        modifiers = ttyParseModifiers(modMask)
        if (modParts.size > 1) {
            kind = ttyParseKeyEventKind(modParts[1].toIntOrNull() ?: 1)
        }
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
        else -> return TtyParseResult.Error
    }

    return TtyParseResult.Success(InternalEvent.EventWrapper(Event.Key(KeyEvent(keycode, modifiers, kind))))
}

/**
 * Parses CSI special key codes (tilde sequences).
 * ESC [ number ~
 */
private fun ttyParseCsiSpecialKeyCode(buffer: ByteArray): TtyParseResult {
    val str = buffer.decodeToString(2, buffer.size - 1)
    val parts = str.split(';')
    val keyNum = parts[0].toIntOrNull() ?: return TtyParseResult.Error

    var modifiers = KeyModifiers.NONE
    var kind = KeyEventKind.Press

    if (parts.size > 1) {
        val modParts = parts[1].split(':')
        modifiers = ttyParseModifiers(modParts[0].toIntOrNull() ?: 0)
        if (modParts.size > 1) {
            kind = ttyParseKeyEventKind(modParts[1].toIntOrNull() ?: 1)
        }
    }

    val keycode = when (keyNum) {
        1, 7 -> KeyCode.Home
        2 -> KeyCode.Insert
        3 -> KeyCode.Delete
        4, 8 -> KeyCode.End
        5 -> KeyCode.PageUp
        6 -> KeyCode.PageDown
        11, 12, 13, 14, 15 -> KeyCode.F((keyNum - 10).toUByte())
        17, 18, 19, 20, 21 -> KeyCode.F((keyNum - 11).toUByte())
        23, 24 -> KeyCode.F((keyNum - 12).toUByte())
        else -> return TtyParseResult.Error
    }

    return TtyParseResult.Success(InternalEvent.EventWrapper(Event.Key(KeyEvent(keycode, modifiers, kind))))
}

/**
 * Parses CSI u-encoded key codes (Kitty keyboard protocol).
 */
private fun ttyParseCsiUEncodedKeyCode(buffer: ByteArray): TtyParseResult {
    val str = buffer.decodeToString(2, buffer.size - 1)
    val parts = str.split(';')

    val codepoint = parts[0].toIntOrNull() ?: return TtyParseResult.Error

    var modifiers = KeyModifiers.NONE
    var kind = KeyEventKind.Press

    if (parts.size > 1) {
        val modParts = parts[1].split(':')
        modifiers = ttyParseModifiers(modParts[0].toIntOrNull() ?: 0)
        if (modParts.size > 1) {
            kind = ttyParseKeyEventKind(modParts[1].toIntOrNull() ?: 1)
        }
    }

    val keycode = ttyTranslateCodepoint(codepoint)

    return TtyParseResult.Success(InternalEvent.EventWrapper(Event.Key(KeyEvent(keycode, modifiers, kind))))
}

/**
 * Translates a Unicode codepoint to a [KeyCode].
 */
private fun ttyTranslateCodepoint(codepoint: Int): KeyCode = when (codepoint) {
    13 -> KeyCode.Enter
    9 -> KeyCode.Tab
    127 -> KeyCode.Backspace
    27 -> KeyCode.Esc
    57358 -> KeyCode.CapsLock
    57359 -> KeyCode.ScrollLock
    57360 -> KeyCode.NumLock
    57361 -> KeyCode.PrintScreen
    57362 -> KeyCode.Pause
    57363 -> KeyCode.Menu
    in 57376..57387 -> KeyCode.F((codepoint - 57376 + 13).toUByte()) // F13-F24
    in 57388..57397 -> KeyCode.F((codepoint - 57388 + 25).toUByte()) // F25-F34
    else -> KeyCode.Char(codepoint.toChar())
}

/**
 * Parses CSI normal mouse (X10 compatibility mode).
 * ESC [ M Cb Cx Cy
 */
private fun ttyParseCsiNormalMouse(buffer: ByteArray): TtyParseResult {
    if (buffer.size < 6) {
        return TtyParseResult.Incomplete
    }

    val cb = buffer[3].toInt() - 32
    val cx = buffer[4].toInt() - 32 - 1
    val cy = buffer[5].toInt() - 32 - 1

    val (kind, modifiers) = ttyParseCb(cb)

    return TtyParseResult.Success(
        InternalEvent.EventWrapper(
            Event.Mouse(MouseEvent(kind, cx.toUShort(), cy.toUShort(), modifiers))
        )
    )
}

/**
 * Parses CSI SGR mouse (extended mode).
 * ESC [ < Cb ; Cx ; Cy M/m
 */
private fun ttyParseCsiSgrMouse(buffer: ByteArray): TtyParseResult {
    val lastByte = buffer.last()
    if (lastByte != 'M'.code.toByte() && lastByte != 'm'.code.toByte()) {
        return TtyParseResult.Incomplete
    }

    val str = buffer.decodeToString(3, buffer.size - 1)
    val parts = str.split(';')
    if (parts.size != 3) {
        return TtyParseResult.Error
    }

    val cb = parts[0].toIntOrNull() ?: return TtyParseResult.Error
    val cx = (parts[1].toIntOrNull() ?: return TtyParseResult.Error) - 1
    val cy = (parts[2].toIntOrNull() ?: return TtyParseResult.Error) - 1

    val (kind, modifiers) = ttyParseCb(cb)

    // For SGR, 'm' means button release
    val finalKind = if (lastByte == 'm'.code.toByte()) {
        MouseEventKind.Up
    } else {
        kind
    }

    return TtyParseResult.Success(
        InternalEvent.EventWrapper(
            Event.Mouse(MouseEvent(finalKind, cx.toUShort(), cy.toUShort(), modifiers))
        )
    )
}

/**
 * Parses CSI rxvt mouse format.
 */
private fun ttyParseCsiRxvtMouse(buffer: ByteArray): TtyParseResult {
    val str = buffer.decodeToString(2, buffer.size - 1)
    val parts = str.split(';')
    if (parts.size != 3) {
        return TtyParseResult.Error
    }

    val cb = parts[0].toIntOrNull() ?: return TtyParseResult.Error
    val cx = (parts[1].toIntOrNull() ?: return TtyParseResult.Error) - 1
    val cy = (parts[2].toIntOrNull() ?: return TtyParseResult.Error) - 1

    val (kind, modifiers) = ttyParseCb(cb)

    return TtyParseResult.Success(
        InternalEvent.EventWrapper(
            Event.Mouse(MouseEvent(kind, cx.toUShort(), cy.toUShort(), modifiers))
        )
    )
}

/**
 * Parses the mouse button/event code byte.
 */
private fun ttyParseCb(cb: Int): Pair<MouseEventKind, KeyModifiers> {
    var modifiers = KeyModifiers.NONE
    if (cb and 4 != 0) modifiers = modifiers + KeyModifiers.SHIFT
    if (cb and 8 != 0) modifiers = modifiers + KeyModifiers.ALT
    if (cb and 16 != 0) modifiers = modifiers + KeyModifiers.CONTROL

    val kind = when {
        cb and 64 != 0 -> {
            // Scroll
            when (cb and 3) {
                0 -> MouseEventKind.ScrollUp()
                1 -> MouseEventKind.ScrollDown()
                2 -> MouseEventKind.ScrollLeft()
                3 -> MouseEventKind.ScrollRight()
                else -> MouseEventKind.ScrollUp()
            }
        }
        cb and 32 != 0 -> MouseEventKind.Drag
        cb and 3 == 3 -> MouseEventKind.Up
        else -> MouseEventKind.Down
    }

    return Pair(kind, modifiers)
}

/**
 * Parses CSI bracketed paste.
 * ESC [ 200 ~ ... ESC [ 201 ~
 */
private fun ttyParseCsiBracketedPaste(buffer: ByteArray): TtyParseResult {
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
        return TtyParseResult.Incomplete // Need more data
    }

    val content = buffer.decodeToString(6, endIdx)
    return TtyParseResult.Success(InternalEvent.EventWrapper(Event.Paste(content)))
}

/**
 * Parses a UTF-8 character from the buffer.
 */
private fun ttyParseUtf8Char(buffer: ByteArray): TtyParseResult {
    if (buffer.isEmpty()) return TtyParseResult.Incomplete

    val firstByte = buffer[0].toInt() and 0xFF

    val expectedLen = when {
        firstByte and 0x80 == 0 -> 1
        firstByte and 0xE0 == 0xC0 -> 2
        firstByte and 0xF0 == 0xE0 -> 3
        firstByte and 0xF8 == 0xF0 -> 4
        else -> return TtyParseResult.Error // Invalid UTF-8
    }

    if (buffer.size < expectedLen) {
        return TtyParseResult.Incomplete // Need more bytes
    }

    return try {
        val char = buffer.decodeToString(0, expectedLen).firstOrNull() ?: return TtyParseResult.Error
        val modifiers = if (char.isUpperCase()) KeyModifiers.SHIFT else KeyModifiers.NONE
        TtyParseResult.Success(
            InternalEvent.EventWrapper(
                Event.Key(KeyEvent.new(KeyCode.Char(char), modifiers))
            )
        )
    } catch (e: Exception) {
        TtyParseResult.Error
    }
}
