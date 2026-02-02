// port-lint: source cursor/sys/unix.rs
package io.github.kotlinmania.crossterm.cursor.sys

import io.github.kotlinmania.crossterm.event.CursorPositionFilter
import io.github.kotlinmania.crossterm.event.InternalEvent
import io.github.kotlinmania.crossterm.event.poll
import io.github.kotlinmania.crossterm.event.read
import io.github.kotlinmania.crossterm.terminal.sys.disableRawMode
import io.github.kotlinmania.crossterm.terminal.sys.enableRawMode
import io.github.kotlinmania.crossterm.terminal.sys.isRawModeEnabled
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.posix.STDOUT_FILENO
import platform.posix.fflush
import platform.posix.write
import kotlin.time.Duration.Companion.milliseconds

/**
 * Writes a string to stdout using POSIX write.
 *
 * @param str The string to write.
 */
@OptIn(ExperimentalForeignApi::class)
private fun writeToStdout(str: String) {
    val bytes = str.encodeToByteArray()
    bytes.usePinned { pinned ->
        write(STDOUT_FILENO, pinned.addressOf(0), bytes.size.toULong())
    }
    fflush(null)
}

/**
 * Returns the cursor position (column, row).
 *
 * The top left cell is represented as `(0, 0)`.
 *
 * On Unix systems, this function will block and possibly time out while
 * [io.github.kotlinmania.crossterm.event.read] or [io.github.kotlinmania.crossterm.event.poll]
 * are being called.
 *
 * @return A pair containing (column, row) coordinates of the cursor.
 * @throws IllegalStateException if the cursor position cannot be determined.
 */
@OptIn(ExperimentalForeignApi::class)
actual fun position(): Pair<UShort, UShort> {
    return if (isRawModeEnabled()) {
        readPositionRaw()
    } else {
        readPosition()
    }
}

/**
 * Reads the cursor position when not in raw mode.
 * Temporarily enables raw mode to read the response.
 */
private fun readPosition(): Pair<UShort, UShort> {
    enableRawMode()
    try {
        return readPositionRaw()
    } finally {
        disableRawMode()
    }
}

/**
 * Reads the cursor position when already in raw mode.
 * Uses the `ESC [ 6 n` escape sequence to query the cursor position.
 */
private fun readPositionRaw(): Pair<UShort, UShort> {
    // Send the cursor position query: ESC [ 6 n
    writeToStdout("\u001B[6n")

    // Poll for the response with a timeout
    val timeout = 2000.milliseconds

    while (true) {
        when {
            poll(timeout, CursorPositionFilter) -> {
                val event = read(CursorPositionFilter)
                if (event is InternalEvent.CursorPosition) {
                    return Pair(event.column, event.row)
                }
            }
            else -> {
                throw IllegalStateException(
                    "The cursor position could not be read within a normal duration"
                )
            }
        }
    }
}

/**
 * Moves the cursor to the specified position.
 *
 * On Unix, this outputs the ANSI escape sequence directly.
 *
 * @param column The target column (0-indexed).
 * @param row The target row (0-indexed).
 */
actual fun moveTo(column: UShort, row: UShort) {
    // ANSI uses 1-indexed positions
    writeToStdout("\u001B[${row.toInt() + 1};${column.toInt() + 1}H")
}

/**
 * Moves the cursor up by the specified number of rows.
 *
 * @param count The number of rows to move up.
 */
actual fun moveUp(count: UShort) {
    writeToStdout("\u001B[${count}A")
}

/**
 * Moves the cursor down by the specified number of rows.
 *
 * @param count The number of rows to move down.
 */
actual fun moveDown(count: UShort) {
    writeToStdout("\u001B[${count}B")
}

/**
 * Moves the cursor right by the specified number of columns.
 *
 * @param count The number of columns to move right.
 */
actual fun moveRight(count: UShort) {
    writeToStdout("\u001B[${count}C")
}

/**
 * Moves the cursor left by the specified number of columns.
 *
 * @param count The number of columns to move left.
 */
actual fun moveLeft(count: UShort) {
    writeToStdout("\u001B[${count}D")
}

/**
 * Moves the cursor to the specified column in the current row.
 *
 * @param column The target column (0-indexed).
 */
actual fun moveToColumn(column: UShort) {
    // ANSI uses 1-indexed positions
    writeToStdout("\u001B[${column.toInt() + 1}G")
}

/**
 * Moves the cursor to the specified row in the current column.
 *
 * @param row The target row (0-indexed).
 */
actual fun moveToRow(row: UShort) {
    // ANSI uses 1-indexed positions
    writeToStdout("\u001B[${row.toInt() + 1}d")
}

/**
 * Moves the cursor to the beginning of the line, [count] lines down.
 *
 * @param count The number of lines to move down.
 */
actual fun moveToNextLine(count: UShort) {
    writeToStdout("\u001B[${count}E")
}

/**
 * Moves the cursor to the beginning of the line, [count] lines up.
 *
 * @param count The number of lines to move up.
 */
actual fun moveToPreviousLine(count: UShort) {
    writeToStdout("\u001B[${count}F")
}

/**
 * Saves the current cursor position.
 *
 * The position can later be restored using [restorePosition].
 * Uses the ANSI escape sequence ESC 7 (DECSC).
 */
actual fun savePosition() {
    writeToStdout("\u001B7")
}

/**
 * Restores a previously saved cursor position.
 *
 * The position must have been saved using [savePosition].
 * Uses the ANSI escape sequence ESC 8 (DECRC).
 */
actual fun restorePosition() {
    writeToStdout("\u001B8")
}

/**
 * Shows or hides the cursor.
 *
 * @param visible Whether the cursor should be visible.
 */
actual fun showCursor(visible: Boolean) {
    writeToStdout(if (visible) "\u001B[?25h" else "\u001B[?25l")
}
