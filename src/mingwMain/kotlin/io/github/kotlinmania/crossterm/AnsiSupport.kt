// port-lint: source ansi_support.rs
package io.github.kotlinmania.crossterm

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.windows.DWORDVar
import platform.windows.ENABLE_VIRTUAL_TERMINAL_PROCESSING
import platform.windows.GetConsoleMode
import platform.windows.GetStdHandle
import platform.windows.INVALID_HANDLE_VALUE
import platform.windows.STD_OUTPUT_HANDLE
import platform.windows.SetConsoleMode

@OptIn(ExperimentalForeignApi::class)
internal actual fun enableVtProcessing(): Boolean {
    val handle = GetStdHandle(STD_OUTPUT_HANDLE)
    if (handle == INVALID_HANDLE_VALUE || handle == null) {
        return false
    }
    memScoped {
        val mode = alloc<DWORDVar>()
        if (GetConsoleMode(handle, mode.ptr) == 0) {
            return false
        }
        val newMode = mode.value or ENABLE_VIRTUAL_TERMINAL_PROCESSING.toUInt()
        if (SetConsoleMode(handle, newMode) != 0) {
            return true
        }
    }
    return false
}
