// port-lint: tests style/types/colors.rs
package io.github.kotlinmania.crossterm.style.types

import kotlin.test.Test
import kotlin.test.assertEquals

class ColorsTest {

    @Test
    fun testColorsThen() {
        assertEquals(
            Colors(foreground = null, background = null).then(
                Colors(foreground = null, background = null)
            ),
            Colors(foreground = null, background = null),
        )

        assertEquals(
            Colors(foreground = null, background = null).then(
                Colors(foreground = Color.Black, background = null)
            ),
            Colors(foreground = Color.Black, background = null),
        )

        assertEquals(
            Colors(foreground = null, background = null).then(
                Colors(foreground = null, background = Color.Grey)
            ),
            Colors(foreground = null, background = Color.Grey),
        )

        assertEquals(
            Colors(foreground = null, background = null).then(
                Colors.new(Color.White, Color.Grey)
            ),
            Colors.new(Color.White, Color.Grey),
        )

        assertEquals(
            Colors(foreground = null, background = Color.Blue).then(
                Colors.new(Color.White, Color.Grey)
            ),
            Colors.new(Color.White, Color.Grey),
        )

        assertEquals(
            Colors(foreground = Color.Blue, background = null).then(
                Colors.new(Color.White, Color.Grey)
            ),
            Colors.new(Color.White, Color.Grey),
        )

        assertEquals(
            Colors.new(Color.Blue, Color.Green).then(
                Colors.new(Color.White, Color.Grey)
            ),
            Colors.new(Color.White, Color.Grey),
        )

        assertEquals(
            Colors(foreground = Color.Blue, background = Color.Green).then(
                Colors(foreground = null, background = Color.Grey)
            ),
            Colors(foreground = Color.Blue, background = Color.Grey),
        )

        assertEquals(
            Colors(foreground = Color.Blue, background = Color.Green).then(
                Colors(foreground = Color.White, background = null)
            ),
            Colors(foreground = Color.White, background = Color.Green),
        )

        assertEquals(
            Colors(foreground = Color.Blue, background = Color.Green).then(
                Colors(foreground = null, background = null)
            ),
            Colors(foreground = Color.Blue, background = Color.Green),
        )

        assertEquals(
            Colors(foreground = null, background = Color.Green).then(
                Colors(foreground = null, background = null)
            ),
            Colors(foreground = null, background = Color.Green),
        )

        assertEquals(
            Colors(foreground = Color.Blue, background = null).then(
                Colors(foreground = null, background = null)
            ),
            Colors(foreground = Color.Blue, background = null),
        )
    }
}

