// port-lint: source terminal/sys/unix.rs
package io.github.kotlinmania.crossterm.terminal.sys

import io.github.kotlinmania.crossterm.terminal.WindowSize
import kotlinx.cinterop.CFunction
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.invoke
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import platform.posix.STDIN_FILENO
import platform.posix.STDOUT_FILENO
import platform.posix.TIOCGWINSZ
import platform.posix.dlsym
import platform.posix.isatty
import platform.posix.winsize

import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi

// RTLD_DEFAULT is not available in platform.posix on all platforms.
// On most POSIX systems, RTLD_DEFAULT is defined as ((void*)0) or similar.
// Using null achieves the same effect for dlsym.
@OptIn(ExperimentalForeignApi::class)
private val RTLD_DEFAULT: kotlinx.cinterop.COpaquePointer? = null

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
    memScoped {
        val size = alloc<winsize>()
        val fd = getTtyFd()

        if (ioctlSymbol(fd, TIOCGWINSZ.convert(), size.ptr) != 0) {
            throw IllegalStateException("Failed to get window size")
        }

        return WindowSize(
            columns = size.ws_col,
            rows = size.ws_row,
            width = size.ws_xpixel,
            height = size.ws_ypixel
        )
    }
}

@OptIn(ExperimentalForeignApi::class)
private val ioctlSymbol by lazy {
    // Resolved lazily because terminal-size probing is not always used.
    dlsym(RTLD_DEFAULT, "ioctl")?.reinterpret<CFunction<(Int, Int, CPointer<winsize>?) -> Int>>()
        ?: error(
            "Failed to resolve ioctl symbol from POSIX C library on this platform; terminal size queries are unavailable"
        )
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
