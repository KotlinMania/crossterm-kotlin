// port-lint: tests style/types/colored.rs
package io.github.kotlinmania.crossterm.style.types

import kotlin.test.Test
import kotlin.test.assertEquals

class ColoredTest {

    private fun checkFormatColor(colored: Colored, expected: String) {
        Colored.setAnsiColorDisabled(true)
        assertEquals("", colored.toString())
        Colored.setAnsiColorDisabled(false)
        assertEquals(expected, colored.toString())
    }

    @Test
    fun testFormatFgColor() {
        val colored = Colored.ForegroundColor(Color.Red)
        checkFormatColor(colored, "38;5;9")
    }

    @Test
    fun testFormatBgColor() {
        val colored = Colored.BackgroundColor(Color.Red)
        checkFormatColor(colored, "48;5;9")
    }

    @Test
    fun testFormatResetFgColor() {
        val colored = Colored.ForegroundColor(Color.Reset)
        checkFormatColor(colored, "39")
    }

    @Test
    fun testFormatResetBgColor() {
        val colored = Colored.BackgroundColor(Color.Reset)
        checkFormatColor(colored, "49")
    }

    @Test
    fun testFormatBgRgbColor() {
        val colored = Colored.BackgroundColor(Color.Rgb(1u.toUByte(), 2u.toUByte(), 3u.toUByte()))
        checkFormatColor(colored, "48;2;1;2;3")
    }

    @Test
    fun testFormatFgAnsiColor() {
        val colored = Colored.ForegroundColor(Color.AnsiValue(255u.toUByte()))
        checkFormatColor(colored, "38;5;255")
    }

    @Test
    fun testParseAnsiFg() {
        testParseAnsi { Colored.ForegroundColor(it) }
    }

    @Test
    fun testParseAnsiBg() {
        testParseAnsi { Colored.BackgroundColor(it) }
    }

    private fun testParseAnsi(bgOrFg: (Color) -> Colored) {
        fun test(color: Color) {
            val colored = bgOrFg(color)
            assertEquals(colored, Colored.parseAnsi(colored.toString()))
        }

        test(Color.Reset)
        test(Color.Black)
        test(Color.DarkGrey)
        test(Color.Red)
        test(Color.DarkRed)
        test(Color.Green)
        test(Color.DarkGreen)
        test(Color.Yellow)
        test(Color.DarkYellow)
        test(Color.Blue)
        test(Color.DarkBlue)
        test(Color.Magenta)
        test(Color.DarkMagenta)
        test(Color.Cyan)
        test(Color.DarkCyan)
        test(Color.White)
        test(Color.Grey)

        for (n in 16..255) {
            test(Color.AnsiValue(n.toUByte()))
        }

        val gs = listOf(0, 2, 18, 19, 60, 100, 200, 250, 254, 255)
        val bs = listOf(0, 12, 16, 99, 100, 161, 200, 255)

        for (r in 0..255) {
            for (g in gs) {
                for (b in bs) {
                    test(Color.Rgb(r.toUByte(), g.toUByte(), b.toUByte()))
                }
            }
        }
    }

    @Test
    fun testParseInvalidAnsiColor() {
        fun test(s: String) {
            assertEquals(null, Colored.parseAnsi(s))
        }

        test("")
        test(";")
        test(";;")
        test("0")
        test("1")
        test("12")
        test("100")
        test("100048949345")
        test("39;")
        test("49;")
        test("39;2")
        test("49;2")
        test("38")
        test("38;")
        test("38;0")
        test("38;5")
        test("38;5;0;")
        test("38;5;0;2")
        test("38;5;80;")
        test("38;5;80;2")
        test("38;5;257")
        test("38;2")
        test("38;2;")
        test("38;2;0")
        test("38;2;0;2")
        test("38;2;0;2;257")
        test("38;2;0;2;25;")
        test("38;2;0;2;25;3")
        test("48")
        test("48;")
        test("48;0")
        test("48;5")
        test("48;5;0;")
        test("48;5;0;2")
        test("48;5;80;")
        test("48;5;80;2")
        test("48;5;257")
        test("48;2")
        test("48;2;")
        test("48;2;0")
        test("48;2;0;2")
        test("48;2;0;2;257")
        test("48;2;0;2;25;")
        test("48;2;0;2;25;3")
    }
}
