// port-lint: source terminal/sys/unix.rs
@file:OptIn(kotlin.experimental.ExperimentalObjCRefinement::class, kotlinx.cinterop.UnsafeNumber::class)

package io.github.kotlinmania.crossterm.terminal.sys

import io.github.kotlinmania.crossterm.terminal.WindowSize
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import platform.posix.TIOCGWINSZ
import platform.posix.ioctl
import platform.posix.winsize

/**
 * Apple-specific implementation to get window size via ioctl.
 */
@OptIn(ExperimentalForeignApi::class)
internal actual fun windowSizeViaIoctl(fd: Int): WindowSize {
    memScoped {
        val size = alloc<winsize>()
        
        if (ioctl(fd, TIOCGWINSZ.convert(), size.ptr) != 0) {
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
