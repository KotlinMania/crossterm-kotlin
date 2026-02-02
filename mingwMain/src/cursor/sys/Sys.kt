// port-lint: source cursor/sys/windows.rs
package io.github.kotlinmania.crossterm.cursor.sys

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
import platform.windows.SetConsoleCursorInfo
import platform.windows.SetConsoleCursorPosition
import platform.windows.STD_OUTPUT_HANDLE
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi

/**
 * Stores a saved cursor position for later restoration.
 */
@OptIn(ExperimentalAtomicApi::class)
private val savedPosition: AtomicReference<Pair<UShort, UShort>?> = AtomicReference(null)

/**
 * Returns the cursor position as a pair of (column, row).
 *
 * The top left cell is represented as `(0, 0)`.
 *
 * On Windows, this function queries the console screen buffer directly
 * using the Windows Console API.
 *
 * @return A pair containing (column, row) coordinates of the cursor.
 * @throws IllegalStateException if the cursor position cannot be determined.
 */
@OptIn(ExperimentalForeignApi::class)
actual fun position(): Pair<UShort, UShort> {
    memScoped {
        val handle = GetStdHandle(STD_OUTPUT_HANDLE.toUInt())
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

/**
 * Moves the cursor to the specified position.
 *
 * On Windows, this function uses SetConsoleCursorPosition to move the cursor.
 *
 * @param column The target column (0-indexed).
 * @param row The target row (0-indexed).
 * @throws IllegalStateException if the cursor cannot be moved.
 */
@OptIn(ExperimentalForeignApi::class)
actual fun moveTo(column: UShort, row: UShort) {
    memScoped {
        val handle = GetStdHandle(STD_OUTPUT_HANDLE.toUInt())
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

/**
 * Moves the cursor up by the specified number of rows.
 *
 * @param count The number of rows to move up.
 * @throws IllegalStateException if the cursor cannot be moved.
 */
actual fun moveUp(count: UShort) {
    val (col, row) = position()
    val newRow = if (row >= count) (row - count).toUShort() else 0u.toUShort()
    moveTo(col, newRow)
}

/**
 * Moves the cursor down by the specified number of rows.
 *
 * @param count The number of rows to move down.
 * @throws IllegalStateException if the cursor cannot be moved.
 */
actual fun moveDown(count: UShort) {
    val (col, row) = position()
    moveTo(col, (row + count).toUShort())
}

/**
 * Moves the cursor right by the specified number of columns.
 *
 * @param count The number of columns to move right.
 * @throws IllegalStateException if the cursor cannot be moved.
 */
actual fun moveRight(count: UShort) {
    val (col, row) = position()
    moveTo((col + count).toUShort(), row)
}

/**
 * Moves the cursor left by the specified number of columns.
 *
 * @param count The number of columns to move left.
 * @throws IllegalStateException if the cursor cannot be moved.
 */
actual fun moveLeft(count: UShort) {
    val (col, row) = position()
    val newCol = if (col >= count) (col - count).toUShort() else 0u.toUShort()
    moveTo(newCol, row)
}

/**
 * Moves the cursor to the specified column in the current row.
 *
 * @param column The target column (0-indexed).
 * @throws IllegalStateException if the cursor cannot be moved.
 */
actual fun moveToColumn(column: UShort) {
    val (_, row) = position()
    moveTo(column, row)
}

/**
 * Moves the cursor to the specified row in the current column.
 *
 * @param row The target row (0-indexed).
 * @throws IllegalStateException if the cursor cannot be moved.
 */
actual fun moveToRow(row: UShort) {
    val (col, _) = position()
    moveTo(col, row)
}

/**
 * Moves the cursor to the beginning of the line, [count] lines down.
 *
 * @param count The number of lines to move down.
 * @throws IllegalStateException if the cursor cannot be moved.
 */
actual fun moveToNextLine(count: UShort) {
    val (_, row) = position()
    moveTo(0u, (row + count).toUShort())
}

/**
 * Moves the cursor to the beginning of the line, [count] lines up.
 *
 * @param count The number of lines to move up.
 * @throws IllegalStateException if the cursor cannot be moved.
 */
actual fun moveToPreviousLine(count: UShort) {
    val (_, row) = position()
    val newRow = if (row >= count) (row - count).toUShort() else 0u.toUShort()
    moveTo(0u, newRow)
}

/**
 * Saves the current cursor position.
 *
 * The position can later be restored using [restorePosition].
 * On Windows, the position is stored in an atomic variable.
 *
 * @throws IllegalStateException if the position cannot be saved.
 */
@OptIn(ExperimentalAtomicApi::class)
actual fun savePosition() {
    savedPosition.store(position())
}

/**
 * Restores a previously saved cursor position.
 *
 * The position must have been saved using [savePosition].
 *
 * @throws IllegalStateException if no position was saved or restore fails.
 */
@OptIn(ExperimentalAtomicApi::class)
actual fun restorePosition() {
    val pos = savedPosition.load()
        ?: throw IllegalStateException("No cursor position was saved")
    moveTo(pos.first, pos.second)
}

/**
 * Shows or hides the cursor.
 *
 * On Windows, this uses SetConsoleCursorInfo to change cursor visibility.
 *
 * @param visible Whether the cursor should be visible.
 * @throws IllegalStateException if the cursor visibility cannot be changed.
 */
@OptIn(ExperimentalForeignApi::class)
actual fun showCursor(visible: Boolean) {
    memScoped {
        val handle = GetStdHandle(STD_OUTPUT_HANDLE.toUInt())
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
