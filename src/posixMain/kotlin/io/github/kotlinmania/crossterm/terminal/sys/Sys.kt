// port-lint: source terminal/sys/unix.rs
@file:OptIn(kotlin.experimental.ExperimentalObjCRefinement::class, kotlinx.cinterop.ExperimentalForeignApi::class)

package io.github.kotlinmania.crossterm.terminal.sys

import kotlin.native.HiddenFromObjC

actual fun enableRawMode(): Unit = throw UnsupportedOperationException("Raw mode is not supported on this platform")

actual fun disableRawMode(): Unit = throw UnsupportedOperationException("Raw mode is not supported on this platform")

actual fun isRawModeEnabled(): Boolean = false

@HiddenFromObjC
actual fun size(): Pair<UShort, UShort> = throw UnsupportedOperationException("The terminal size could not be retrieved")

actual fun windowSize(): io.github.kotlinmania.crossterm.terminal.WindowSize =
    throw UnsupportedOperationException("The terminal window size could not be retrieved")

actual fun supportsKeyboardEnhancement(): Boolean = false
