// Stub implementation for Android - terminal emulator integration not yet implemented
package io.github.kotlinmania.crossterm.terminal.sys

private fun notYetImplemented(): Nothing =
    throw NotImplementedError("Terminal operations for Android terminal emulators are not yet implemented")

actual fun enableRawMode(): Unit = notYetImplemented()
actual fun disableRawMode(): Unit = notYetImplemented()
actual fun isRawModeEnabled(): Boolean = notYetImplemented()
actual fun size(): Pair<UShort, UShort> = notYetImplemented()
actual fun windowSize(): WindowSize = notYetImplemented()
actual fun supportsKeyboardEnhancement(): Boolean = notYetImplemented()
