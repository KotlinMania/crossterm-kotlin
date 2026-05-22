// port-lint: source terminal/sys.rs
package io.github.kotlinmania.crossterm.terminal.sys

import io.github.kotlinmania.crossterm.terminal.WindowSize

/**
 * External JS functions for Node.js terminal operations.
 */
@JsName("nodeStdinSetRawMode")
private external fun jsStdinSetRawMode(enabled: Boolean)

@JsName("nodeStdinResume")
private external fun jsStdinResume()

@JsName("nodeStdinIsRaw")
private external fun jsStdinIsRaw(): Boolean

@JsName("nodeStdoutColumns")
private external fun jsStdoutColumns(): Int

@JsName("nodeStdoutRows")
private external fun jsStdoutRows(): Int

actual fun enableRawMode() {
    jsStdinSetRawMode(true)
    try {
        jsStdinResume()
    } catch (_: Throwable) {
        // resume() not available in some environments
    }
}

actual fun disableRawMode() {
    jsStdinSetRawMode(false)
}

actual fun isRawModeEnabled(): Boolean {
    return try {
        jsStdinIsRaw()
    } catch (_: Throwable) {
        false
    }
}

actual fun size(): Pair<UShort, UShort> {
    val cols = jsStdoutColumns()
    val rows = jsStdoutRows()
    return Pair(cols.toUShort(), rows.toUShort())
}

actual fun windowSize(): WindowSize {
    val (columns, rows) = size()
    return WindowSize(
        rows = rows,
        columns = columns,
        width = 0u.toUShort(),
        height = 0u.toUShort()
    )
}

actual fun supportsKeyboardEnhancement(): Boolean = false
