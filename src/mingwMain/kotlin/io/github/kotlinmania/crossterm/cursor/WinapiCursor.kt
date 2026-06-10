// port-lint: source cursor/sys/windows.rs
// WinAPI implementations for cursor movement.
// On Windows pre-10 (or when VT processing fails), isAnsiCodeSupported()
// returns false and QueueableCommand.queue() calls executeWinapi() instead
// of writeAnsi(). These functions implement the WinAPI fallback path.

@file:OptIn(
    kotlinx.cinterop.ExperimentalForeignApi::class,
    kotlin.concurrent.atomics.ExperimentalAtomicApi::class,
    kotlin.experimental.ExperimentalObjCRefinement::class,
)

package io.github.kotlinmania.crossterm.cursor

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.readValue
import platform.windows.CONSOLE_CURSOR_INFO
import platform.windows.CONSOLE_SCREEN_BUFFER_INFO
import platform.windows.COORD
import platform.windows.GetConsoleCursorInfo
import platform.windows.GetConsoleScreenBufferInfo
import platform.windows.GetStdHandle
import platform.windows.INVALID_HANDLE_VALUE
import platform.windows.STD_OUTPUT_HANDLE
import platform.windows.SetConsoleCursorInfo
import platform.windows.SetConsoleCursorPosition
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi

@OptIn(ExperimentalAtomicApi::class)
private val savedPosition: AtomicReference<Pair<UShort, UShort>?> = AtomicReference(null)

@HiddenFromObjC
internal actual object WinapiCursor {
    @OptIn(ExperimentalForeignApi::class)
    actual fun moveTo(
        column: UShort,
        row: UShort,
    ) {
        memScoped {
            val handle = GetStdHandle(STD_OUTPUT_HANDLE)
            if (handle == INVALID_HANDLE_VALUE) {
                throw IllegalStateException("Failed to get standard output handle")
            }

            val coord = alloc<COORD>()
            coord.X = column.toShort()
            coord.Y = row.toShort()

            if (SetConsoleCursorPosition(handle, coord.readValue()) == 0) {
                throw IllegalStateException("Failed to set cursor position")
            }
        }
    }

    actual fun moveUp(count: UShort) {
        val (col, row) = position()
        val newRow = if (row >= count) (row - count).toUShort() else 0u.toUShort()
        moveTo(col, newRow)
    }

    actual fun moveDown(count: UShort) {
        val (col, row) = position()
        moveTo(col, (row + count).toUShort())
    }

    actual fun moveRight(count: UShort) {
        val (col, row) = position()
        moveTo((col + count).toUShort(), row)
    }

    actual fun moveLeft(count: UShort) {
        val (col, row) = position()
        val newCol = if (col >= count) (col - count).toUShort() else 0u.toUShort()
        moveTo(newCol, row)
    }

    actual fun moveToColumn(column: UShort) {
        val (_, row) = position()
        moveTo(column, row)
    }

    actual fun moveToRow(row: UShort) {
        val (col, _) = position()
        moveTo(col, row)
    }

    actual fun moveToNextLine(count: UShort) {
        val (_, row) = position()
        moveTo(0u, (row + count).toUShort())
    }

    actual fun moveToPreviousLine(count: UShort) {
        val (_, row) = position()
        val newRow = if (row >= count) (row - count).toUShort() else 0u.toUShort()
        moveTo(0u, newRow)
    }

    @OptIn(ExperimentalAtomicApi::class)
    actual fun savePosition() {
        savedPosition.store(position())
    }

    @OptIn(ExperimentalAtomicApi::class)
    actual fun restorePosition() {
        val pos =
            savedPosition.load()
                ?: throw IllegalStateException("No cursor position was saved")
        moveTo(pos.first, pos.second)
    }

    @OptIn(ExperimentalForeignApi::class)
    actual fun showCursor(visible: Boolean) {
        memScoped {
            val handle = GetStdHandle(STD_OUTPUT_HANDLE)
            if (handle == INVALID_HANDLE_VALUE) {
                throw IllegalStateException("Failed to get standard output handle")
            }

            val cursorInfo = alloc<CONSOLE_CURSOR_INFO>()
            if (GetConsoleCursorInfo(handle, cursorInfo.ptr) == 0) {
                throw IllegalStateException("Failed to get cursor info")
            }

            cursorInfo.bVisible = if (visible) 1 else 0
            if (SetConsoleCursorInfo(handle, cursorInfo.ptr) == 0) {
                throw IllegalStateException("Failed to set cursor visibility")
            }
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun position(): Pair<UShort, UShort> {
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
                csbi.dwCursorPosition.Y.toUShort(),
            )
        }
    }
}
