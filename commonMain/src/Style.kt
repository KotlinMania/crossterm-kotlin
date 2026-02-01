// port-lint: source style.rs
package io.github.kotlinmania.crossterm.style

import io.github.kotlinmania.crossterm.Command

/**
 * Represents a color.
 */
sealed class Color {
    /** Resets the color to the default */
    data object Reset : Color()
    /** Black color */
    data object Black : Color()
    /** Dark grey color */
    data object DarkGrey : Color()
    /** Red color */
    data object Red : Color()
    /** Dark red color */
    data object DarkRed : Color()
    /** Green color */
    data object Green : Color()
    /** Dark green color */
    data object DarkGreen : Color()
    /** Yellow color */
    data object Yellow : Color()
    /** Dark yellow color */
    data object DarkYellow : Color()
    /** Blue color */
    data object Blue : Color()
    /** Dark blue color */
    data object DarkBlue : Color()
    /** Magenta color */
    data object Magenta : Color()
    /** Dark magenta color */
    data object DarkMagenta : Color()
    /** Cyan color */
    data object Cyan : Color()
    /** Dark cyan color */
    data object DarkCyan : Color()
    /** White color */
    data object White : Color()
    /** Grey color */
    data object Grey : Color()

    /** An ANSI color (0-255) */
    data class Ansi(val value: UByte) : Color()

    /** An RGB color */
    data class Rgb(val r: UByte, val g: UByte, val b: UByte) : Color()
}

/**
 * A command that sets the foreground color.
 */
data class SetForegroundColor(val color: Color) : Command {
    override fun writeAnsi(writer: Appendable) {
        writer.append(colorToForegroundAnsi(color))
    }
}

/**
 * A command that sets the background color.
 */
data class SetBackgroundColor(val color: Color) : Command {
    override fun writeAnsi(writer: Appendable) {
        writer.append(colorToBackgroundAnsi(color))
    }
}

/**
 * A command that sets both foreground and background colors.
 */
data class SetColors(val foreground: Color, val background: Color) : Command {
    override fun writeAnsi(writer: Appendable) {
        writer.append(colorToForegroundAnsi(foreground))
        writer.append(colorToBackgroundAnsi(background))
    }
}

/**
 * A command that resets all colors to default.
 */
data object ResetColor : Command {
    override fun writeAnsi(writer: Appendable) {
        writer.append("\u001B[0m")
    }
}

/**
 * A command that prints styled content.
 */
data class Print(val content: String) : Command {
    override fun writeAnsi(writer: Appendable) {
        writer.append(content)
    }
}

/**
 * Text attributes.
 */
enum class Attribute {
    /** Resets all attributes */
    Reset,
    /** Bold text */
    Bold,
    /** Dim text */
    Dim,
    /** Italic text */
    Italic,
    /** Underlined text */
    Underlined,
    /** Double underlined text */
    DoubleUnderlined,
    /** Undercurl (curly underline) */
    Undercurled,
    /** Dotted underline */
    Underdotted,
    /** Dashed underline */
    Underdashed,
    /** Slow blink */
    SlowBlink,
    /** Rapid blink */
    RapidBlink,
    /** Reverse video (swap foreground/background) */
    Reverse,
    /** Hidden text */
    Hidden,
    /** Crossed out text */
    CrossedOut,
    /** Fraktur (rarely supported) */
    Fraktur,
    /** No bold */
    NoBold,
    /** Normal intensity */
    NormalIntensity,
    /** No italic */
    NoItalic,
    /** No underline */
    NoUnderline,
    /** No blink */
    NoBlink,
    /** No reverse */
    NoReverse,
    /** No hidden */
    NoHidden,
    /** Not crossed out */
    NotCrossedOut,
    /** Framed */
    Framed,
    /** Encircled */
    Encircled,
    /** Overlined */
    OverLined,
    /** Not framed or encircled */
    NotFramedOrEncircled,
    /** Not overlined */
    NotOverLined
}

/**
 * A command that sets a text attribute.
 */
data class SetAttribute(val attribute: Attribute) : Command {
    override fun writeAnsi(writer: Appendable) {
        val code = when (attribute) {
            Attribute.Reset -> 0
            Attribute.Bold -> 1
            Attribute.Dim -> 2
            Attribute.Italic -> 3
            Attribute.Underlined -> 4
            Attribute.DoubleUnderlined -> 21
            Attribute.Undercurled -> 4 // Same as underlined in basic ANSI
            Attribute.Underdotted -> 4
            Attribute.Underdashed -> 4
            Attribute.SlowBlink -> 5
            Attribute.RapidBlink -> 6
            Attribute.Reverse -> 7
            Attribute.Hidden -> 8
            Attribute.CrossedOut -> 9
            Attribute.Fraktur -> 20
            Attribute.NoBold -> 21
            Attribute.NormalIntensity -> 22
            Attribute.NoItalic -> 23
            Attribute.NoUnderline -> 24
            Attribute.NoBlink -> 25
            Attribute.NoReverse -> 27
            Attribute.NoHidden -> 28
            Attribute.NotCrossedOut -> 29
            Attribute.Framed -> 51
            Attribute.Encircled -> 52
            Attribute.OverLined -> 53
            Attribute.NotFramedOrEncircled -> 54
            Attribute.NotOverLined -> 55
        }
        writer.append("\u001B[${code}m")
    }
}

// Helper functions for color conversion
private fun colorToForegroundAnsi(color: Color): String = when (color) {
    Color.Reset -> "\u001B[39m"
    Color.Black -> "\u001B[30m"
    Color.DarkGrey -> "\u001B[90m"
    Color.Red -> "\u001B[91m"
    Color.DarkRed -> "\u001B[31m"
    Color.Green -> "\u001B[92m"
    Color.DarkGreen -> "\u001B[32m"
    Color.Yellow -> "\u001B[93m"
    Color.DarkYellow -> "\u001B[33m"
    Color.Blue -> "\u001B[94m"
    Color.DarkBlue -> "\u001B[34m"
    Color.Magenta -> "\u001B[95m"
    Color.DarkMagenta -> "\u001B[35m"
    Color.Cyan -> "\u001B[96m"
    Color.DarkCyan -> "\u001B[36m"
    Color.White -> "\u001B[97m"
    Color.Grey -> "\u001B[37m"
    is Color.Ansi -> "\u001B[38;5;${color.value}m"
    is Color.Rgb -> "\u001B[38;2;${color.r};${color.g};${color.b}m"
}

private fun colorToBackgroundAnsi(color: Color): String = when (color) {
    Color.Reset -> "\u001B[49m"
    Color.Black -> "\u001B[40m"
    Color.DarkGrey -> "\u001B[100m"
    Color.Red -> "\u001B[101m"
    Color.DarkRed -> "\u001B[41m"
    Color.Green -> "\u001B[102m"
    Color.DarkGreen -> "\u001B[42m"
    Color.Yellow -> "\u001B[103m"
    Color.DarkYellow -> "\u001B[43m"
    Color.Blue -> "\u001B[104m"
    Color.DarkBlue -> "\u001B[44m"
    Color.Magenta -> "\u001B[105m"
    Color.DarkMagenta -> "\u001B[45m"
    Color.Cyan -> "\u001B[106m"
    Color.DarkCyan -> "\u001B[46m"
    Color.White -> "\u001B[107m"
    Color.Grey -> "\u001B[47m"
    is Color.Ansi -> "\u001B[48;5;${color.value}m"
    is Color.Rgb -> "\u001B[48;2;${color.r};${color.g};${color.b}m"
}
