// port-lint: source style/stylize.rs
package io.github.kotlinmania.crossterm.style

/**
 * Provides a set of methods to set attributes and colors.
 *
 * This interface defines a fluent API for styling content with colors and text attributes.
 * Types implementing this interface can be styled using method chaining.
 *
 * Example:
 * ```kotlin
 * import io.github.kotlinmania.crossterm.style.*
 *
 * println("Bold text".bold())
 * println("Underlined text".underlined())
 * println("Negative text".negative())
 * println("Red on blue".red().onBlue())
 * ```
 */
interface Stylize<S : Stylize<S>> {
    /**
     * Styles this type.
     */
    fun stylize(): S

    /**
     * Sets the foreground color.
     */
    fun with(color: Color): S

    /**
     * Sets the background color.
     */
    fun on(color: Color): S

    /**
     * Sets the underline color.
     */
    fun underline(color: Color): S

    /**
     * Styles the content with the attribute.
     */
    fun attribute(attr: Attribute): S

    // Attribute methods

    /** Applies the [Attribute.Reset] attribute to the text. */
    fun reset(): S = attribute(Attribute.Reset)

    /** Applies the [Attribute.Bold] attribute to the text. */
    fun bold(): S = attribute(Attribute.Bold)

    /** Applies the [Attribute.Underlined] attribute to the text. */
    fun underlined(): S = attribute(Attribute.Underlined)

    /** Applies the [Attribute.Reverse] attribute to the text. */
    fun reverse(): S = attribute(Attribute.Reverse)

    /** Applies the [Attribute.Dim] attribute to the text. */
    fun dim(): S = attribute(Attribute.Dim)

    /** Applies the [Attribute.Italic] attribute to the text. */
    fun italic(): S = attribute(Attribute.Italic)

    /** Applies the [Attribute.Reverse] attribute to the text. (Alias for reverse) */
    fun negative(): S = attribute(Attribute.Reverse)

    /** Applies the [Attribute.SlowBlink] attribute to the text. */
    fun slowBlink(): S = attribute(Attribute.SlowBlink)

    /** Applies the [Attribute.RapidBlink] attribute to the text. */
    fun rapidBlink(): S = attribute(Attribute.RapidBlink)

    /** Applies the [Attribute.Hidden] attribute to the text. */
    fun hidden(): S = attribute(Attribute.Hidden)

    /** Applies the [Attribute.CrossedOut] attribute to the text. */
    fun crossedOut(): S = attribute(Attribute.CrossedOut)

    // Foreground color methods

    /** Sets the foreground color to [Color.Black]. */
    fun black(): S = with(Color.Black)

    /** Sets the foreground color to [Color.DarkGrey]. */
    fun darkGrey(): S = with(Color.DarkGrey)

    /** Sets the foreground color to [Color.Red]. */
    fun red(): S = with(Color.Red)

    /** Sets the foreground color to [Color.DarkRed]. */
    fun darkRed(): S = with(Color.DarkRed)

    /** Sets the foreground color to [Color.Green]. */
    fun green(): S = with(Color.Green)

    /** Sets the foreground color to [Color.DarkGreen]. */
    fun darkGreen(): S = with(Color.DarkGreen)

    /** Sets the foreground color to [Color.Yellow]. */
    fun yellow(): S = with(Color.Yellow)

    /** Sets the foreground color to [Color.DarkYellow]. */
    fun darkYellow(): S = with(Color.DarkYellow)

    /** Sets the foreground color to [Color.Blue]. */
    fun blue(): S = with(Color.Blue)

    /** Sets the foreground color to [Color.DarkBlue]. */
    fun darkBlue(): S = with(Color.DarkBlue)

    /** Sets the foreground color to [Color.Magenta]. */
    fun magenta(): S = with(Color.Magenta)

    /** Sets the foreground color to [Color.DarkMagenta]. */
    fun darkMagenta(): S = with(Color.DarkMagenta)

    /** Sets the foreground color to [Color.Cyan]. */
    fun cyan(): S = with(Color.Cyan)

    /** Sets the foreground color to [Color.DarkCyan]. */
    fun darkCyan(): S = with(Color.DarkCyan)

    /** Sets the foreground color to [Color.White]. */
    fun white(): S = with(Color.White)

    /** Sets the foreground color to [Color.Grey]. */
    fun grey(): S = with(Color.Grey)

    // Background color methods

    /** Sets the background color to [Color.Black]. */
    fun onBlack(): S = on(Color.Black)

    /** Sets the background color to [Color.DarkGrey]. */
    fun onDarkGrey(): S = on(Color.DarkGrey)

    /** Sets the background color to [Color.Red]. */
    fun onRed(): S = on(Color.Red)

    /** Sets the background color to [Color.DarkRed]. */
    fun onDarkRed(): S = on(Color.DarkRed)

    /** Sets the background color to [Color.Green]. */
    fun onGreen(): S = on(Color.Green)

    /** Sets the background color to [Color.DarkGreen]. */
    fun onDarkGreen(): S = on(Color.DarkGreen)

    /** Sets the background color to [Color.Yellow]. */
    fun onYellow(): S = on(Color.Yellow)

    /** Sets the background color to [Color.DarkYellow]. */
    fun onDarkYellow(): S = on(Color.DarkYellow)

    /** Sets the background color to [Color.Blue]. */
    fun onBlue(): S = on(Color.Blue)

    /** Sets the background color to [Color.DarkBlue]. */
    fun onDarkBlue(): S = on(Color.DarkBlue)

    /** Sets the background color to [Color.Magenta]. */
    fun onMagenta(): S = on(Color.Magenta)

    /** Sets the background color to [Color.DarkMagenta]. */
    fun onDarkMagenta(): S = on(Color.DarkMagenta)

    /** Sets the background color to [Color.Cyan]. */
    fun onCyan(): S = on(Color.Cyan)

    /** Sets the background color to [Color.DarkCyan]. */
    fun onDarkCyan(): S = on(Color.DarkCyan)

    /** Sets the background color to [Color.White]. */
    fun onWhite(): S = on(Color.White)

    /** Sets the background color to [Color.Grey]. */
    fun onGrey(): S = on(Color.Grey)

    // Underline color methods

    /** Sets the underline color to [Color.Black]. */
    fun underlineBlack(): S = underline(Color.Black)

    /** Sets the underline color to [Color.DarkGrey]. */
    fun underlineDarkGrey(): S = underline(Color.DarkGrey)

    /** Sets the underline color to [Color.Red]. */
    fun underlineRed(): S = underline(Color.Red)

    /** Sets the underline color to [Color.DarkRed]. */
    fun underlineDarkRed(): S = underline(Color.DarkRed)

    /** Sets the underline color to [Color.Green]. */
    fun underlineGreen(): S = underline(Color.Green)

    /** Sets the underline color to [Color.DarkGreen]. */
    fun underlineDarkGreen(): S = underline(Color.DarkGreen)

    /** Sets the underline color to [Color.Yellow]. */
    fun underlineYellow(): S = underline(Color.Yellow)

    /** Sets the underline color to [Color.DarkYellow]. */
    fun underlineDarkYellow(): S = underline(Color.DarkYellow)

    /** Sets the underline color to [Color.Blue]. */
    fun underlineBlue(): S = underline(Color.Blue)

    /** Sets the underline color to [Color.DarkBlue]. */
    fun underlineDarkBlue(): S = underline(Color.DarkBlue)

    /** Sets the underline color to [Color.Magenta]. */
    fun underlineMagenta(): S = underline(Color.Magenta)

    /** Sets the underline color to [Color.DarkMagenta]. */
    fun underlineDarkMagenta(): S = underline(Color.DarkMagenta)

    /** Sets the underline color to [Color.Cyan]. */
    fun underlineCyan(): S = underline(Color.Cyan)

    /** Sets the underline color to [Color.DarkCyan]. */
    fun underlineDarkCyan(): S = underline(Color.DarkCyan)

    /** Sets the underline color to [Color.White]. */
    fun underlineWhite(): S = underline(Color.White)

    /** Sets the underline color to [Color.Grey]. */
    fun underlineGrey(): S = underline(Color.Grey)
}

/**
 * Creates a [StyledContent] with the given content.
 * This is a convenience function to start styling content.
 *
 * @param content The content to style.
 * @return A new [StyledContent] with default style.
 */
fun <D> style(content: D): StyledContent<D> = StyledContent(ContentStyle.new(), content)

// ============================================================================
// ContentStyle Stylize Extensions
// ============================================================================

/**
 * Extension function to make [ContentStyle] work with styling methods.
 */
fun ContentStyle.stylize(): ContentStyle = this

/**
 * Extension function to set foreground color on [ContentStyle].
 */
fun ContentStyle.with(color: Color): ContentStyle =
    copy(foregroundColor = color)

/**
 * Extension function to set background color on [ContentStyle].
 */
fun ContentStyle.on(color: Color): ContentStyle =
    copy(backgroundColor = color)

/**
 * Extension function to set underline color on [ContentStyle].
 */
fun ContentStyle.underline(color: Color): ContentStyle =
    copy(underlineColor = color)

/**
 * Extension function to add an attribute on [ContentStyle].
 */
fun ContentStyle.attribute(attr: Attribute): ContentStyle =
    copy(attributes = attributes.set(attr))

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
fun <D> StyledContent<D>.stylize(): StyledContent<D> = this

/**
 * Extension function to set foreground color on [StyledContent].
 */
fun <D> StyledContent<D>.with(color: Color): StyledContent<D> =
    withStyle { it.copy(foregroundColor = color) }

/**
 * Extension function to set background color on [StyledContent].
 */
fun <D> StyledContent<D>.on(color: Color): StyledContent<D> =
    withStyle { it.copy(backgroundColor = color) }

/**
 * Extension function to set underline color on [StyledContent].
 */
fun <D> StyledContent<D>.underline(color: Color): StyledContent<D> =
    withStyle { it.copy(underlineColor = color) }

/**
 * Extension function to add an attribute on [StyledContent].
 */
fun <D> StyledContent<D>.attribute(attr: Attribute): StyledContent<D> =
    withStyle { it.copy(attributes = it.attributes.set(attr)) }

/** Applies the [Attribute.Reset] attribute to the text. */
fun <D> StyledContent<D>.reset(): StyledContent<D> = attribute(Attribute.Reset)

/** Applies the [Attribute.Bold] attribute to the text. */
fun <D> StyledContent<D>.bold(): StyledContent<D> = attribute(Attribute.Bold)

/** Applies the [Attribute.Underlined] attribute to the text. */
fun <D> StyledContent<D>.underlined(): StyledContent<D> = attribute(Attribute.Underlined)

/** Applies the [Attribute.Reverse] attribute to the text. */
fun <D> StyledContent<D>.reverse(): StyledContent<D> = attribute(Attribute.Reverse)

/** Applies the [Attribute.Dim] attribute to the text. */
fun <D> StyledContent<D>.dim(): StyledContent<D> = attribute(Attribute.Dim)

/** Applies the [Attribute.Italic] attribute to the text. */
fun <D> StyledContent<D>.italic(): StyledContent<D> = attribute(Attribute.Italic)

/** Applies the [Attribute.Reverse] attribute to the text. (Alias for reverse) */
fun <D> StyledContent<D>.negative(): StyledContent<D> = attribute(Attribute.Reverse)

/** Applies the [Attribute.SlowBlink] attribute to the text. */
fun <D> StyledContent<D>.slowBlink(): StyledContent<D> = attribute(Attribute.SlowBlink)

/** Applies the [Attribute.RapidBlink] attribute to the text. */
fun <D> StyledContent<D>.rapidBlink(): StyledContent<D> = attribute(Attribute.RapidBlink)

/** Applies the [Attribute.Hidden] attribute to the text. */
fun <D> StyledContent<D>.hidden(): StyledContent<D> = attribute(Attribute.Hidden)

/** Applies the [Attribute.CrossedOut] attribute to the text. */
fun <D> StyledContent<D>.crossedOut(): StyledContent<D> = attribute(Attribute.CrossedOut)

// Foreground color extension functions for StyledContent

/** Sets the foreground color to [Color.Black]. */
fun <D> StyledContent<D>.black(): StyledContent<D> = with(Color.Black)

/** Sets the foreground color to [Color.DarkGrey]. */
fun <D> StyledContent<D>.darkGrey(): StyledContent<D> = with(Color.DarkGrey)

/** Sets the foreground color to [Color.Red]. */
fun <D> StyledContent<D>.red(): StyledContent<D> = with(Color.Red)

/** Sets the foreground color to [Color.DarkRed]. */
fun <D> StyledContent<D>.darkRed(): StyledContent<D> = with(Color.DarkRed)

/** Sets the foreground color to [Color.Green]. */
fun <D> StyledContent<D>.green(): StyledContent<D> = with(Color.Green)

/** Sets the foreground color to [Color.DarkGreen]. */
fun <D> StyledContent<D>.darkGreen(): StyledContent<D> = with(Color.DarkGreen)

/** Sets the foreground color to [Color.Yellow]. */
fun <D> StyledContent<D>.yellow(): StyledContent<D> = with(Color.Yellow)

/** Sets the foreground color to [Color.DarkYellow]. */
fun <D> StyledContent<D>.darkYellow(): StyledContent<D> = with(Color.DarkYellow)

/** Sets the foreground color to [Color.Blue]. */
fun <D> StyledContent<D>.blue(): StyledContent<D> = with(Color.Blue)

/** Sets the foreground color to [Color.DarkBlue]. */
fun <D> StyledContent<D>.darkBlue(): StyledContent<D> = with(Color.DarkBlue)

/** Sets the foreground color to [Color.Magenta]. */
fun <D> StyledContent<D>.magenta(): StyledContent<D> = with(Color.Magenta)

/** Sets the foreground color to [Color.DarkMagenta]. */
fun <D> StyledContent<D>.darkMagenta(): StyledContent<D> = with(Color.DarkMagenta)

/** Sets the foreground color to [Color.Cyan]. */
fun <D> StyledContent<D>.cyan(): StyledContent<D> = with(Color.Cyan)

/** Sets the foreground color to [Color.DarkCyan]. */
fun <D> StyledContent<D>.darkCyan(): StyledContent<D> = with(Color.DarkCyan)

/** Sets the foreground color to [Color.White]. */
fun <D> StyledContent<D>.white(): StyledContent<D> = with(Color.White)

/** Sets the foreground color to [Color.Grey]. */
fun <D> StyledContent<D>.grey(): StyledContent<D> = with(Color.Grey)

// Background color extension functions for StyledContent

/** Sets the background color to [Color.Black]. */
fun <D> StyledContent<D>.onBlack(): StyledContent<D> = on(Color.Black)

/** Sets the background color to [Color.DarkGrey]. */
fun <D> StyledContent<D>.onDarkGrey(): StyledContent<D> = on(Color.DarkGrey)

/** Sets the background color to [Color.Red]. */
fun <D> StyledContent<D>.onRed(): StyledContent<D> = on(Color.Red)

/** Sets the background color to [Color.DarkRed]. */
fun <D> StyledContent<D>.onDarkRed(): StyledContent<D> = on(Color.DarkRed)

/** Sets the background color to [Color.Green]. */
fun <D> StyledContent<D>.onGreen(): StyledContent<D> = on(Color.Green)

/** Sets the background color to [Color.DarkGreen]. */
fun <D> StyledContent<D>.onDarkGreen(): StyledContent<D> = on(Color.DarkGreen)

/** Sets the background color to [Color.Yellow]. */
fun <D> StyledContent<D>.onYellow(): StyledContent<D> = on(Color.Yellow)

/** Sets the background color to [Color.DarkYellow]. */
fun <D> StyledContent<D>.onDarkYellow(): StyledContent<D> = on(Color.DarkYellow)

/** Sets the background color to [Color.Blue]. */
fun <D> StyledContent<D>.onBlue(): StyledContent<D> = on(Color.Blue)

/** Sets the background color to [Color.DarkBlue]. */
fun <D> StyledContent<D>.onDarkBlue(): StyledContent<D> = on(Color.DarkBlue)

/** Sets the background color to [Color.Magenta]. */
fun <D> StyledContent<D>.onMagenta(): StyledContent<D> = on(Color.Magenta)

/** Sets the background color to [Color.DarkMagenta]. */
fun <D> StyledContent<D>.onDarkMagenta(): StyledContent<D> = on(Color.DarkMagenta)

/** Sets the background color to [Color.Cyan]. */
fun <D> StyledContent<D>.onCyan(): StyledContent<D> = on(Color.Cyan)

/** Sets the background color to [Color.DarkCyan]. */
fun <D> StyledContent<D>.onDarkCyan(): StyledContent<D> = on(Color.DarkCyan)

/** Sets the background color to [Color.White]. */
fun <D> StyledContent<D>.onWhite(): StyledContent<D> = on(Color.White)

/** Sets the background color to [Color.Grey]. */
fun <D> StyledContent<D>.onGrey(): StyledContent<D> = on(Color.Grey)

// Underline color extension functions for StyledContent

/** Sets the underline color to [Color.Black]. */
fun <D> StyledContent<D>.underlineBlack(): StyledContent<D> = underline(Color.Black)

/** Sets the underline color to [Color.DarkGrey]. */
fun <D> StyledContent<D>.underlineDarkGrey(): StyledContent<D> = underline(Color.DarkGrey)

/** Sets the underline color to [Color.Red]. */
fun <D> StyledContent<D>.underlineRed(): StyledContent<D> = underline(Color.Red)

/** Sets the underline color to [Color.DarkRed]. */
fun <D> StyledContent<D>.underlineDarkRed(): StyledContent<D> = underline(Color.DarkRed)

/** Sets the underline color to [Color.Green]. */
fun <D> StyledContent<D>.underlineGreen(): StyledContent<D> = underline(Color.Green)

/** Sets the underline color to [Color.DarkGreen]. */
fun <D> StyledContent<D>.underlineDarkGreen(): StyledContent<D> = underline(Color.DarkGreen)

/** Sets the underline color to [Color.Yellow]. */
fun <D> StyledContent<D>.underlineYellow(): StyledContent<D> = underline(Color.Yellow)

/** Sets the underline color to [Color.DarkYellow]. */
fun <D> StyledContent<D>.underlineDarkYellow(): StyledContent<D> = underline(Color.DarkYellow)

/** Sets the underline color to [Color.Blue]. */
fun <D> StyledContent<D>.underlineBlue(): StyledContent<D> = underline(Color.Blue)

/** Sets the underline color to [Color.DarkBlue]. */
fun <D> StyledContent<D>.underlineDarkBlue(): StyledContent<D> = underline(Color.DarkBlue)

/** Sets the underline color to [Color.Magenta]. */
fun <D> StyledContent<D>.underlineMagenta(): StyledContent<D> = underline(Color.Magenta)

/** Sets the underline color to [Color.DarkMagenta]. */
fun <D> StyledContent<D>.underlineDarkMagenta(): StyledContent<D> = underline(Color.DarkMagenta)

/** Sets the underline color to [Color.Cyan]. */
fun <D> StyledContent<D>.underlineCyan(): StyledContent<D> = underline(Color.Cyan)

/** Sets the underline color to [Color.DarkCyan]. */
fun <D> StyledContent<D>.underlineDarkCyan(): StyledContent<D> = underline(Color.DarkCyan)

/** Sets the underline color to [Color.White]. */
fun <D> StyledContent<D>.underlineWhite(): StyledContent<D> = underline(Color.White)

/** Sets the underline color to [Color.Grey]. */
fun <D> StyledContent<D>.underlineGrey(): StyledContent<D> = underline(Color.Grey)

// ============================================================================
// String Stylize Extensions
// ============================================================================

/**
 * Extension function to start styling a [String].
 */
fun String.stylize(): StyledContent<String> = style(this)

/** Sets the foreground color on a [String]. */
fun String.with(color: Color): StyledContent<String> = stylize().with(color)

/** Sets the background color on a [String]. */
fun String.on(color: Color): StyledContent<String> = stylize().on(color)

/** Sets the underline color on a [String]. */
fun String.underline(color: Color): StyledContent<String> = stylize().underline(color)

/** Adds an attribute to a [String]. */
fun String.attribute(attr: Attribute): StyledContent<String> = stylize().attribute(attr)

/** Applies the [Attribute.Reset] attribute to the text. */
fun String.reset(): StyledContent<String> = attribute(Attribute.Reset)

/** Applies the [Attribute.Bold] attribute to the text. */
fun String.bold(): StyledContent<String> = attribute(Attribute.Bold)

/** Applies the [Attribute.Underlined] attribute to the text. */
fun String.underlined(): StyledContent<String> = attribute(Attribute.Underlined)

/** Applies the [Attribute.Reverse] attribute to the text. */
fun String.reverse(): StyledContent<String> = attribute(Attribute.Reverse)

/** Applies the [Attribute.Dim] attribute to the text. */
fun String.dim(): StyledContent<String> = attribute(Attribute.Dim)

/** Applies the [Attribute.Italic] attribute to the text. */
fun String.italic(): StyledContent<String> = attribute(Attribute.Italic)

/** Applies the [Attribute.Reverse] attribute to the text. (Alias for reverse) */
fun String.negative(): StyledContent<String> = attribute(Attribute.Reverse)

/** Applies the [Attribute.SlowBlink] attribute to the text. */
fun String.slowBlink(): StyledContent<String> = attribute(Attribute.SlowBlink)

/** Applies the [Attribute.RapidBlink] attribute to the text. */
fun String.rapidBlink(): StyledContent<String> = attribute(Attribute.RapidBlink)

/** Applies the [Attribute.Hidden] attribute to the text. */
fun String.hidden(): StyledContent<String> = attribute(Attribute.Hidden)

/** Applies the [Attribute.CrossedOut] attribute to the text. */
fun String.crossedOut(): StyledContent<String> = attribute(Attribute.CrossedOut)

// Foreground color extension functions for String

/** Sets the foreground color to [Color.Black]. */
fun String.black(): StyledContent<String> = with(Color.Black)

/** Sets the foreground color to [Color.DarkGrey]. */
fun String.darkGrey(): StyledContent<String> = with(Color.DarkGrey)

/** Sets the foreground color to [Color.Red]. */
fun String.red(): StyledContent<String> = with(Color.Red)

/** Sets the foreground color to [Color.DarkRed]. */
fun String.darkRed(): StyledContent<String> = with(Color.DarkRed)

/** Sets the foreground color to [Color.Green]. */
fun String.green(): StyledContent<String> = with(Color.Green)

/** Sets the foreground color to [Color.DarkGreen]. */
fun String.darkGreen(): StyledContent<String> = with(Color.DarkGreen)

/** Sets the foreground color to [Color.Yellow]. */
fun String.yellow(): StyledContent<String> = with(Color.Yellow)

/** Sets the foreground color to [Color.DarkYellow]. */
fun String.darkYellow(): StyledContent<String> = with(Color.DarkYellow)

/** Sets the foreground color to [Color.Blue]. */
fun String.blue(): StyledContent<String> = with(Color.Blue)

/** Sets the foreground color to [Color.DarkBlue]. */
fun String.darkBlue(): StyledContent<String> = with(Color.DarkBlue)

/** Sets the foreground color to [Color.Magenta]. */
fun String.magenta(): StyledContent<String> = with(Color.Magenta)

/** Sets the foreground color to [Color.DarkMagenta]. */
fun String.darkMagenta(): StyledContent<String> = with(Color.DarkMagenta)

/** Sets the foreground color to [Color.Cyan]. */
fun String.cyan(): StyledContent<String> = with(Color.Cyan)

/** Sets the foreground color to [Color.DarkCyan]. */
fun String.darkCyan(): StyledContent<String> = with(Color.DarkCyan)

/** Sets the foreground color to [Color.White]. */
fun String.white(): StyledContent<String> = with(Color.White)

/** Sets the foreground color to [Color.Grey]. */
fun String.grey(): StyledContent<String> = with(Color.Grey)

// Background color extension functions for String

/** Sets the background color to [Color.Black]. */
fun String.onBlack(): StyledContent<String> = on(Color.Black)

/** Sets the background color to [Color.DarkGrey]. */
fun String.onDarkGrey(): StyledContent<String> = on(Color.DarkGrey)

/** Sets the background color to [Color.Red]. */
fun String.onRed(): StyledContent<String> = on(Color.Red)

/** Sets the background color to [Color.DarkRed]. */
fun String.onDarkRed(): StyledContent<String> = on(Color.DarkRed)

/** Sets the background color to [Color.Green]. */
fun String.onGreen(): StyledContent<String> = on(Color.Green)

/** Sets the background color to [Color.DarkGreen]. */
fun String.onDarkGreen(): StyledContent<String> = on(Color.DarkGreen)

/** Sets the background color to [Color.Yellow]. */
fun String.onYellow(): StyledContent<String> = on(Color.Yellow)

/** Sets the background color to [Color.DarkYellow]. */
fun String.onDarkYellow(): StyledContent<String> = on(Color.DarkYellow)

/** Sets the background color to [Color.Blue]. */
fun String.onBlue(): StyledContent<String> = on(Color.Blue)

/** Sets the background color to [Color.DarkBlue]. */
fun String.onDarkBlue(): StyledContent<String> = on(Color.DarkBlue)

/** Sets the background color to [Color.Magenta]. */
fun String.onMagenta(): StyledContent<String> = on(Color.Magenta)

/** Sets the background color to [Color.DarkMagenta]. */
fun String.onDarkMagenta(): StyledContent<String> = on(Color.DarkMagenta)

/** Sets the background color to [Color.Cyan]. */
fun String.onCyan(): StyledContent<String> = on(Color.Cyan)

/** Sets the background color to [Color.DarkCyan]. */
fun String.onDarkCyan(): StyledContent<String> = on(Color.DarkCyan)

/** Sets the background color to [Color.White]. */
fun String.onWhite(): StyledContent<String> = on(Color.White)

/** Sets the background color to [Color.Grey]. */
fun String.onGrey(): StyledContent<String> = on(Color.Grey)

// Underline color extension functions for String

/** Sets the underline color to [Color.Black]. */
fun String.underlineBlack(): StyledContent<String> = underline(Color.Black)

/** Sets the underline color to [Color.DarkGrey]. */
fun String.underlineDarkGrey(): StyledContent<String> = underline(Color.DarkGrey)

/** Sets the underline color to [Color.Red]. */
fun String.underlineRed(): StyledContent<String> = underline(Color.Red)

/** Sets the underline color to [Color.DarkRed]. */
fun String.underlineDarkRed(): StyledContent<String> = underline(Color.DarkRed)

/** Sets the underline color to [Color.Green]. */
fun String.underlineGreen(): StyledContent<String> = underline(Color.Green)

/** Sets the underline color to [Color.DarkGreen]. */
fun String.underlineDarkGreen(): StyledContent<String> = underline(Color.DarkGreen)

/** Sets the underline color to [Color.Yellow]. */
fun String.underlineYellow(): StyledContent<String> = underline(Color.Yellow)

/** Sets the underline color to [Color.DarkYellow]. */
fun String.underlineDarkYellow(): StyledContent<String> = underline(Color.DarkYellow)

/** Sets the underline color to [Color.Blue]. */
fun String.underlineBlue(): StyledContent<String> = underline(Color.Blue)

/** Sets the underline color to [Color.DarkBlue]. */
fun String.underlineDarkBlue(): StyledContent<String> = underline(Color.DarkBlue)

/** Sets the underline color to [Color.Magenta]. */
fun String.underlineMagenta(): StyledContent<String> = underline(Color.Magenta)

/** Sets the underline color to [Color.DarkMagenta]. */
fun String.underlineDarkMagenta(): StyledContent<String> = underline(Color.DarkMagenta)

/** Sets the underline color to [Color.Cyan]. */
fun String.underlineCyan(): StyledContent<String> = underline(Color.Cyan)

/** Sets the underline color to [Color.DarkCyan]. */
fun String.underlineDarkCyan(): StyledContent<String> = underline(Color.DarkCyan)

/** Sets the underline color to [Color.White]. */
fun String.underlineWhite(): StyledContent<String> = underline(Color.White)

/** Sets the underline color to [Color.Grey]. */
fun String.underlineGrey(): StyledContent<String> = underline(Color.Grey)

// ============================================================================
// Char Stylize Extensions
// ============================================================================

/**
 * Extension function to start styling a [Char].
 */
fun Char.stylize(): StyledContent<Char> = style(this)

/** Sets the foreground color on a [Char]. */
fun Char.with(color: Color): StyledContent<Char> = stylize().with(color)

/** Sets the background color on a [Char]. */
fun Char.on(color: Color): StyledContent<Char> = stylize().on(color)

/** Sets the underline color on a [Char]. */
fun Char.underline(color: Color): StyledContent<Char> = stylize().underline(color)

/** Adds an attribute to a [Char]. */
fun Char.attribute(attr: Attribute): StyledContent<Char> = stylize().attribute(attr)

/** Applies the [Attribute.Bold] attribute to the char. */
fun Char.bold(): StyledContent<Char> = attribute(Attribute.Bold)

/** Applies the [Attribute.Underlined] attribute to the char. */
fun Char.underlined(): StyledContent<Char> = attribute(Attribute.Underlined)

/** Applies the [Attribute.Reverse] attribute to the char. */
fun Char.reverse(): StyledContent<Char> = attribute(Attribute.Reverse)

/** Applies the [Attribute.Dim] attribute to the char. */
fun Char.dim(): StyledContent<Char> = attribute(Attribute.Dim)

/** Applies the [Attribute.Italic] attribute to the char. */
fun Char.italic(): StyledContent<Char> = attribute(Attribute.Italic)

/** Applies the [Attribute.Reverse] attribute to the char. (Alias for reverse) */
fun Char.negative(): StyledContent<Char> = attribute(Attribute.Reverse)

/** Sets the foreground color to [Color.Red]. */
fun Char.red(): StyledContent<Char> = with(Color.Red)

/** Sets the foreground color to [Color.Green]. */
fun Char.green(): StyledContent<Char> = with(Color.Green)

/** Sets the foreground color to [Color.Blue]. */
fun Char.blue(): StyledContent<Char> = with(Color.Blue)

/** Sets the foreground color to [Color.Yellow]. */
fun Char.yellow(): StyledContent<Char> = with(Color.Yellow)

/** Sets the foreground color to [Color.Magenta]. */
fun Char.magenta(): StyledContent<Char> = with(Color.Magenta)

/** Sets the foreground color to [Color.Cyan]. */
fun Char.cyan(): StyledContent<Char> = with(Color.Cyan)

/** Sets the foreground color to [Color.White]. */
fun Char.white(): StyledContent<Char> = with(Color.White)

/** Sets the foreground color to [Color.Black]. */
fun Char.black(): StyledContent<Char> = with(Color.Black)
