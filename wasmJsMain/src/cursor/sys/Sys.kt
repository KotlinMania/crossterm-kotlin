// port-lint: source cursor/sys.rs
package io.github.kotlinmania.crossterm.cursor.sys

/**
 * External JS function to write to stdout in Node.js.
 */
@JsName("nodeWriteStdout")
private external fun jsWriteStdout(str: String)

/**
 * Writes a string to stdout using Node.js via JS interop.
 * @throws Exception if not in a Node.js environment or write fails
 */
private fun writeToStdout(str: String) {
    jsWriteStdout(str)
}

/**
 * Cursor position query is not reliably supported on WASM/JS targets.
 * Position queries require reading from stdin, which is complex in async JS environments.
 */
private fun unsupportedPosition(): Nothing =
    throw IllegalStateException("Cursor position query is not supported on WASM/JS targets")

actual fun position(): Pair<UShort, UShort> = unsupportedPosition()

actual fun moveTo(column: UShort, row: UShort) {
    // ANSI uses 1-indexed positions
    writeToStdout("\u001B[${row.toInt() + 1};${column.toInt() + 1}H")
}

actual fun moveUp(count: UShort) {
    writeToStdout("\u001B[${count}A")
}

actual fun moveDown(count: UShort) {
    writeToStdout("\u001B[${count}B")
}

actual fun moveRight(count: UShort) {
    writeToStdout("\u001B[${count}C")
}

actual fun moveLeft(count: UShort) {
    writeToStdout("\u001B[${count}D")
}

actual fun moveToColumn(column: UShort) {
    // ANSI uses 1-indexed positions
    writeToStdout("\u001B[${column.toInt() + 1}G")
}

actual fun moveToRow(row: UShort) {
    // ANSI uses 1-indexed positions
    writeToStdout("\u001B[${row.toInt() + 1}d")
}

actual fun moveToNextLine(count: UShort) {
    writeToStdout("\u001B[${count}E")
}

actual fun moveToPreviousLine(count: UShort) {
    writeToStdout("\u001B[${count}F")
}

actual fun savePosition() {
    writeToStdout("\u001B7")
}

actual fun restorePosition() {
    writeToStdout("\u001B8")
}

actual fun showCursor(visible: Boolean) {
    writeToStdout(if (visible) "\u001B[?25h" else "\u001B[?25l")
}
