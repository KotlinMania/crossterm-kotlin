// port-lint: tests style/types/color.rs
package io.github.kotlinmania.crossterm.style.types

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for [Color].
 *
 * These tests correspond to the tests in the Rust style/types/color.rs module.
 */
class ColorTest {

    @Test
    fun testKnownColorConversion() {
        assertEquals(Color.Reset, Color.fromString("reset"))
        assertEquals(Color.Grey, Color.fromString("grey"))
        assertEquals(Color.DarkGrey, Color.fromString("dark_grey"))
        assertEquals(Color.Red, Color.fromString("red"))
        assertEquals(Color.DarkRed, Color.fromString("dark_red"))
        assertEquals(Color.Green, Color.fromString("green"))
        assertEquals(Color.DarkGreen, Color.fromString("dark_green"))
        assertEquals(Color.Yellow, Color.fromString("yellow"))
        assertEquals(Color.DarkYellow, Color.fromString("dark_yellow"))
        assertEquals(Color.Blue, Color.fromString("blue"))
        assertEquals(Color.DarkBlue, Color.fromString("dark_blue"))
        assertEquals(Color.Magenta, Color.fromString("magenta"))
        assertEquals(Color.DarkMagenta, Color.fromString("dark_magenta"))
        assertEquals(Color.Cyan, Color.fromString("cyan"))
        assertEquals(Color.DarkCyan, Color.fromString("dark_cyan"))
        assertEquals(Color.White, Color.fromString("white"))
        assertEquals(Color.Black, Color.fromString("black"))
    }

    @Test
    fun testUnknownColorConversionYieldsWhite() {
        assertEquals(Color.White, Color.fromString("foo"))
    }

    @Test
    fun testKnowRgbColorConversion() {
        assertEquals(
            Color.Rgb(0u, 0u, 0u),
            Color.Rgb(0u, 0u, 0u)
        )
        assertEquals(
            Color.Rgb(255u, 255u, 255u),
            Color.Rgb(255u, 255u, 255u)
        )
    }
}
