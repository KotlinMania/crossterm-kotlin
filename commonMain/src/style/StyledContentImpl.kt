// port-lint: source style/styled_content.rs
package io.github.kotlinmania.crossterm.style

import io.github.kotlinmania.crossterm.Command

/**
 * A command that sets the underline color.
 *
 * Notes:
 * Commands must be executed/queued for execution otherwise they do nothing.
 *
 * Example:
 * ```kotlin
 * import io.github.kotlinmania.crossterm.style.SetUnderlineColor
 * import io.github.kotlinmania.crossterm.style.Color
 *
 * print(SetUnderlineColor(Color.Red).ansiString())
 * ```
 *
 * @param color The color to set for underlines.
 */
data class SetUnderlineColor(val color: Color) : Command {
    override fun writeAnsi(writer: Appendable) {
        writer.append("\u001B[")
        writer.append(colorToUnderlineAnsi(color))
        writer.append("m")
    }
}

/**
 * A command that sets multiple text attributes at once.
 *
 * Notes:
 * Commands must be executed/queued for execution otherwise they do nothing.
 *
 * Example:
 * ```kotlin
 * import io.github.kotlinmania.crossterm.style.SetAttributes
 * import io.github.kotlinmania.crossterm.style.Attributes
 * import io.github.kotlinmania.crossterm.style.Attribute
 *
 * val attrs = Attributes.of(Attribute.Bold, Attribute.Italic)
 * print(SetAttributes(attrs).ansiString())
 * ```
 *
 * @param attributes The attributes to set.
 */
data class SetAttributes(val attributes: Attributes) : Command {
    override fun writeAnsi(writer: Appendable) {
        for (attr in Attribute.entries) {
            if (attributes.has(attr)) {
                SetAttribute(attr).writeAnsi(writer)
            }
        }
    }
}

/**
 * A command that prints styled content.
 *
 * This command applies the style (colors and attributes) from a [StyledContent],
 * prints the content, and then resets the styling.
 *
 * See [StyledContent] for more info.
 *
 * Notes:
 * Commands must be executed/queued for execution otherwise they do nothing.
 *
 * Example:
 * ```kotlin
 * import io.github.kotlinmania.crossterm.style.PrintStyledContent
 * import io.github.kotlinmania.crossterm.style.StyledContent
 * import io.github.kotlinmania.crossterm.style.ContentStyle
 * import io.github.kotlinmania.crossterm.style.Color
 * import io.github.kotlinmania.crossterm.style.Attribute
 * import io.github.kotlinmania.crossterm.style.Attributes
 *
 * val style = ContentStyle(
 *     foregroundColor = Color.Yellow,
 *     backgroundColor = Color.Blue,
 *     attributes = Attributes.from(Attribute.Bold)
 * )
 * val styledContent = StyledContent(style, "Hello there")
 * print(PrintStyledContent(styledContent).ansiString())
 * ```
 *
 * @param D The type of the content to be styled.
 * @param styledContent The styled content to print.
 */
data class PrintStyledContent<D>(val styledContent: StyledContent<D>) : Command {
    override fun writeAnsi(writer: Appendable) {
        val style = styledContent.style()

        var resetBackground = false
        var resetForeground = false
        var reset = false

        style.backgroundColor?.let { bg ->
            SetBackgroundColor(bg).writeAnsi(writer)
            resetBackground = true
        }

        style.foregroundColor?.let { fg ->
            SetForegroundColor(fg).writeAnsi(writer)
            resetForeground = true
        }

        style.underlineColor?.let { ul ->
            SetUnderlineColor(ul).writeAnsi(writer)
            resetForeground = true
        }

        if (!style.attributes.isEmpty()) {
            SetAttributes(style.attributes).writeAnsi(writer)
            reset = true
        }

        writer.append(styledContent.content().toString())

        if (reset) {
            // NOTE: This will reset colors even though self has no colors, hence produce unexpected
            // resets.
            // TODO: reset the set attributes only.
            ResetColor.writeAnsi(writer)
        } else {
            // NOTE: Since the above bug, we do not need to reset colors when we reset attributes.
            if (resetBackground) {
                SetBackgroundColor(Color.Reset).writeAnsi(writer)
            }
            if (resetForeground) {
                SetForegroundColor(Color.Reset).writeAnsi(writer)
            }
        }
    }
}

/**
 * A command that sets a style (colors and attributes).
 *
 * Notes:
 * Commands must be executed/queued for execution otherwise they do nothing.
 *
 * Example:
 * ```kotlin
 * import io.github.kotlinmania.crossterm.style.SetStyle
 * import io.github.kotlinmania.crossterm.style.ContentStyle
 * import io.github.kotlinmania.crossterm.style.Color
 * import io.github.kotlinmania.crossterm.style.Attribute
 * import io.github.kotlinmania.crossterm.style.Attributes
 *
 * val style = ContentStyle(
 *     foregroundColor = Color.Yellow,
 *     backgroundColor = Color.Blue,
 *     attributes = Attributes.from(Attribute.Bold)
 * )
 * print(SetStyle(style).ansiString())
 * ```
 *
 * @param style The style to set.
 */
data class SetStyle(val style: ContentStyle) : Command {
    override fun writeAnsi(writer: Appendable) {
        style.backgroundColor?.let { bg ->
            SetBackgroundColor(bg).writeAnsi(writer)
        }

        style.foregroundColor?.let { fg ->
            SetForegroundColor(fg).writeAnsi(writer)
        }

        style.underlineColor?.let { ul ->
            SetUnderlineColor(ul).writeAnsi(writer)
        }

        if (!style.attributes.isEmpty()) {
            SetAttributes(style.attributes).writeAnsi(writer)
        }
    }
}

/**
 * Extension function to convert a [StyledContent] to its ANSI string representation.
 *
 * This applies the style, renders the content, and resets the styling.
 *
 * Example:
 * ```kotlin
 * import io.github.kotlinmania.crossterm.style.*
 *
 * val styled = "Hello there"
 *     .with(Color.Yellow)
 *     .on(Color.Blue)
 *     .attribute(Attribute.Bold)
 *
 * println(styled.toAnsiString())
 * ```
 *
 * @return The ANSI-escaped string representation of this styled content.
 */
fun <D> StyledContent<D>.toAnsiString(): String = PrintStyledContent(this).ansiString()

/**
 * Extension function to write the styled content as ANSI sequences to an [Appendable].
 *
 * Example:
 * ```kotlin
 * import io.github.kotlinmania.crossterm.style.*
 *
 * val styled = "Hello".bold().red()
 * val sb = StringBuilder()
 * styled.writeAnsi(sb)
 * print(sb)
 * ```
 *
 * @param writer The [Appendable] to write to.
 */
fun <D> StyledContent<D>.writeAnsi(writer: Appendable) {
    PrintStyledContent(this).writeAnsi(writer)
}

// Helper function for underline color conversion
private fun colorToUnderlineAnsi(color: Color): String = when (color) {
    Color.Reset -> "59"
    Color.Black -> "58;5;0"
    Color.DarkGrey -> "58;5;8"
    Color.Red -> "58;5;9"
    Color.DarkRed -> "58;5;1"
    Color.Green -> "58;5;10"
    Color.DarkGreen -> "58;5;2"
    Color.Yellow -> "58;5;11"
    Color.DarkYellow -> "58;5;3"
    Color.Blue -> "58;5;12"
    Color.DarkBlue -> "58;5;4"
    Color.Magenta -> "58;5;13"
    Color.DarkMagenta -> "58;5;5"
    Color.Cyan -> "58;5;14"
    Color.DarkCyan -> "58;5;6"
    Color.White -> "58;5;15"
    Color.Grey -> "58;5;7"
    is Color.Ansi -> "58;5;${color.value}"
    is Color.Rgb -> "58;2;${color.r};${color.g};${color.b}"
}
