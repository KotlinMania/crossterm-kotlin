// port-lint: source event/source/unix.rs
package io.github.kotlinmania.crossterm.event.source

import io.github.kotlinmania.crossterm.event.Event
import io.github.kotlinmania.crossterm.event.InternalEvent
import io.github.kotlinmania.crossterm.event.KeyCode
import io.github.kotlinmania.crossterm.event.KeyEvent

/**
 * Unix-specific event source module.
 *
 * This module provides the Unix implementation of [EventSource] for reading
 * terminal events. It corresponds to the Rust `event/source/unix.rs` module
 * which conditionally includes either the TTY-based or Mio-based implementation
 * depending on the `use-dev-tty` feature flag.
 *
 * In Kotlin, both implementations are available:
 * - `unix.mio` - Mio-based event source using poll() for I/O multiplexing
 * - `unix.tty` - TTY-based event source with Unix socket pairs for signals
 *
 * The default implementation uses the Mio-style approach as it's more portable
 * and doesn't require the `use-dev-tty` feature.
 *
 * ## Submodules
 *
 * - `unix.mio` - Mio-based event source using poll() for I/O multiplexing
 * - `unix.tty` - TTY-based event source with Unix socket pairs for signals
 *
 * ## Usage
 *
 * The event source is typically used internally by the event reading system.
 * Users should use the high-level event API instead:
 *
 * ```kotlin
 * import io.github.kotlinmania.crossterm.event.poll
 * import io.github.kotlinmania.crossterm.event.read
 *
 * // Poll for an event with timeout
 * if (poll(1.seconds)) {
 *     val event = read()
 *     // Handle the event
 * }
 * ```
 *
 * @see io.github.kotlinmania.crossterm.event.source.EventSource
 */

// Re-export the default Unix event source implementation
// In Rust, this is conditionally compiled based on the `use-dev-tty` feature.
// In Kotlin, we default to the Mio-based implementation which is more portable.
//
// When the unix.mio and unix.tty modules are fully implemented, this would be:
// typealias UnixInternalEventSource = io.github.kotlinmania.crossterm.event.source.unix.MioUnixInternalEventSource
//
// For now, we provide shared utilities used by both implementations.

/**
 * Buffer size for reading from the TTY.
 *
 * Based on testing on macOS/Linux, reading more than 1022 bytes at a time
 * is not common, so 1024 bytes is sufficient for the buffer.
 */
internal const val TTY_BUFFER_SIZE: Int = 1024

/**
 * Internal parser for ANSI escape sequences and terminal input.
 *
 * This parser exists to:
 * - Mimic the anes Parser interface from the original Rust implementation
 * - Move the advancing, parsing logic out of the main event reading method
 *
 * The parser maintains a buffer for the current ANSI escape sequence being
 * parsed and a queue of parsed internal events.
 *
 * This is shared between the Mio and TTY implementations.
 */
internal class Parser {
    /**
     * Buffer for the current ANSI escape sequence being parsed.
     * Sized at 256 bytes as that should be sufficient for any single
     * ANSI escape sequence.
     */
    private val buffer: MutableList<Byte> = ArrayList(256)

    /**
     * Queue of parsed internal events.
     * Sized at 128 entries based on TTY_BUFFER_SIZE / average sequence length (8 bytes).
     */
    private val internalEvents: ArrayDeque<InternalEvent> = ArrayDeque(128)

    /**
     * Advances the parser with new input bytes.
     *
     * Processes each byte, attempting to parse complete ANSI escape sequences
     * or individual characters as events. Successfully parsed events are
     * added to the internal event queue.
     *
     * @param data The bytes to process
     * @param more Whether more data may be available immediately
     */
    fun advance(data: ByteArray, more: Boolean) {
        for ((idx, byte) in data.withIndex()) {
            val moreAvailable = idx + 1 < data.size || more

            buffer.add(byte)

            val parseResult = parseEvent(buffer.toByteArray(), moreAvailable)
            when {
                parseResult != null -> {
                    internalEvents.addLast(parseResult)
                    buffer.clear()
                }
                // parseResult is null - could be incomplete sequence or error
                // If buffer is getting too long with no result, it's likely an error
                buffer.size > 64 -> {
                    // Event can't be parsed (not enough parameters, parameter is not a number, etc.)
                    // Clear the buffer and continue with another sequence
                    buffer.clear()
                }
                // Otherwise, keep the buffer and process next bytes
                // (incomplete sequence case)
            }
        }
    }

    /**
     * Returns the next parsed event, if any.
     *
     * @return The next internal event, or null if the queue is empty
     */
    fun next(): InternalEvent? = internalEvents.removeFirstOrNull()
}

/**
 * Parses input bytes into an internal event.
 *
 * This is a placeholder for the actual ANSI escape sequence parser.
 * The full implementation will be provided by the unix.parse module.
 *
 * @param buffer The bytes to parse
 * @param more Whether more data may be available
 * @return The parsed event, or null if parsing is incomplete or failed
 */
internal fun parseEvent(buffer: ByteArray, more: Boolean): InternalEvent? {
    // TODO: Delegate to io.github.kotlinmania.crossterm.event.sys.unix.parseEvent
    // For now, handle simple single-byte characters
    if (buffer.size == 1 && !more) {
        val byte = buffer[0]
        // Simple ASCII character handling
        if (byte in 32..126) {
            return InternalEvent.EventWrapper(
                Event.Key(KeyEvent(code = KeyCode.Char(byte.toInt().toChar())))
            )
        }
        // Handle Enter key
        if (byte == 13.toByte() || byte == 10.toByte()) {
            return InternalEvent.EventWrapper(
                Event.Key(KeyEvent(code = KeyCode.Enter))
            )
        }
        // Handle Escape key
        if (byte == 27.toByte() && !more) {
            return InternalEvent.EventWrapper(
                Event.Key(KeyEvent(code = KeyCode.Esc))
            )
        }
        // Handle Tab key
        if (byte == 9.toByte()) {
            return InternalEvent.EventWrapper(
                Event.Key(KeyEvent(code = KeyCode.Tab))
            )
        }
        // Handle Backspace
        if (byte == 127.toByte() || byte == 8.toByte()) {
            return InternalEvent.EventWrapper(
                Event.Key(KeyEvent(code = KeyCode.Backspace))
            )
        }
    }
    // More bytes expected or unrecognized sequence
    return null
}
