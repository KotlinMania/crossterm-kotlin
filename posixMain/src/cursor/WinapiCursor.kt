// No-op WinAPI cursor operations for POSIX (macOS, Linux, iOS, tvOS, watchOS).
// On these platforms, isAnsiCodeSupported() always returns true, so
// Command.executeWinapi() is never called. These actuals exist only to
// satisfy the expect/actual compilation contract.

@file:OptIn(kotlin.experimental.ExperimentalObjCRefinement::class)

package io.github.kotlinmania.crossterm.cursor

import kotlin.native.HiddenFromObjC

@HiddenFromObjC
internal actual object WinapiCursor {
    actual fun moveTo(column: UShort, row: UShort) { /* no-op: ANSI used on this platform */ }
    actual fun moveUp(count: UShort) {}
    actual fun moveDown(count: UShort) {}
    actual fun moveRight(count: UShort) {}
    actual fun moveLeft(count: UShort) {}
    actual fun moveToColumn(column: UShort) {}
    actual fun moveToRow(row: UShort) {}
    actual fun moveToNextLine(count: UShort) {}
    actual fun moveToPreviousLine(count: UShort) {}
    actual fun savePosition() {}
    actual fun restorePosition() {}
    actual fun showCursor(visible: Boolean) {}
}