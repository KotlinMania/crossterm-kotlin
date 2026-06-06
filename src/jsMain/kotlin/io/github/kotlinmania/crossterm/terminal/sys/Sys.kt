package io.github.kotlinmania.crossterm.terminal.sys

actual fun enableRawMode(): Unit = throw UnsupportedOperationException("Raw mode is not supported on JS")

actual fun disableRawMode(): Unit = throw UnsupportedOperationException("Raw mode is not supported on JS")

actual fun isRawModeEnabled(): Boolean = false

actual fun size(): Pair<UShort, UShort> = throw UnsupportedOperationException("The terminal size could not be retrieved")

actual fun windowSize(): io.github.kotlinmania.crossterm.terminal.WindowSize =
    throw UnsupportedOperationException("The terminal window size could not be retrieved")

actual fun supportsKeyboardEnhancement(): Boolean = false
