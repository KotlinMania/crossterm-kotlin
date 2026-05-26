package io.github.kotlinmania.crossterm.terminal.sys

actual fun enableRawMode() {
    throw UnsupportedOperationException("Raw mode is not supported on JVM")
}

actual fun disableRawMode() {
    throw UnsupportedOperationException("Raw mode is not supported on JVM")
}

actual fun isRawModeEnabled(): Boolean = false

actual fun size(): Pair<UShort, UShort> {
    throw UnsupportedOperationException("The terminal size could not be retrieved")
}

actual fun windowSize(): io.github.kotlinmania.crossterm.terminal.WindowSize {
    throw UnsupportedOperationException("The terminal window size could not be retrieved")
}

actual fun supportsKeyboardEnhancement(): Boolean = false