// port-lint: source cursor/sys.rs
package io.github.kotlinmania.crossterm.cursor.sys

/**
 * This module provides platform-related cursor functions.
 *
 * The actual implementations are platform-specific:
 * - On Unix systems, cursor position is queried via ANSI escape sequences.
 * - On Windows, cursor manipulation uses the WinAPI console functions.
 *
 * ## Platform Behavior
 *
 * ### Unix
 * On Unix systems, [position] uses the `ESC [ 6 n` escape sequence to query the
 * cursor position from the terminal. This function will block and may time out
 * if event reading is in progress via [io.github.kotlinmania.crossterm.event.read] or
 * [io.github.kotlinmania.crossterm.event.poll].
 *
 * ### Windows
 * On Windows, cursor operations use the Windows Console API (WinAPI) directly
 * for operations like [position], [moveTo], [showCursor], etc.
 */

/**
 * Returns the cursor position as a pair of (column, row).
 *
 * The top left cell is represented as `(0, 0)`.
 *
 * ## Platform Notes
 *
 * On Unix systems, this function will block and possibly time out while
 * [io.github.kotlinmania.crossterm.event.read] or [io.github.kotlinmania.crossterm.event.poll]
 * are being called.
 *
 * On Windows, this function queries the console screen buffer directly via WinAPI.
 *
 * @return A pair containing (column, row) coordinates of the cursor.
 * @throws Exception if the cursor position cannot be determined.
 */
expect fun position(): Pair<UShort, UShort>

/**
 * Moves the cursor to the specified position.
 *
 * This function is primarily used on Windows where ANSI sequences may not be
 * fully supported. On Unix systems, ANSI escape sequences are typically used
 * directly.
 *
 * @param column The target column (0-indexed).
 * @param row The target row (0-indexed).
 * @throws Exception if the cursor cannot be moved.
 */
expect fun moveTo(column: UShort, row: UShort)

/**
 * Moves the cursor up by the specified number of rows.
 *
 * This is a Windows-specific function using WinAPI. On Unix, use ANSI sequences.
 *
 * @param count The number of rows to move up.
 * @throws Exception if the cursor cannot be moved.
 */
expect fun moveUp(count: UShort)

/**
 * Moves the cursor down by the specified number of rows.
 *
 * This is a Windows-specific function using WinAPI. On Unix, use ANSI sequences.
 *
 * @param count The number of rows to move down.
 * @throws Exception if the cursor cannot be moved.
 */
expect fun moveDown(count: UShort)

/**
 * Moves the cursor right by the specified number of columns.
 *
 * This is a Windows-specific function using WinAPI. On Unix, use ANSI sequences.
 *
 * @param count The number of columns to move right.
 * @throws Exception if the cursor cannot be moved.
 */
expect fun moveRight(count: UShort)

/**
 * Moves the cursor left by the specified number of columns.
 *
 * This is a Windows-specific function using WinAPI. On Unix, use ANSI sequences.
 *
 * @param count The number of columns to move left.
 * @throws Exception if the cursor cannot be moved.
 */
expect fun moveLeft(count: UShort)

/**
 * Moves the cursor to the specified column in the current row.
 *
 * This is a Windows-specific function using WinAPI. On Unix, use ANSI sequences.
 *
 * @param column The target column (0-indexed).
 * @throws Exception if the cursor cannot be moved.
 */
expect fun moveToColumn(column: UShort)

/**
 * Moves the cursor to the specified row in the current column.
 *
 * This is a Windows-specific function using WinAPI. On Unix, use ANSI sequences.
 *
 * @param row The target row (0-indexed).
 * @throws Exception if the cursor cannot be moved.
 */
expect fun moveToRow(row: UShort)

/**
 * Moves the cursor to the beginning of the line, [count] lines down.
 *
 * This is a Windows-specific function using WinAPI. On Unix, use ANSI sequences.
 *
 * @param count The number of lines to move down.
 * @throws Exception if the cursor cannot be moved.
 */
expect fun moveToNextLine(count: UShort)

/**
 * Moves the cursor to the beginning of the line, [count] lines up.
 *
 * This is a Windows-specific function using WinAPI. On Unix, use ANSI sequences.
 *
 * @param count The number of lines to move up.
 * @throws Exception if the cursor cannot be moved.
 */
expect fun moveToPreviousLine(count: UShort)

/**
 * Saves the current cursor position.
 *
 * The position can later be restored using [restorePosition].
 *
 * On Windows, this stores the position in a global atomic variable.
 * On Unix, ANSI escape sequences are typically used instead.
 *
 * @throws Exception if the position cannot be saved.
 */
expect fun savePosition()

/**
 * Restores a previously saved cursor position.
 *
 * The position must have been saved using [savePosition].
 *
 * @throws Exception if the position cannot be restored.
 */
expect fun restorePosition()

/**
 * Shows or hides the cursor.
 *
 * This is a Windows-specific function using WinAPI. On Unix, use ANSI sequences.
 *
 * @param visible Whether the cursor should be visible.
 * @throws Exception if the cursor visibility cannot be changed.
 */
expect fun showCursor(visible: Boolean)
