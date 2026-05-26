// port-lint: source cursor/sys/windows.rs
@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class, kotlin.experimental.ExperimentalObjCRefinement::class)

package io.github.kotlinmania.crossterm.cursor.sys

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import platform.windows.CONSOLE_SCREEN_BUFFER_INFO
import platform.windows.GetConsoleScreenBufferInfo
import platform.windows.GetStdHandle
import platform.windows.INVALID_HANDLE_VALUE
import platform.windows.STD_OUTPUT_HANDLE
import kotlin.experimental.ExperimentalObjCRefinement
import kotlin.native.HiddenFromObjC

@OptIn(ExperimentalForeignApi::class)
@HiddenFromObjC
actual fun position(): Pair<UShort, UShort> {
    memScoped {
        val handle = GetStdHandle(STD_OUTPUT_HANDLE)
        if (handle == INVALID_HANDLE_VALUE) {
            throw IllegalStateException("Failed to get standard output handle")
        }

        val csbi = alloc<CONSOLE_SCREEN_BUFFER_INFO>()
        if (GetConsoleScreenBufferInfo(handle, csbi.ptr) == 0) {
            throw IllegalStateException("Failed to get console screen buffer info")
        }

        return Pair(
            csbi.dwCursorPosition.X.toUShort(),
            csbi.dwCursorPosition.Y.toUShort()
        )
    }
}