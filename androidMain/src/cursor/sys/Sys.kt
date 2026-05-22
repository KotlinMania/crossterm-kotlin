// port-lint: source cursor/sys.rs
package io.github.kotlinmania.crossterm.cursor.sys

/**
 * Writes ANSI escape codes to stdout.
 * This can work in terminal emulator apps like Termux on Android.
 */
private fun writeAnsiCode(code: String) {
    print(code)
}

/**
 * Cursor position query is not supported on Android.
 * Reading cursor position requires stdin interaction which is not generally available.
 */
private fun unsupportedPosition(): Nothing =
    throw IllegalStateException("Cursor position query is not supported on Android targets")

actual fun position(): Pair<UShort, UShort> = unsupportedPosition()

actual fun moveTo(column: UShort, row: UShort) {
    // ANSI uses 1-indexed positions
    writeAnsiCode("\u001B[${row.toInt() + 1};${column.toInt() + 1}H")
}

actual fun moveUp(count: UShort) {
    writeAnsiCode("\u001B[${count}A")
}

actual fun moveDown(count: UShort) {
    writeAnsiCode("\u001B[${count}B")
}

actual fun moveRight(count: UShort) {
    writeAnsiCode("\u001B[${count}C")
}

actual fun moveLeft(count: UShort) {
    writeAnsiCode("\u001B[${count}D")
}

actual fun moveToColumn(column: UShort) {
    // ANSI uses 1-indexed positions
    writeAnsiCode("\u001B[${column.toInt() + 1}G")
}

actual fun moveToRow(row: UShort) {
    // ANSI uses 1-indexed positions
    writeAnsiCode("\u001B[${row.toInt() + 1}d")
}

actual fun moveToNextLine(count: UShort) {
    writeAnsiCode("\u001B[${count}E")
}

actual fun moveToPreviousLine(count: UShort) {
    writeAnsiCode("\u001B[${count}F")
}

actual fun savePosition() {
    writeAnsiCode("\u001B7")
}

actual fun restorePosition() {
    writeAnsiCode("\u001B8")
}

actual fun showCursor(visible: Boolean) {
    writeAnsiCode(if (visible) "\u001B[?25h" else "\u001B[?25l")
}
