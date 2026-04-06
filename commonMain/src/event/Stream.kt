// port-lint: source event/stream.rs
package io.github.kotlinmania.crossterm.event

import io.github.kotlinmania.crossterm.event.sys.Waker
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.Duration

/**
 * A flow of terminal events.
 *
 * This is the Kotlin equivalent of Rust's `EventStream`, providing an asynchronous
 * stream of terminal events using Kotlin coroutines and Flow.
 *
 * The Rust implementation uses `futures_core::Stream` trait which is semantically
 * equivalent to Kotlin's `Flow`. This implementation provides the same functionality
 * using idiomatic Kotlin patterns.
 *
 * Example:
 * ```kotlin
 * import io.github.kotlinmania.crossterm.event.eventStream
 * import kotlinx.coroutines.flow.collect
 *
 * // Collect events from the stream
 * eventStream().collect { result ->
 *     result.onSuccess { event ->
 *         println("Received event: $event")
 *     }.onFailure { error ->
 *         println("Error: $error")
 *     }
 * }
 * ```
 *
 * @see Event for the types of events that can be received.
 */
class EventStream private constructor() {
    /**
     * The waker used to interrupt the internal poll operation.
     *
     * This is used when the stream is closed to wake up any blocking poll
     * and allow graceful shutdown.
     */
    private var pollInternalWaker: Waker? = null

    /**
     * Channel used to signal that the stream should be closed.
     *
     * When the channel is closed, the background polling coroutine will
     * stop and clean up resources.
     */
    private val closeSignal = Channel<Unit>(Channel.CONFLATED)

    /**
     * Whether the stream has been closed.
     */
    private val _isClosed = atomic(false)
    private var isClosed: Boolean
        get() = _isClosed.value
        set(value) { _isClosed.value = value }

    companion object {
        /**
         * Creates a new [EventStream] instance.
         *
         * @return A new [EventStream] ready to emit events.
         */
        fun new(): EventStream = EventStream()
    }

    /**
     * Returns this event stream as a [Flow] of [Result]<[Event]>.
     *
     * The flow will emit events as they become available from the terminal.
     * Each emission is wrapped in a [Result] to handle potential I/O errors.
     *
     * The flow is cold - collection only starts when a terminal operator
     * (like `collect`) is called. Multiple collectors will each receive
     * all events (each collector gets its own stream).
     *
     * The flow will continue emitting events until:
     * - The stream is closed via [close]
     * - An unrecoverable error occurs
     * - The collecting coroutine is cancelled
     *
     * @return A [Flow] that emits [Result]<[Event]> values.
     */
    fun asFlow(): Flow<Result<Event>> = callbackFlow {
        // Initialize the waker from the event reader
        pollInternalWaker = try {
            lockEventReader { reader -> reader.waker() }
        } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
            // Waker not available - that's ok, we just can't interrupt polls
            null
        }

        // Launch a background coroutine to poll for events
        // Note: Using Dispatchers.Default for cross-platform compatibility
        // On JVM/Android, Dispatchers.IO would be preferred for blocking I/O
        val pollingJob = launch(Dispatchers.Default) {
            while (isActive && !isClosed) {
                try {
                    // Poll with a short timeout to allow checking for cancellation
                    val hasEvent = poll(POLL_TIMEOUT, EventFilter)

                    if (hasEvent) {
                        // Read the event
                        val internalEvent = read(EventFilter)

                        // Extract the public event from the internal event wrapper
                        when (internalEvent) {
                            is InternalEvent.Event -> {
                                send(Result.success(internalEvent.event))
                            }
                            // Other internal events are not exposed publicly
                            else -> {
                                // Continue polling - this shouldn't happen with EventFilter
                                // but handle gracefully
                            }
                        }
                    }
                } catch (e: CancellationException) {
                    // Coroutine cancelled - exit gracefully
                    throw e
                } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
                    // Check if this is an interruption (used for waking)
                    if (e.message?.contains("interrupted", ignoreCase = true) == true ||
                        e::class.simpleName == "InterruptedException"
                    ) {
                        // Interrupted - check if we should continue or exit
                        if (isClosed) break
                        continue
                    }
                    // Emit the error and continue
                    send(Result.failure(e))
                }
            }
        }

        // Wait for close signal or channel close
        awaitClose {
            isClosed = true
            pollingJob.cancel()
            // Wake up any blocking poll operation
            try {
                pollInternalWaker?.wake()
            } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
                // Ignore wake errors during shutdown
            }
        }
    }

    /**
     * Closes this event stream.
     *
     * After calling this method, the flow returned by [asFlow] will complete.
     * Any blocking poll operations will be interrupted to allow graceful shutdown.
     *
     * This method is idempotent - calling it multiple times has no additional effect.
     */
    fun close() {
        if (!isClosed) {
            isClosed = true
            closeSignal.close()
            // Wake up any blocking poll to allow it to exit
            try {
                pollInternalWaker?.wake()
            } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
                // Ignore wake errors during shutdown
            }
        }
    }
}

/**
 * Default poll timeout for the event stream.
 *
 * This timeout is used to periodically check for cancellation while polling.
 * A shorter timeout means more responsive cancellation but slightly higher CPU usage.
 */
private val POLL_TIMEOUT = Duration.parse("PT0.1S") // 100ms

/**
 * Creates a new event stream as a [Flow] of terminal events.
 *
 * This is a convenience function that creates an [EventStream] and returns
 * its flow. For more control over the stream lifecycle, use [EventStream.new]
 * directly.
 *
 * Example:
 * ```kotlin
 * import io.github.kotlinmania.crossterm.event.eventStream
 * import kotlinx.coroutines.flow.collect
 * import kotlinx.coroutines.flow.take
 *
 * // Collect the first 10 events
 * eventStream().take(10).collect { result ->
 *     result.onSuccess { event ->
 *         println("Event: $event")
 *     }
 * }
 * ```
 *
 * @return A [Flow] that emits [Result]<[Event]> values.
 */
fun eventStream(): Flow<Result<Event>> = EventStream.new().asFlow()

/**
 * Reads events from the terminal as a flow, mapping successful events.
 *
 * This is a convenience function that filters out errors and only emits
 * successful events. Use [eventStream] if you need to handle errors.
 *
 * Example:
 * ```kotlin
 * import io.github.kotlinmania.crossterm.event.events
 * import kotlinx.coroutines.flow.collect
 *
 * // Collect events, ignoring errors
 * events().collect { event ->
 *     when (event) {
 *         is Event.Key -> println("Key: ${event.keyEvent}")
 *         is Event.Mouse -> println("Mouse: ${event.mouseEvent}")
 *         is Event.Resize -> println("Resize: ${event.columns}x${event.rows}")
 *         else -> {}
 *     }
 * }
 * ```
 *
 * @return A [Flow] that emits [Event] values, filtering out errors.
 */
suspend fun events(): Flow<Event> = withContext(Dispatchers.Default) {
    kotlinx.coroutines.flow.flow {
        eventStream().collect { result ->
            result.onSuccess { event ->
                emit(event)
            }
        }
    }
}
