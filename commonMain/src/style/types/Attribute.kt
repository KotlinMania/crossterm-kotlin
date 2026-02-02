// port-lint: source style/types/attribute.rs
package io.github.kotlinmania.crossterm.style.types

import io.github.kotlinmania.crossterm.Command

/**
 * Represents an attribute.
 *
 * ## Platform-specific Notes
 *
 * * Only UNIX and Windows 10 terminals support text attributes.
 * * Keep in mind that not all terminals support all attributes.
 * * Crossterm implements almost all attributes listed in the
 *   [SGR parameters](https://en.wikipedia.org/wiki/ANSI_escape_code#Select_Graphic_Rendition_parameters).
 *
 * | Attribute | Windows | UNIX | Notes |
 * | :-- | :--: | :--: | :-- |
 * | `Reset` | Yes | Yes | |
 * | `Bold` | Yes | Yes | |
 * | `Dim` | Yes | Yes | |
 * | `Italic` | ? | ? | Not widely supported, sometimes treated as inverse. |
 * | `Underlined` | Yes | Yes | |
 * | `SlowBlink` | ? | ? | Not widely supported, sometimes treated as inverse. |
 * | `RapidBlink` | ? | ? | Not widely supported. MS-DOS ANSI.SYS; 150+ per minute. |
 * | `Reverse` | Yes | Yes | |
 * | `Hidden` | Yes | Yes | Also known as Conceal. |
 * | `Fraktur` | No | Yes | Legible characters, but marked for deletion. |
 * | `DefaultForegroundColor` | ? | ? | Implementation specific (according to standard). |
 * | `DefaultBackgroundColor` | ? | ? | Implementation specific (according to standard). |
 * | `Framed` | ? | ? | Not widely supported. |
 * | `Encircled` | ? | ? | This should turn on the encircled attribute. |
 * | `OverLined` | ? | ? | This should draw a line at the top of the text. |
 *
 * Example:
 * ```kotlin
 * import io.github.kotlinmania.crossterm.style.types.Attribute
 * import io.github.kotlinmania.crossterm.style.types.SetAttribute
 *
 * print("${SetAttribute(Attribute.Underlined).ansiString()} Underlined ${SetAttribute(Attribute.NoUnderline).ansiString()} No Underline")
 * ```
 *
 * Style existing text:
 * ```kotlin
 * // Using stylize extensions (when available)
 * println("Bold text".bold())
 * println("Underlined text".underlined())
 * println("Negative text".negative())
 * ```
 */
enum class Attribute {
    /** Resets all the attributes. */
    Reset,
    /** Increases the text intensity. */
    Bold,
    /** Decreases the text intensity. */
    Dim,
    /** Emphasises the text. */
    Italic,
    /** Underlines the text. */
    Underlined,
    /** Double underlines the text. */
    DoubleUnderlined,
    /** Undercurls the text. */
    Undercurled,
    /** Underdots the text. */
    Underdotted,
    /** Underdashes the text. */
    Underdashed,
    /** Makes the text blinking (< 150 per minute). */
    SlowBlink,
    /** Makes the text blinking (>= 150 per minute). */
    RapidBlink,
    /** Swaps foreground and background colors. */
    Reverse,
    /** Hides the text (also known as Conceal). */
    Hidden,
    /** Crosses the text. */
    CrossedOut,
    /**
     * Sets the [Fraktur](https://en.wikipedia.org/wiki/Fraktur) typeface.
     *
     * Mostly used for [mathematical alphanumeric symbols](https://en.wikipedia.org/wiki/Mathematical_Alphanumeric_Symbols).
     */
    Fraktur,
    /** Turns off the `Bold` attribute. - Inconsistent - Prefer to use NormalIntensity */
    NoBold,
    /** Switches the text back to normal intensity (no bold, italic). */
    NormalIntensity,
    /** Turns off the `Italic` attribute. */
    NoItalic,
    /** Turns off the `Underlined` attribute. */
    NoUnderline,
    /** Turns off the text blinking (`SlowBlink` or `RapidBlink`). */
    NoBlink,
    /** Turns off the `Reverse` attribute. */
    NoReverse,
    /** Turns off the `Hidden` attribute. */
    NoHidden,
    /** Turns off the `CrossedOut` attribute. */
    NotCrossedOut,
    /** Makes the text framed. */
    Framed,
    /** Makes the text encircled. */
    Encircled,
    /** Draws a line at the top of the text. */
    OverLined,
    /** Turns off the `Frame` and `Encircled` attributes. */
    NotFramedOrEncircled,
    /** Turns off the `OverLined` attribute. */
    NotOverLined;

    companion object {
        /**
         * Iterates over all the variants of the Attribute enum.
         */
        fun iterator(): Iterator<Attribute> = entries.iterator()

        /**
         * Static array containing the SGR code of each attribute.
         */
        val SGR: ShortArray = shortArrayOf(
            0,   // Reset
            1,   // Bold
            2,   // Dim
            3,   // Italic
            4,   // Underlined
            2,   // DoubleUnderlined (4:2)
            3,   // Undercurled (4:3)
            4,   // Underdotted (4:4)
            5,   // Underdashed (4:5)
            5,   // SlowBlink
            6,   // RapidBlink
            7,   // Reverse
            8,   // Hidden
            9,   // CrossedOut
            20,  // Fraktur
            21,  // NoBold
            22,  // NormalIntensity
            23,  // NoItalic
            24,  // NoUnderline
            25,  // NoBlink
            27,  // NoReverse
            28,  // NoHidden
            29,  // NotCrossedOut
            51,  // Framed
            52,  // Encircled
            53,  // OverLined
            54,  // NotFramedOrEncircled
            55   // NotOverLined
        )
    }

    /**
     * Returns a UInt with one bit set, which is the
     * signature of this attribute in the Attributes
     * bitset.
     *
     * The +1 enables storing Reset (whose index is 0)
     * in the bitset Attributes.
     */
    fun bytes(): UInt = 1u shl (ordinal + 1)

    /**
     * Returns the SGR attribute value.
     *
     * See [https://en.wikipedia.org/wiki/ANSI_escape_code#SGR_parameters](https://en.wikipedia.org/wiki/ANSI_escape_code#SGR_parameters)
     */
    fun sgr(): String {
        // Special handling for extended underline styles (indices 5-8)
        // These use the "4:n" format for extended underline styles
        return if (ordinal in 5..8) {
            "4:${SGR[ordinal]}"
        } else {
            SGR[ordinal].toString()
        }
    }
}

/**
 * A command that sets an attribute.
 *
 * See [Attribute] for more info.
 *
 * ## Notes
 *
 * Commands must be executed/queued for execution otherwise they do nothing.
 */
data class SetAttribute(val attribute: Attribute) : Command {
    override fun writeAnsi(writer: Appendable) {
        writer.append("\u001B[${attribute.sgr()}m")
    }
}
