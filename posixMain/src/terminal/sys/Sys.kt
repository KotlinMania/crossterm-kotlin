// port-lint: source terminal/sys/unix.rs
package io.github.kotlinmania.crossterm.terminal.sys

import io.github.kotlinmania.crossterm.terminal.WindowSize
import kotlinx.cinterop.ExperimentalForeignApi
import platform.posix.STDIN_FILENO
import platform.posix.STDOUT_FILENO
import platform.posix.isatty

import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi

/**
 * Tracks whether raw mode is currently enabled.
 */
@OptIn(ExperimentalAtomicApi::class)
private val rawModeEnabled: AtomicBoolean = AtomicBoolean(false)

/**
 * Enables raw mode for the terminal.
 * Platform-specific implementation handles termios manipulation.
 */
@OptIn(ExperimentalAtomicApi::class)
actual fun enableRawMode() {
    if (!rawModeEnabled.compareAndSet(false, true)) {
        return
    }
    try {
        enableRawModeImpl()
    } catch (t: Throwable) {
        rawModeEnabled.store(false)
        throw t
    }
}

/**
 * Disables raw mode for the terminal.
 * Platform-specific implementation handles termios restoration.
 */
@OptIn(ExperimentalAtomicApi::class)
actual fun disableRawMode() {
    if (!rawModeEnabled.compareAndSet(true, false)) {
        return
    }
    try {
        disableRawModeImpl()
    } catch (t: Throwable) {
        rawModeEnabled.store(true)
        throw t
    }
}

/**
 * Checks whether raw mode is currently enabled.
 */
@OptIn(ExperimentalAtomicApi::class)
actual fun isRawModeEnabled(): Boolean {
    return rawModeEnabled.load()
}

/**
 * Returns the terminal size as a pair of (columns, rows).
 */
@OptIn(ExperimentalForeignApi::class)
actual fun size(): Pair<UShort, UShort> {
    val ws = try {
        windowSize()
    } catch (e: Exception) {
        throw IllegalStateException("Failed to determine terminal size", e)
    }
    return Pair(ws.columns, ws.rows)
}

/**
 * Returns the terminal window size including pixel dimensions.
 */
@OptIn(ExperimentalForeignApi::class)
actual fun windowSize(): WindowSize {
    val fd = getTtyFd()
    return windowSizeViaIoctl(fd)
}

/**
 * Queries the terminal's support for progressive keyboard enhancement.
 */
actual fun supportsKeyboardEnhancement(): Boolean {
    return false
}

@OptIn(ExperimentalForeignApi::class)
internal fun getTtyFd(): Int {
    return if (isatty(STDIN_FILENO) == 1) {
        STDIN_FILENO
    } else {
        STDOUT_FILENO
    }
}
