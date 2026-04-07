// port-lint: source terminal/sys.rs
package io.github.kotlinmania.crossterm.terminal.sys

import io.github.kotlinmania.crossterm.terminal.WindowSize

private fun unsupported(): Nothing =
    throw IllegalStateException("Terminal operations are not supported on Android targets")

actual fun enableRawMode(): Unit = unsupported()
actual fun disableRawMode(): Unit = unsupported()
actual fun isRawModeEnabled(): Boolean = unsupported()
actual fun size(): Pair<UShort, UShort> = unsupported()
actual fun windowSize(): WindowSize = unsupported()
actual fun supportsKeyboardEnhancement(): Boolean = false
