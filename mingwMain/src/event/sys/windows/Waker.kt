// port-lint: source event/sys/windows/waker.rs
@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
package io.github.kotlinmania.crossterm.event.sys.windows

import kotlinx.atomicfu.locks.ReentrantLock
import kotlinx.atomicfu.locks.withLock
import kotlinx.cinterop.ExperimentalForeignApi
import platform.windows.CreateSemaphoreW
import platform.windows.ReleaseSemaphore

/**
 * Allows to wake up the `WinApiPoll.poll()` method.
 *
 * This waker uses a Windows semaphore to signal wake events. The semaphore
 * handle should be passed to WaitForMultipleObjects along with the console
 * input handle.
 */
class Waker private constructor(
    private var semaphore: Semaphore
) : io.github.kotlinmania.crossterm.event.sys.Waker {
    private val lock = ReentrantLock()

    companion object {
        /**
         * Creates a new waker.
         *
         * `Waker` is based on the `Semaphore`. You have to use the semaphore
         * handle along with the `WaitForMultipleObjects`.
         *
         * @return A new Waker instance
         * @throws IllegalStateException if semaphore creation fails
         */
        fun new(): Waker {
            return Waker(Semaphore.new())
        }
    }

    /**
     * Wakes the `WaitForMultipleObjects`.
     *
     * Releases the semaphore, causing any wait operation to return.
     *
     * @throws IllegalStateException if the semaphore cannot be released
     */
    override fun wake() {
        lock.withLock {
            semaphore.release()
        }
    }

    /**
     * Replaces the current semaphore with a new one allowing us to reuse the same `Waker`.
     *
     * This should be called after the waker has been triggered to prepare for
     * the next wake cycle.
     *
     * @throws IllegalStateException if a new semaphore cannot be created
     */
    fun reset() {
        lock.withLock {
            semaphore = Semaphore.new()
        }
    }

    /**
     * Returns the semaphore associated with the waker.
     *
     * @return A clone of the internal semaphore
     */
    fun semaphore(): Semaphore {
        return lock.withLock {
            semaphore.clone()
        }
    }

    /**
     * Returns the semaphore handle for use with WaitForMultipleObjects.
     */
    fun semaphoreHandle(): WindowsHandle {
        return lock.withLock {
            semaphore.handle()
        }
    }

    /**
     * Creates a clone of this waker (shares the same underlying semaphore).
     */
    fun clone(): Waker {
        return lock.withLock {
            Waker(semaphore.clone())
        }
    }
}

/**
 * Windows semaphore wrapper.
 *
 * Backed by WinAPI CreateSemaphore/ReleaseSemaphore for waking poll loops.
 */
class Semaphore private constructor(
    private val handle: WindowsHandle
) {
    companion object {
        /**
         * Creates a new semaphore with initial count 0 and max count 1.
         */
        @OptIn(ExperimentalForeignApi::class)
        fun new(): Semaphore {
            val handle = CreateSemaphoreW(
                null,
                0,
                1,
                null
            ) ?: throw IllegalStateException("Failed to create semaphore")
            return Semaphore(handle)
        }
    }

    /**
     * Releases the semaphore (increments the count).
     */
    @OptIn(ExperimentalForeignApi::class)
    fun release() {
        if (ReleaseSemaphore(handle, 1, null) == 0) {
            throw IllegalStateException("Failed to release semaphore")
        }
    }

    /**
     * Returns the underlying handle.
     */
    fun handle(): WindowsHandle = handle

    /**
     * Creates a clone of this semaphore (shares the same handle).
     */
    fun clone(): Semaphore = Semaphore(handle)
}
