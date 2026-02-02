// Stub implementation for WasmJS - web shell integration not yet implemented
package io.github.kotlinmania.crossterm.terminal.sys

private fun notYetImplemented(): Nothing =
    throw NotImplementedError("Terminal operations for web shells are not yet implemented on WasmJS")

actual fun enableRawMode(): Unit = notYetImplemented()
actual fun disableRawMode(): Unit = notYetImplemented()
actual fun isRawModeEnabled(): Boolean = notYetImplemented()
actual fun size(): Pair<UShort, UShort> = notYetImplemented()
actual fun windowSize(): WindowSize = notYetImplemented()
actual fun supportsKeyboardEnhancement(): Boolean = notYetImplemented()
