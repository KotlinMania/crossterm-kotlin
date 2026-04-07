// port-lint: source cursor/sys.rs
package io.github.kotlinmania.crossterm.cursor.sys

private fun unsupported(): Nothing =
    throw IllegalStateException("Cursor operations are not supported in WasmJS targets")

actual fun position(): Pair<UShort, UShort> = unsupported()
actual fun moveTo(column: UShort, row: UShort): Unit = unsupported()
actual fun moveUp(count: UShort): Unit = unsupported()
actual fun moveDown(count: UShort): Unit = unsupported()
actual fun moveRight(count: UShort): Unit = unsupported()
actual fun moveLeft(count: UShort): Unit = unsupported()
actual fun moveToColumn(column: UShort): Unit = unsupported()
actual fun moveToRow(row: UShort): Unit = unsupported()
actual fun moveToNextLine(count: UShort): Unit = unsupported()
actual fun moveToPreviousLine(count: UShort): Unit = unsupported()
actual fun savePosition(): Unit = unsupported()
actual fun restorePosition(): Unit = unsupported()
actual fun showCursor(visible: Boolean): Unit = unsupported()
