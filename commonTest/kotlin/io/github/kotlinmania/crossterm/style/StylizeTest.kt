// port-lint: tests style/stylize.rs
package io.github.kotlinmania.crossterm.style

import io.github.kotlinmania.crossterm.style.types.Attribute
import io.github.kotlinmania.crossterm.style.types.Color
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StylizeTest {

    @Test
    fun setFgBgAddAttr() {
        val style = ContentStyle.new()
            .with(Color.Blue)
            .on(Color.Red)
            .attribute(Attribute.Bold)

        assertEquals(Color.Blue, style.foregroundColor)
        assertEquals(Color.Red, style.backgroundColor)
        assertTrue(style.attributes.has(Attribute.Bold))

        var styledContent = style.apply("test")

        styledContent = styledContent
            .with(Color.Green)
            .on(Color.Magenta)
            .attribute(Attribute.NoItalic)

        val finalStyle = styledContent.style()

        assertEquals(Color.Green, finalStyle.foregroundColor)
        assertEquals(Color.Magenta, finalStyle.backgroundColor)
        assertTrue(finalStyle.attributes.has(Attribute.Bold))
        assertTrue(finalStyle.attributes.has(Attribute.NoItalic))
    }
}
