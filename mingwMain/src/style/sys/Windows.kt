// port-lint: source style/sys/windows.rs
package io.github.kotlinmania.crossterm.style.sys

import io.github.kotlinmania.crossterm.style.types.Color
import io.github.kotlinmania.crossterm.style.types.Colored
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import platform.windows.CONSOLE_SCREEN_BUFFER_INFO
import platform.windows.GetConsoleScreenBufferInfo
import platform.windows.GetStdHandle
import platform.windows.INVALID_HANDLE_VALUE
import platform.windows.SetConsoleTextAttribute
import platform.windows.STD_OUTPUT_HANDLE
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi

/**
 * Windows-specific style implementation using WinAPI console colors.
 *
 * This module provides functions to set foreground and background colors
 * on Windows consoles that don't support ANSI escape codes (Windows 7 and earlier).
 */

// Windows console color constants
private const val FG_BLUE: UShort = 0x0001u
private const val FG_GREEN: UShort = 0x0002u
private const val FG_RED: UShort = 0x0004u
private const val FG_INTENSITY: UShort = 0x0008u

private const val BG_BLUE: UShort = 0x0010u
private const val BG_GREEN: UShort = 0x0020u
private const val BG_RED: UShort = 0x0040u
private const val BG_INTENSITY: UShort = 0x0080u

/**
 * Stores the original console color. Uses Int.MAX_VALUE to indicate uninitialized.
 */
@OptIn(ExperimentalAtomicApi::class)
private val originalConsoleColor: AtomicInt = AtomicInt(Int.MAX_VALUE)

/**
 * Initializes the default console color. It will be skipped if it has already been initialized.
 */
@OptIn(ExperimentalAtomicApi::class)
fun initConsoleColor() {
    if (originalConsoleColor.load() == Int.MAX_VALUE) {
        val attrs = getCurrentConsoleAttributes()
        originalConsoleColor.compareAndSet(Int.MAX_VALUE, attrs.toInt())
    }
}

/**
 * Returns the original console color.
 *
 * @throws IllegalStateException if the original console color has not been initialized
 */
@OptIn(ExperimentalAtomicApi::class)
fun originalConsoleColor(): UShort {
    val value = originalConsoleColor.load()
    if (value == Int.MAX_VALUE) {
        throw IllegalStateException("Initial console color not set")
    }
    return value.toUShort()
}

/**
 * Sets the foreground color using Windows Console API.
 *
 * @param fgColor The foreground color to set
 * @throws IllegalStateException if the color cannot be set
 */
fun setForegroundColor(fgColor: Color) {
    initConsoleColor()

    val colorValue = coloredToU16(Colored.ForegroundColor(fgColor))
    val attrs = getCurrentConsoleAttributes()

    // Preserve background color bits (0x0070)
    val bgColor = attrs and 0x0070u
    var color = colorValue or bgColor

    // Preserve background intensity if set
    if ((attrs and BG_INTENSITY) != 0u.toUShort()) {
        color = color or BG_INTENSITY
    }

    setConsoleTextAttribute(color)
}

/**
 * Sets the background color using Windows Console API.
 *
 * @param bgColor The background color to set
 * @throws IllegalStateException if the color cannot be set
 */
fun setBackgroundColor(bgColor: Color) {
    initConsoleColor()

    val colorValue = coloredToU16(Colored.BackgroundColor(bgColor))
    val attrs = getCurrentConsoleAttributes()

    // Preserve foreground color bits (0x0007)
    val fgColor = attrs and 0x0007u
    var color = fgColor or colorValue

    // Preserve foreground intensity if set
    if ((attrs and FG_INTENSITY) != 0u.toUShort()) {
        color = color or FG_INTENSITY
    }

    setConsoleTextAttribute(color)
}

/**
 * Resets the console colors to the original values.
 */
@OptIn(ExperimentalAtomicApi::class)
fun reset() {
    val value = originalConsoleColor.load()
    if (value != Int.MAX_VALUE) {
        setConsoleTextAttribute(value.toUShort())
    }
}

/**
 * Converts a Colored value to a Windows console color attribute.
 */
fun coloredToU16(colored: Colored): UShort {
    return when (colored) {
        is Colored.ForegroundColor -> foregroundColorToU16(colored.color)
        is Colored.BackgroundColor -> backgroundColorToU16(colored.color)
        is Colored.UnderlineColor -> 0u // Underline color not supported in WinAPI
    }
}

private fun foregroundColorToU16(color: Color): UShort {
    return when (color) {
        Color.Black -> 0u
        Color.DarkGrey -> FG_INTENSITY
        Color.Red -> FG_INTENSITY or FG_RED
        Color.DarkRed -> FG_RED
        Color.Green -> FG_INTENSITY or FG_GREEN
        Color.DarkGreen -> FG_GREEN
        Color.Yellow -> FG_INTENSITY or FG_GREEN or FG_RED
        Color.DarkYellow -> FG_GREEN or FG_RED
        Color.Blue -> FG_INTENSITY or FG_BLUE
        Color.DarkBlue -> FG_BLUE
        Color.Magenta -> FG_INTENSITY or FG_RED or FG_BLUE
        Color.DarkMagenta -> FG_RED or FG_BLUE
        Color.Cyan -> FG_INTENSITY or FG_GREEN or FG_BLUE
        Color.DarkCyan -> FG_GREEN or FG_BLUE
        Color.White -> FG_INTENSITY or FG_RED or FG_GREEN or FG_BLUE
        Color.Grey -> FG_RED or FG_GREEN or FG_BLUE
        Color.Reset -> {
            val original = originalConsoleColor()
            val removeBgMask = BG_INTENSITY or BG_RED or BG_GREEN or BG_BLUE
            original and removeBgMask.inv()
        }
        // RGB and AnsiValue not supported in legacy WinAPI
        is Color.Rgb -> 0u
        is Color.AnsiValue -> 0u
    }
}

private fun backgroundColorToU16(color: Color): UShort {
    return when (color) {
        Color.Black -> 0u
        Color.DarkGrey -> BG_INTENSITY
        Color.Red -> BG_INTENSITY or BG_RED
        Color.DarkRed -> BG_RED
        Color.Green -> BG_INTENSITY or BG_GREEN
        Color.DarkGreen -> BG_GREEN
        Color.Yellow -> BG_INTENSITY or BG_GREEN or BG_RED
        Color.DarkYellow -> BG_GREEN or BG_RED
        Color.Blue -> BG_INTENSITY or BG_BLUE
        Color.DarkBlue -> BG_BLUE
        Color.Magenta -> BG_INTENSITY or BG_RED or BG_BLUE
        Color.DarkMagenta -> BG_RED or BG_BLUE
        Color.Cyan -> BG_INTENSITY or BG_GREEN or BG_BLUE
        Color.DarkCyan -> BG_GREEN or BG_BLUE
        Color.White -> BG_INTENSITY or BG_RED or BG_GREEN or BG_BLUE
        Color.Grey -> BG_RED or BG_GREEN or BG_BLUE
        Color.Reset -> {
            val original = originalConsoleColor()
            val removeFgMask = FG_INTENSITY or FG_RED or FG_GREEN or FG_BLUE
            original and removeFgMask.inv()
        }
        // RGB and AnsiValue not supported in legacy WinAPI
        is Color.Rgb -> 0u
        is Color.AnsiValue -> 0u
    }
}

/**
 * Gets the current console text attributes.
 */
@OptIn(ExperimentalForeignApi::class)
private fun getCurrentConsoleAttributes(): UShort {
    memScoped {
        val handle = GetStdHandle(STD_OUTPUT_HANDLE)
        if (handle == INVALID_HANDLE_VALUE) {
            throw IllegalStateException("Failed to get standard output handle")
        }

        val csbi = alloc<CONSOLE_SCREEN_BUFFER_INFO>()
        if (GetConsoleScreenBufferInfo(handle, csbi.ptr) == 0) {
            throw IllegalStateException("Failed to get console screen buffer info")
        }

        return csbi.wAttributes.toUShort()
    }
}

/**
 * Sets the console text attribute.
 */
@OptIn(ExperimentalForeignApi::class)
private fun setConsoleTextAttribute(attribute: UShort) {
    val handle = GetStdHandle(STD_OUTPUT_HANDLE)
    if (handle == INVALID_HANDLE_VALUE) {
        throw IllegalStateException("Failed to get standard output handle")
    }

    if (SetConsoleTextAttribute(handle, attribute) == 0) {
        throw IllegalStateException("Failed to set console text attribute")
    }
}
