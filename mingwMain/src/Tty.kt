// port-lint: source tty.rs
package io.github.kotlinmania.crossterm

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import platform.windows.DWORDVar
import platform.windows.GetConsoleMode
import platform.windows.GetStdHandle
import platform.windows.INVALID_HANDLE_VALUE
import platform.windows.STD_INPUT_HANDLE
import platform.windows.STD_OUTPUT_HANDLE
import platform.windows.STD_ERROR_HANDLE

/**
 * On Windows, we use GetConsoleMode to check if a handle is a console.
 *
 * The file descriptor is mapped to Windows standard handles:
 * - 0 -> STD_INPUT_HANDLE
 * - 1 -> STD_OUTPUT_HANDLE
 * - 2 -> STD_ERROR_HANDLE
 *
 * @param fd The file descriptor to check (0=stdin, 1=stdout, 2=stderr)
 * @return true if the handle is a console, false otherwise
 */
@OptIn(ExperimentalForeignApi::class)
actual fun isatty(fd: Int): Boolean {
    val stdHandle = when (fd) {
        0 -> STD_INPUT_HANDLE
        1 -> STD_OUTPUT_HANDLE
        2 -> STD_ERROR_HANDLE
        else -> return false
    }

    val handle = GetStdHandle(stdHandle.toUInt())
    if (handle == INVALID_HANDLE_VALUE || handle == null) {
        return false
    }

    return memScoped {
        val mode = alloc<DWORDVar>()
        GetConsoleMode(handle, mode.ptr) != 0
    }
}
