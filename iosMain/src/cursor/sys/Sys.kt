// Stub implementation for iOS - terminal operations not yet implemented
package io.github.kotlinmania.crossterm.cursor.sys

private fun notYetImplemented(): Nothing =
    throw NotImplementedError("Cursor operations for iOS terminal emulators are not yet implemented")

actual fun position(): Pair<UShort, UShort> = notYetImplemented()
actual fun moveTo(column: UShort, row: UShort): Unit = notYetImplemented()
actual fun moveUp(count: UShort): Unit = notYetImplemented()
actual fun moveDown(count: UShort): Unit = notYetImplemented()
actual fun moveRight(count: UShort): Unit = notYetImplemented()
actual fun moveLeft(count: UShort): Unit = notYetImplemented()
actual fun moveToColumn(column: UShort): Unit = notYetImplemented()
actual fun moveToRow(row: UShort): Unit = notYetImplemented()
actual fun moveToNextLine(count: UShort): Unit = notYetImplemented()
actual fun moveToPreviousLine(count: UShort): Unit = notYetImplemented()
actual fun savePosition(): Unit = notYetImplemented()
actual fun restorePosition(): Unit = notYetImplemented()
actual fun showCursor(visible: Boolean): Unit = notYetImplemented()
