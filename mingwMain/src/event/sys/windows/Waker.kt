// port-lint: source event/sys/windows/waker.rs
package io.github.kotlinmania.crossterm.event.sys.windows

import kotlinx.atomicfu.locks.ReentrantLock
import kotlinx.atomicfu.locks.withLock

/**
 * Allows to wake up the `WinApiPoll.poll()` method.
 *
 * This waker uses a Windows semaphore to signal wake events. The semaphore
 * handle should be passed to WaitForMultipleObjects along with the console
 * input handle.
 */
class Waker private constructor(
    private var semaphore: Semaphore
) : io.github.kotlinmania.crossterm.event.source.Waker {
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
 * This is a placeholder that should be implemented using the Windows API
 * via Kotlin/Native (CreateSemaphore, ReleaseSemaphore, CloseHandle).
 */
class Semaphore private constructor(
    private val handle: WindowsHandle
) {
    companion object {
        /**
         * Creates a new semaphore with initial count 0 and max count 1.
         */
        fun new(): Semaphore {
            // TODO: Implement with CreateSemaphore(NULL, 0, 1, NULL)
            return Semaphore(0L)
        }
    }

    /**
     * Releases the semaphore (increments the count).
     */
    fun release() {
        // TODO: Implement with ReleaseSemaphore(handle, 1, NULL)
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
