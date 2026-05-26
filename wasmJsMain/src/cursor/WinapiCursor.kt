package io.github.kotlinmania.crossterm.cursor

internal actual object WinapiCursor {
    actual fun moveTo(column: UShort, row: UShort) {}
    actual fun moveUp(count: UShort) {}
    actual fun moveDown(count: UShort) {}
    actual fun moveRight(count: UShort) {}
    actual fun moveLeft(count: UShort) {}
    actual fun moveToColumn(column: UShort) {}
    actual fun moveToRow(row: UShort) {}
    actual fun moveToNextLine(count: UShort) {}
    actual fun moveToPreviousLine(count: UShort) {}
    actual fun savePosition() {}
    actual fun restorePosition() {}
    actual fun showCursor(visible: Boolean) {}
}