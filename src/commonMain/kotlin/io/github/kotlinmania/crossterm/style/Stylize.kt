// port-lint: source style/stylize.rs

package io.github.kotlinmania.crossterm.style

import io.github.kotlinmania.crossterm.style.types.Attribute
import io.github.kotlinmania.crossterm.style.types.Color

// ============================================================================
// ContentStyle Stylize Extensions
// ============================================================================

/**
 * Extension function to set foreground color on [ContentStyle].
 */
fun ContentStyle.with(color: Color): ContentStyle {
    foregroundColor = color
    return this
}

/**
 * Extension function to set background color on [ContentStyle].
 */
fun ContentStyle.on(color: Color): ContentStyle {
    backgroundColor = color
    return this
}

/**
 * Extension function to set underline color on [ContentStyle].
 */
fun ContentStyle.underline(color: Color): ContentStyle {
    underlineColor = color
    return this
}

/**
 * Extension function to add an attribute on [ContentStyle].
 */
fun ContentStyle.attribute(attr: Attribute): ContentStyle {
    attributes = attributes.set(attr)
    return this
}

/** Applies the [Attribute.Reset] attribute. */
fun ContentStyle.reset(): ContentStyle = attribute(Attribute.Reset)

/** Applies the [Attribute.Bold] attribute. */
fun ContentStyle.bold(): ContentStyle = attribute(Attribute.Bold)

/** Applies the [Attribute.Underlined] attribute. */
fun ContentStyle.underlined(): ContentStyle = attribute(Attribute.Underlined)

/** Applies the [Attribute.Reverse] attribute. */
fun ContentStyle.reverse(): ContentStyle = attribute(Attribute.Reverse)

/** Applies the [Attribute.Dim] attribute. */
fun ContentStyle.dim(): ContentStyle = attribute(Attribute.Dim)

/** Applies the [Attribute.Italic] attribute. */
fun ContentStyle.italic(): ContentStyle = attribute(Attribute.Italic)

/** Applies the [Attribute.Reverse] attribute. (Alias for reverse) */
fun ContentStyle.negative(): ContentStyle = attribute(Attribute.Reverse)

/** Applies the [Attribute.SlowBlink] attribute. */
fun ContentStyle.slowBlink(): ContentStyle = attribute(Attribute.SlowBlink)

/** Applies the [Attribute.RapidBlink] attribute. */
fun ContentStyle.rapidBlink(): ContentStyle = attribute(Attribute.RapidBlink)

/** Applies the [Attribute.Hidden] attribute. */
fun ContentStyle.hidden(): ContentStyle = attribute(Attribute.Hidden)

/** Applies the [Attribute.CrossedOut] attribute. */
fun ContentStyle.crossedOut(): ContentStyle = attribute(Attribute.CrossedOut)

// Foreground color methods for ContentStyle

/** Sets the foreground color to [Color.Black]. */
fun ContentStyle.black(): ContentStyle = with(Color.Black)

/** Sets the foreground color to [Color.DarkGrey]. */
fun ContentStyle.darkGrey(): ContentStyle = with(Color.DarkGrey)

/** Sets the foreground color to [Color.Red]. */
fun ContentStyle.red(): ContentStyle = with(Color.Red)

/** Sets the foreground color to [Color.DarkRed]. */
fun ContentStyle.darkRed(): ContentStyle = with(Color.DarkRed)

/** Sets the foreground color to [Color.Green]. */
fun ContentStyle.green(): ContentStyle = with(Color.Green)

/** Sets the foreground color to [Color.DarkGreen]. */
fun ContentStyle.darkGreen(): ContentStyle = with(Color.DarkGreen)

/** Sets the foreground color to [Color.Yellow]. */
fun ContentStyle.yellow(): ContentStyle = with(Color.Yellow)

/** Sets the foreground color to [Color.DarkYellow]. */
fun ContentStyle.darkYellow(): ContentStyle = with(Color.DarkYellow)

/** Sets the foreground color to [Color.Blue]. */
fun ContentStyle.blue(): ContentStyle = with(Color.Blue)

/** Sets the foreground color to [Color.DarkBlue]. */
fun ContentStyle.darkBlue(): ContentStyle = with(Color.DarkBlue)

/** Sets the foreground color to [Color.Magenta]. */
fun ContentStyle.magenta(): ContentStyle = with(Color.Magenta)

/** Sets the foreground color to [Color.DarkMagenta]. */
fun ContentStyle.darkMagenta(): ContentStyle = with(Color.DarkMagenta)

/** Sets the foreground color to [Color.Cyan]. */
fun ContentStyle.cyan(): ContentStyle = with(Color.Cyan)

/** Sets the foreground color to [Color.DarkCyan]. */
fun ContentStyle.darkCyan(): ContentStyle = with(Color.DarkCyan)

/** Sets the foreground color to [Color.White]. */
fun ContentStyle.white(): ContentStyle = with(Color.White)

/** Sets the foreground color to [Color.Grey]. */
fun ContentStyle.grey(): ContentStyle = with(Color.Grey)

// Background color methods for ContentStyle

/** Sets the background color to [Color.Black]. */
fun ContentStyle.onBlack(): ContentStyle = on(Color.Black)

/** Sets the background color to [Color.DarkGrey]. */
fun ContentStyle.onDarkGrey(): ContentStyle = on(Color.DarkGrey)

/** Sets the background color to [Color.Red]. */
fun ContentStyle.onRed(): ContentStyle = on(Color.Red)

/** Sets the background color to [Color.DarkRed]. */
fun ContentStyle.onDarkRed(): ContentStyle = on(Color.DarkRed)

/** Sets the background color to [Color.Green]. */
fun ContentStyle.onGreen(): ContentStyle = on(Color.Green)

/** Sets the background color to [Color.DarkGreen]. */
fun ContentStyle.onDarkGreen(): ContentStyle = on(Color.DarkGreen)

/** Sets the background color to [Color.Yellow]. */
fun ContentStyle.onYellow(): ContentStyle = on(Color.Yellow)

/** Sets the background color to [Color.DarkYellow]. */
fun ContentStyle.onDarkYellow(): ContentStyle = on(Color.DarkYellow)

/** Sets the background color to [Color.Blue]. */
fun ContentStyle.onBlue(): ContentStyle = on(Color.Blue)

/** Sets the background color to [Color.DarkBlue]. */
fun ContentStyle.onDarkBlue(): ContentStyle = on(Color.DarkBlue)

/** Sets the background color to [Color.Magenta]. */
fun ContentStyle.onMagenta(): ContentStyle = on(Color.Magenta)

/** Sets the background color to [Color.DarkMagenta]. */
fun ContentStyle.onDarkMagenta(): ContentStyle = on(Color.DarkMagenta)

/** Sets the background color to [Color.Cyan]. */
fun ContentStyle.onCyan(): ContentStyle = on(Color.Cyan)

/** Sets the background color to [Color.DarkCyan]. */
fun ContentStyle.onDarkCyan(): ContentStyle = on(Color.DarkCyan)

/** Sets the background color to [Color.White]. */
fun ContentStyle.onWhite(): ContentStyle = on(Color.White)

/** Sets the background color to [Color.Grey]. */
fun ContentStyle.onGrey(): ContentStyle = on(Color.Grey)

// Underline color methods for ContentStyle

/** Sets the underline color to [Color.Black]. */
fun ContentStyle.underlineBlack(): ContentStyle = underline(Color.Black)

/** Sets the underline color to [Color.DarkGrey]. */
fun ContentStyle.underlineDarkGrey(): ContentStyle = underline(Color.DarkGrey)

/** Sets the underline color to [Color.Red]. */
fun ContentStyle.underlineRed(): ContentStyle = underline(Color.Red)

/** Sets the underline color to [Color.DarkRed]. */
fun ContentStyle.underlineDarkRed(): ContentStyle = underline(Color.DarkRed)

/** Sets the underline color to [Color.Green]. */
fun ContentStyle.underlineGreen(): ContentStyle = underline(Color.Green)

/** Sets the underline color to [Color.DarkGreen]. */
fun ContentStyle.underlineDarkGreen(): ContentStyle = underline(Color.DarkGreen)

/** Sets the underline color to [Color.Yellow]. */
fun ContentStyle.underlineYellow(): ContentStyle = underline(Color.Yellow)

/** Sets the underline color to [Color.DarkYellow]. */
fun ContentStyle.underlineDarkYellow(): ContentStyle = underline(Color.DarkYellow)

/** Sets the underline color to [Color.Blue]. */
fun ContentStyle.underlineBlue(): ContentStyle = underline(Color.Blue)

/** Sets the underline color to [Color.DarkBlue]. */
fun ContentStyle.underlineDarkBlue(): ContentStyle = underline(Color.DarkBlue)

/** Sets the underline color to [Color.Magenta]. */
fun ContentStyle.underlineMagenta(): ContentStyle = underline(Color.Magenta)

/** Sets the underline color to [Color.DarkMagenta]. */
fun ContentStyle.underlineDarkMagenta(): ContentStyle = underline(Color.DarkMagenta)

/** Sets the underline color to [Color.Cyan]. */
fun ContentStyle.underlineCyan(): ContentStyle = underline(Color.Cyan)

/** Sets the underline color to [Color.DarkCyan]. */
fun ContentStyle.underlineDarkCyan(): ContentStyle = underline(Color.DarkCyan)

/** Sets the underline color to [Color.White]. */
fun ContentStyle.underlineWhite(): ContentStyle = underline(Color.White)

/** Sets the underline color to [Color.Grey]. */
fun ContentStyle.underlineGrey(): ContentStyle = underline(Color.Grey)

// ============================================================================
// StyledContent Stylize Extensions
// ============================================================================

/**
 * Extension function to make [StyledContent] work with styling methods.
 */
fun StyledContent.stylize(): StyledContent = this

/**
 * Extension function to set foreground color on [StyledContent].
 */
fun StyledContent.with(color: Color): StyledContent {
    val styled = stylize()
    styled.asMut().foregroundColor = color
    return styled
}

/**
 * Extension function to set background color on [StyledContent].
 */
fun StyledContent.on(color: Color): StyledContent {
    val styled = stylize()
    styled.asMut().backgroundColor = color
    return styled
}

/**
 * Extension function to set underline color on [StyledContent].
 */
fun StyledContent.underline(color: Color): StyledContent {
    val styled = stylize()
    styled.asMut().underlineColor = color
    return styled
}

/**
 * Extension function to add an attribute on [StyledContent].
 */
fun StyledContent.attribute(attr: Attribute): StyledContent {
    val styled = stylize()
    val s = styled.asMut()
    s.attributes = s.attributes.set(attr)
    return styled
}

/** Applies the [Attribute.Reset] attribute to the text. */
fun StyledContent.reset(): StyledContent = attribute(Attribute.Reset)

/** Applies the [Attribute.Bold] attribute to the text. */
fun StyledContent.bold(): StyledContent = attribute(Attribute.Bold)

/** Applies the [Attribute.Underlined] attribute to the text. */
fun StyledContent.underlined(): StyledContent = attribute(Attribute.Underlined)

/** Applies the [Attribute.Reverse] attribute to the text. */
fun StyledContent.reverse(): StyledContent = attribute(Attribute.Reverse)

/** Applies the [Attribute.Dim] attribute to the text. */
fun StyledContent.dim(): StyledContent = attribute(Attribute.Dim)

/** Applies the [Attribute.Italic] attribute to the text. */
fun StyledContent.italic(): StyledContent = attribute(Attribute.Italic)

/** Applies the [Attribute.Reverse] attribute to the text. (Alias for reverse) */
fun StyledContent.negative(): StyledContent = attribute(Attribute.Reverse)

/** Applies the [Attribute.SlowBlink] attribute to the text. */
fun StyledContent.slowBlink(): StyledContent = attribute(Attribute.SlowBlink)

/** Applies the [Attribute.RapidBlink] attribute to the text. */
fun StyledContent.rapidBlink(): StyledContent = attribute(Attribute.RapidBlink)

/** Applies the [Attribute.Hidden] attribute to the text. */
fun StyledContent.hidden(): StyledContent = attribute(Attribute.Hidden)

/** Applies the [Attribute.CrossedOut] attribute to the text. */
fun StyledContent.crossedOut(): StyledContent = attribute(Attribute.CrossedOut)

// Foreground color extension functions for StyledContent

/** Sets the foreground color to [Color.Black]. */
fun StyledContent.black(): StyledContent = with(Color.Black)

/** Sets the foreground color to [Color.DarkGrey]. */
fun StyledContent.darkGrey(): StyledContent = with(Color.DarkGrey)

/** Sets the foreground color to [Color.Red]. */
fun StyledContent.red(): StyledContent = with(Color.Red)

/** Sets the foreground color to [Color.DarkRed]. */
fun StyledContent.darkRed(): StyledContent = with(Color.DarkRed)

/** Sets the foreground color to [Color.Green]. */
fun StyledContent.green(): StyledContent = with(Color.Green)

/** Sets the foreground color to [Color.DarkGreen]. */
fun StyledContent.darkGreen(): StyledContent = with(Color.DarkGreen)

/** Sets the foreground color to [Color.Yellow]. */
fun StyledContent.yellow(): StyledContent = with(Color.Yellow)

/** Sets the foreground color to [Color.DarkYellow]. */
fun StyledContent.darkYellow(): StyledContent = with(Color.DarkYellow)

/** Sets the foreground color to [Color.Blue]. */
fun StyledContent.blue(): StyledContent = with(Color.Blue)

/** Sets the foreground color to [Color.DarkBlue]. */
fun StyledContent.darkBlue(): StyledContent = with(Color.DarkBlue)

/** Sets the foreground color to [Color.Magenta]. */
fun StyledContent.magenta(): StyledContent = with(Color.Magenta)

/** Sets the foreground color to [Color.DarkMagenta]. */
fun StyledContent.darkMagenta(): StyledContent = with(Color.DarkMagenta)

/** Sets the foreground color to [Color.Cyan]. */
fun StyledContent.cyan(): StyledContent = with(Color.Cyan)

/** Sets the foreground color to [Color.DarkCyan]. */
fun StyledContent.darkCyan(): StyledContent = with(Color.DarkCyan)

/** Sets the foreground color to [Color.White]. */
fun StyledContent.white(): StyledContent = with(Color.White)

/** Sets the foreground color to [Color.Grey]. */
fun StyledContent.grey(): StyledContent = with(Color.Grey)

// Background color extension functions for StyledContent

/** Sets the background color to [Color.Black]. */
fun StyledContent.onBlack(): StyledContent = on(Color.Black)

/** Sets the background color to [Color.DarkGrey]. */
fun StyledContent.onDarkGrey(): StyledContent = on(Color.DarkGrey)

/** Sets the background color to [Color.Red]. */
fun StyledContent.onRed(): StyledContent = on(Color.Red)

/** Sets the background color to [Color.DarkRed]. */
fun StyledContent.onDarkRed(): StyledContent = on(Color.DarkRed)

/** Sets the background color to [Color.Green]. */
fun StyledContent.onGreen(): StyledContent = on(Color.Green)

/** Sets the background color to [Color.DarkGreen]. */
fun StyledContent.onDarkGreen(): StyledContent = on(Color.DarkGreen)

/** Sets the background color to [Color.Yellow]. */
fun StyledContent.onYellow(): StyledContent = on(Color.Yellow)

/** Sets the background color to [Color.DarkYellow]. */
fun StyledContent.onDarkYellow(): StyledContent = on(Color.DarkYellow)

/** Sets the background color to [Color.Blue]. */
fun StyledContent.onBlue(): StyledContent = on(Color.Blue)

/** Sets the background color to [Color.DarkBlue]. */
fun StyledContent.onDarkBlue(): StyledContent = on(Color.DarkBlue)

/** Sets the background color to [Color.Magenta]. */
fun StyledContent.onMagenta(): StyledContent = on(Color.Magenta)

/** Sets the background color to [Color.DarkMagenta]. */
fun StyledContent.onDarkMagenta(): StyledContent = on(Color.DarkMagenta)

/** Sets the background color to [Color.Cyan]. */
fun StyledContent.onCyan(): StyledContent = on(Color.Cyan)

/** Sets the background color to [Color.DarkCyan]. */
fun StyledContent.onDarkCyan(): StyledContent = on(Color.DarkCyan)

/** Sets the background color to [Color.White]. */
fun StyledContent.onWhite(): StyledContent = on(Color.White)

/** Sets the background color to [Color.Grey]. */
fun StyledContent.onGrey(): StyledContent = on(Color.Grey)

// Underline color extension functions for StyledContent

/** Sets the underline color to [Color.Black]. */
fun StyledContent.underlineBlack(): StyledContent = underline(Color.Black)

/** Sets the underline color to [Color.DarkGrey]. */
fun StyledContent.underlineDarkGrey(): StyledContent = underline(Color.DarkGrey)

/** Sets the underline color to [Color.Red]. */
fun StyledContent.underlineRed(): StyledContent = underline(Color.Red)

/** Sets the underline color to [Color.DarkRed]. */
fun StyledContent.underlineDarkRed(): StyledContent = underline(Color.DarkRed)

/** Sets the underline color to [Color.Green]. */
fun StyledContent.underlineGreen(): StyledContent = underline(Color.Green)

/** Sets the underline color to [Color.DarkGreen]. */
fun StyledContent.underlineDarkGreen(): StyledContent = underline(Color.DarkGreen)

/** Sets the underline color to [Color.Yellow]. */
fun StyledContent.underlineYellow(): StyledContent = underline(Color.Yellow)

/** Sets the underline color to [Color.DarkYellow]. */
fun StyledContent.underlineDarkYellow(): StyledContent = underline(Color.DarkYellow)

/** Sets the underline color to [Color.Blue]. */
fun StyledContent.underlineBlue(): StyledContent = underline(Color.Blue)

/** Sets the underline color to [Color.DarkBlue]. */
fun StyledContent.underlineDarkBlue(): StyledContent = underline(Color.DarkBlue)

/** Sets the underline color to [Color.Magenta]. */
fun StyledContent.underlineMagenta(): StyledContent = underline(Color.Magenta)

/** Sets the underline color to [Color.DarkMagenta]. */
fun StyledContent.underlineDarkMagenta(): StyledContent = underline(Color.DarkMagenta)

/** Sets the underline color to [Color.Cyan]. */
fun StyledContent.underlineCyan(): StyledContent = underline(Color.Cyan)

/** Sets the underline color to [Color.DarkCyan]. */
fun StyledContent.underlineDarkCyan(): StyledContent = underline(Color.DarkCyan)

/** Sets the underline color to [Color.White]. */
fun StyledContent.underlineWhite(): StyledContent = underline(Color.White)

/** Sets the underline color to [Color.Grey]. */
fun StyledContent.underlineGrey(): StyledContent = underline(Color.Grey)

// ============================================================================
// String Stylize Extensions
// ============================================================================

/**
 * Extension function to start styling a [String].
 */
fun String.stylize(): StyledContent = style(this)

/** Sets the foreground color on a [String]. */
fun String.with(color: Color): StyledContent = stylize().with(color)

/** Sets the background color on a [String]. */
fun String.on(color: Color): StyledContent = stylize().on(color)

/** Sets the underline color on a [String]. */
fun String.underline(color: Color): StyledContent = stylize().underline(color)

/** Adds an attribute to a [String]. */
fun String.attribute(attr: Attribute): StyledContent = stylize().attribute(attr)

/** Applies the [Attribute.Reset] attribute to the text. */
fun String.reset(): StyledContent = attribute(Attribute.Reset)

/** Applies the [Attribute.Bold] attribute to the text. */
fun String.bold(): StyledContent = attribute(Attribute.Bold)

/** Applies the [Attribute.Underlined] attribute to the text. */
fun String.underlined(): StyledContent = attribute(Attribute.Underlined)

/** Applies the [Attribute.Reverse] attribute to the text. */
fun String.reverse(): StyledContent = attribute(Attribute.Reverse)

/** Applies the [Attribute.Dim] attribute to the text. */
fun String.dim(): StyledContent = attribute(Attribute.Dim)

/** Applies the [Attribute.Italic] attribute to the text. */
fun String.italic(): StyledContent = attribute(Attribute.Italic)

/** Applies the [Attribute.Reverse] attribute to the text. (Alias for reverse) */
fun String.negative(): StyledContent = attribute(Attribute.Reverse)

/** Applies the [Attribute.SlowBlink] attribute to the text. */
fun String.slowBlink(): StyledContent = attribute(Attribute.SlowBlink)

/** Applies the [Attribute.RapidBlink] attribute to the text. */
fun String.rapidBlink(): StyledContent = attribute(Attribute.RapidBlink)

/** Applies the [Attribute.Hidden] attribute to the text. */
fun String.hidden(): StyledContent = attribute(Attribute.Hidden)

/** Applies the [Attribute.CrossedOut] attribute to the text. */
fun String.crossedOut(): StyledContent = attribute(Attribute.CrossedOut)

// Foreground color extension functions for String

/** Sets the foreground color to [Color.Black]. */
fun String.black(): StyledContent = with(Color.Black)

/** Sets the foreground color to [Color.DarkGrey]. */
fun String.darkGrey(): StyledContent = with(Color.DarkGrey)

/** Sets the foreground color to [Color.Red]. */
fun String.red(): StyledContent = with(Color.Red)

/** Sets the foreground color to [Color.DarkRed]. */
fun String.darkRed(): StyledContent = with(Color.DarkRed)

/** Sets the foreground color to [Color.Green]. */
fun String.green(): StyledContent = with(Color.Green)

/** Sets the foreground color to [Color.DarkGreen]. */
fun String.darkGreen(): StyledContent = with(Color.DarkGreen)

/** Sets the foreground color to [Color.Yellow]. */
fun String.yellow(): StyledContent = with(Color.Yellow)

/** Sets the foreground color to [Color.DarkYellow]. */
fun String.darkYellow(): StyledContent = with(Color.DarkYellow)

/** Sets the foreground color to [Color.Blue]. */
fun String.blue(): StyledContent = with(Color.Blue)

/** Sets the foreground color to [Color.DarkBlue]. */
fun String.darkBlue(): StyledContent = with(Color.DarkBlue)

/** Sets the foreground color to [Color.Magenta]. */
fun String.magenta(): StyledContent = with(Color.Magenta)

/** Sets the foreground color to [Color.DarkMagenta]. */
fun String.darkMagenta(): StyledContent = with(Color.DarkMagenta)

/** Sets the foreground color to [Color.Cyan]. */
fun String.cyan(): StyledContent = with(Color.Cyan)

/** Sets the foreground color to [Color.DarkCyan]. */
fun String.darkCyan(): StyledContent = with(Color.DarkCyan)

/** Sets the foreground color to [Color.White]. */
fun String.white(): StyledContent = with(Color.White)

/** Sets the foreground color to [Color.Grey]. */
fun String.grey(): StyledContent = with(Color.Grey)

// Background color extension functions for String

/** Sets the background color to [Color.Black]. */
fun String.onBlack(): StyledContent = on(Color.Black)

/** Sets the background color to [Color.DarkGrey]. */
fun String.onDarkGrey(): StyledContent = on(Color.DarkGrey)

/** Sets the background color to [Color.Red]. */
fun String.onRed(): StyledContent = on(Color.Red)

/** Sets the background color to [Color.DarkRed]. */
fun String.onDarkRed(): StyledContent = on(Color.DarkRed)

/** Sets the background color to [Color.Green]. */
fun String.onGreen(): StyledContent = on(Color.Green)

/** Sets the background color to [Color.DarkGreen]. */
fun String.onDarkGreen(): StyledContent = on(Color.DarkGreen)

/** Sets the background color to [Color.Yellow]. */
fun String.onYellow(): StyledContent = on(Color.Yellow)

/** Sets the background color to [Color.DarkYellow]. */
fun String.onDarkYellow(): StyledContent = on(Color.DarkYellow)

/** Sets the background color to [Color.Blue]. */
fun String.onBlue(): StyledContent = on(Color.Blue)

/** Sets the background color to [Color.DarkBlue]. */
fun String.onDarkBlue(): StyledContent = on(Color.DarkBlue)

/** Sets the background color to [Color.Magenta]. */
fun String.onMagenta(): StyledContent = on(Color.Magenta)

/** Sets the background color to [Color.DarkMagenta]. */
fun String.onDarkMagenta(): StyledContent = on(Color.DarkMagenta)

/** Sets the background color to [Color.Cyan]. */
fun String.onCyan(): StyledContent = on(Color.Cyan)

/** Sets the background color to [Color.DarkCyan]. */
fun String.onDarkCyan(): StyledContent = on(Color.DarkCyan)

/** Sets the background color to [Color.White]. */
fun String.onWhite(): StyledContent = on(Color.White)

/** Sets the background color to [Color.Grey]. */
fun String.onGrey(): StyledContent = on(Color.Grey)

// Underline color extension functions for String

/** Sets the underline color to [Color.Black]. */
fun String.underlineBlack(): StyledContent = underline(Color.Black)

/** Sets the underline color to [Color.DarkGrey]. */
fun String.underlineDarkGrey(): StyledContent = underline(Color.DarkGrey)

/** Sets the underline color to [Color.Red]. */
fun String.underlineRed(): StyledContent = underline(Color.Red)

/** Sets the underline color to [Color.DarkRed]. */
fun String.underlineDarkRed(): StyledContent = underline(Color.DarkRed)

/** Sets the underline color to [Color.Green]. */
fun String.underlineGreen(): StyledContent = underline(Color.Green)

/** Sets the underline color to [Color.DarkGreen]. */
fun String.underlineDarkGreen(): StyledContent = underline(Color.DarkGreen)

/** Sets the underline color to [Color.Yellow]. */
fun String.underlineYellow(): StyledContent = underline(Color.Yellow)

/** Sets the underline color to [Color.DarkYellow]. */
fun String.underlineDarkYellow(): StyledContent = underline(Color.DarkYellow)

/** Sets the underline color to [Color.Blue]. */
fun String.underlineBlue(): StyledContent = underline(Color.Blue)

/** Sets the underline color to [Color.DarkBlue]. */
fun String.underlineDarkBlue(): StyledContent = underline(Color.DarkBlue)

/** Sets the underline color to [Color.Magenta]. */
fun String.underlineMagenta(): StyledContent = underline(Color.Magenta)

/** Sets the underline color to [Color.DarkMagenta]. */
fun String.underlineDarkMagenta(): StyledContent = underline(Color.DarkMagenta)

/** Sets the underline color to [Color.Cyan]. */
fun String.underlineCyan(): StyledContent = underline(Color.Cyan)

/** Sets the underline color to [Color.DarkCyan]. */
fun String.underlineDarkCyan(): StyledContent = underline(Color.DarkCyan)

/** Sets the underline color to [Color.White]. */
fun String.underlineWhite(): StyledContent = underline(Color.White)

/** Sets the underline color to [Color.Grey]. */
fun String.underlineGrey(): StyledContent = underline(Color.Grey)

// ============================================================================
// Char Stylize Extensions
// ============================================================================

/**
 * Extension function to start styling a [Char].
 */
fun Char.stylize(): StyledContent = style(this.toString())

/** Sets the foreground color on a [Char]. */
fun Char.with(color: Color): StyledContent = stylize().with(color)

/** Sets the background color on a [Char]. */
fun Char.on(color: Color): StyledContent = stylize().on(color)

/** Sets the underline color on a [Char]. */
fun Char.underline(color: Color): StyledContent = stylize().underline(color)

/** Adds an attribute to a [Char]. */
fun Char.attribute(attr: Attribute): StyledContent = stylize().attribute(attr)

/** Applies the [Attribute.Bold] attribute to the char. */
fun Char.bold(): StyledContent = attribute(Attribute.Bold)

/** Applies the [Attribute.Underlined] attribute to the char. */
fun Char.underlined(): StyledContent = attribute(Attribute.Underlined)

/** Applies the [Attribute.Reverse] attribute to the char. */
fun Char.reverse(): StyledContent = attribute(Attribute.Reverse)

/** Applies the [Attribute.Dim] attribute to the char. */
fun Char.dim(): StyledContent = attribute(Attribute.Dim)

/** Applies the [Attribute.Italic] attribute to the char. */
fun Char.italic(): StyledContent = attribute(Attribute.Italic)

/** Applies the [Attribute.Reverse] attribute to the char. (Alias for reverse) */
fun Char.negative(): StyledContent = attribute(Attribute.Reverse)

/** Sets the foreground color to [Color.Red]. */
fun Char.red(): StyledContent = with(Color.Red)

/** Sets the foreground color to [Color.Green]. */
fun Char.green(): StyledContent = with(Color.Green)

/** Sets the foreground color to [Color.Blue]. */
fun Char.blue(): StyledContent = with(Color.Blue)

/** Sets the foreground color to [Color.Yellow]. */
fun Char.yellow(): StyledContent = with(Color.Yellow)

/** Sets the foreground color to [Color.Magenta]. */
fun Char.magenta(): StyledContent = with(Color.Magenta)

/** Sets the foreground color to [Color.Cyan]. */
fun Char.cyan(): StyledContent = with(Color.Cyan)

/** Sets the foreground color to [Color.White]. */
fun Char.white(): StyledContent = with(Color.White)

/** Sets the foreground color to [Color.Black]. */
fun Char.black(): StyledContent = with(Color.Black)
