// WinAPI cursor operations for the Command.executeWinapi() fallback path.
//
// In Rust crossterm, the Command trait's execute_winapi() method delegates to
// cursor::sys::windows::* functions on Windows only. On all other platforms,
// is_ansi_code_supported() returns true and execute_winapi() is never called.
//
// This expect object mirrors that pattern: mingwMain provides real WinAPI
// implementations; all other platforms provide no-ops because ANSI is always
// used for cursor movement on those platforms.

@file:OptIn(kotlin.experimental.ExperimentalObjCRefinement::class)

package io.github.kotlinmania.crossterm.cursor

import kotlin.native.HiddenFromObjC

@HiddenFromObjC
internal expect object WinapiCursor {
    fun moveTo(
        column: UShort,
        row: UShort,
    )

    fun moveUp(count: UShort)

    fun moveDown(count: UShort)

    fun moveRight(count: UShort)

    fun moveLeft(count: UShort)

    fun moveToColumn(column: UShort)

    fun moveToRow(row: UShort)

    fun moveToNextLine(count: UShort)

    fun moveToPreviousLine(count: UShort)

    fun savePosition()

    fun restorePosition()

    fun showCursor(visible: Boolean)
}
