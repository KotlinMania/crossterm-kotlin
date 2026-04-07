// port-lint: source terminal/sys.rs
package io.github.kotlinmania.crossterm.terminal.sys

import io.github.kotlinmania.crossterm.terminal.WindowSize

private fun nodeProcessOrNull(): dynamic {
    return try {
        js("process")
    } catch (_: Throwable) {
        null
    }
}

private fun requireNodeStdin(): dynamic {
    val process = nodeProcessOrNull() ?: throw IllegalStateException("Raw mode requires a Node.js TTY environment")
    val stdin = process.stdin ?: throw IllegalStateException("process.stdin is not available")
    if (stdin.isTTY != true) {
        throw IllegalStateException("Raw mode requires a TTY stdin")
    }
    return stdin
}

private fun requireNodeStdout(): dynamic {
    val process = nodeProcessOrNull() ?: throw IllegalStateException("Terminal operations require a Node.js TTY environment")
    val stdout = process.stdout ?: throw IllegalStateException("process.stdout is not available")
    if (stdout.isTTY != true) {
        throw IllegalStateException("Terminal operations require a TTY stdout")
    }
    return stdout
}

private fun dynamicInt(value: dynamic): Int? {
    return when (value) {
        null -> null
        else -> (value as? Number)?.toInt()
    }
}

actual fun enableRawMode() {
    val stdin = requireNodeStdin()
    try {
        stdin.setRawMode(true)
    } catch (t: Throwable) {
        throw IllegalStateException("stdin.setRawMode(true) failed", t)
    }
    try {
        stdin.resume()
    } catch (_: Throwable) {
        // resume() not available in some environments
    }
}

actual fun disableRawMode() {
    val stdin = requireNodeStdin()
    try {
        stdin.setRawMode(false)
    } catch (t: Throwable) {
        throw IllegalStateException("stdin.setRawMode(false) failed", t)
    }
}

actual fun isRawModeEnabled(): Boolean {
    val process = nodeProcessOrNull() ?: return false
    val stdin = process.stdin ?: return false
    return stdin.isRaw == true
}

actual fun size(): Pair<UShort, UShort> {
    val stdout = requireNodeStdout()
    val cols = dynamicInt(stdout.columns) ?: throw IllegalStateException("stdout.columns is not available")
    val rows = dynamicInt(stdout.rows) ?: throw IllegalStateException("stdout.rows is not available")
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
