// port-lint: source event/internal.rs
package io.github.kotlinmania.crossterm.event

import kotlinx.atomicfu.locks.ReentrantLock
import kotlinx.atomicfu.locks.withLock
import kotlin.time.Duration

/**
 * Global event reader singleton holder.
 *
 * This needs to be a singleton because there can only be one event reader
 * at a time reading from the terminal. The static mutex ensures thread-safe
 * access to the reader.
 *
 * This corresponds to the static `EVENT_READER` mutex in the Rust implementation.
 */
private object EventReaderHolder {
    private val lock = ReentrantLock()
    private var reader: InternalEventReader? = null

    /**
     * Executes a block with the locked event reader, initializing it if necessary.
     *
     * This is equivalent to `lock_event_reader()` in the Rust implementation.
     *
     * @param block The block to execute with the reader.
     * @return The result of the block.
     */
    fun <T> withReader(block: (InternalEventReader) -> T): T = lock.withLock {
        val r = reader ?: InternalEventReader.default().also { reader = it }
        block(r)
    }

    /**
     * Attempts to acquire the lock within the given duration and execute a block.
     *
     * This is equivalent to `try_lock_event_reader_for()` in the Rust implementation.
     *
     * Note: In the current implementation, this uses a simple tryLock without
     * timeout support. For full timeout support, a platform-specific implementation
     * would be needed (similar to parking_lot's `try_lock_for`).
     *
     * @param duration The maximum duration to wait for the lock.
     * @param block The block to execute with the reader.
     * @return The result of the block, or null if the lock could not be acquired.
     */
    fun <T> tryWithReader(duration: Duration, block: (InternalEventReader) -> T): T? {
        val acquired = lock.tryLock()
        return if (acquired) {
            try {
                val r = reader ?: InternalEventReader.default().also { reader = it }
                block(r)
            } finally {
                lock.unlock()
            }
        } else {
            null
        }
    }
}

/**
 * Acquires a lock on the global event reader.
 *
 * This function returns the locked event reader, initializing it if necessary.
 * The reader is protected by a global mutex to ensure thread-safe access.
 *
 * This is equivalent to `lock_event_reader()` in the Rust implementation.
 *
 * @param block The block to execute with the locked reader.
 * @return The result of the block.
 */
internal fun <T> lockEventReader(block: (InternalEventReader) -> T): T =
    EventReaderHolder.withReader(block)

/**
 * Attempts to acquire a lock on the global event reader within the given duration.
 *
 * This is equivalent to `try_lock_event_reader_for()` in the Rust implementation.
 *
 * @param duration The maximum duration to wait for the lock.
 * @param block The block to execute with the locked reader.
 * @return The result of the block, or null if the lock could not be acquired.
 */
internal fun <T> tryLockEventReaderFor(duration: Duration, block: (InternalEventReader) -> T): T? =
    EventReaderHolder.tryWithReader(duration, block)

/**
 * Polls to check if there are any [InternalEvent]s that can be read within the given duration.
 *
 * This function acquires the global event reader lock and delegates to the reader's poll method.
 * If a timeout is specified and the lock cannot be acquired within that duration, returns false.
 *
 * @param timeout The maximum duration to wait for an event, or null to wait indefinitely.
 * @param filter The filter to apply to events.
 * @return true if a matching event is available, false otherwise.
 */
internal fun poll(timeout: Duration?, filter: Filter): Boolean {
    return if (timeout != null) {
        val pollTimeout = PollTimeout.new(timeout)
        tryLockEventReaderFor(timeout) { reader ->
            reader.poll(pollTimeout.leftover(), filter)
        } ?: false
    } else {
        lockEventReader { reader ->
            reader.poll(null, filter)
        }
    }
}

/**
 * Reads a single [InternalEvent].
 *
 * This function blocks until an event matching the filter is available.
 * It acquires the global event reader lock and delegates to the reader's read method.
 *
 * @param filter The filter to apply to events.
 * @return The first event that matches the filter.
 */
internal fun read(filter: Filter): InternalEvent =
    lockEventReader { reader ->
        reader.read(filter)
    }

/**
 * Reads a single [InternalEvent]. Non-blocking.
 *
 * This function attempts to read an event without blocking. It acquires the global
 * event reader lock and delegates to the reader's tryRead method.
 *
 * @param filter The filter to apply to events.
 * @return The first matching event, or null if no matching event is available.
 */
internal fun tryRead(filter: Filter): InternalEvent? =
    lockEventReader { reader ->
        reader.tryRead(filter)
    }
