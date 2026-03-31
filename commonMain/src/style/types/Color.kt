// port-lint: source style/types/color.rs
package io.github.kotlinmania.crossterm.style.types

/**
 * Represents a color.
 *
 * ## Platform-specific Notes
 *
 * The following list of 16 base colors are available for almost all terminals (Windows 7 and 8 included).
 *
 * | Light      | Dark          |
 * | :--------- | :------------ |
 * | `DarkGrey` | `Black`       |
 * | `Red`      | `DarkRed`     |
 * | `Green`    | `DarkGreen`   |
 * | `Yellow`   | `DarkYellow`  |
 * | `Blue`     | `DarkBlue`    |
 * | `Magenta`  | `DarkMagenta` |
 * | `Cyan`     | `DarkCyan`    |
 * | `White`    | `Grey`        |
 *
 * Most UNIX terminals and Windows 10 consoles support additional colors.
 * See [Rgb] or [AnsiValue] for more info.
 */
sealed class Color : Comparable<Color> {

    /** Resets the terminal color. */
    data object Reset : Color()

    /** Black color. */
    data object Black : Color()

    /** Dark grey color. */
    data object DarkGrey : Color()

    /** Light red color. */
    data object Red : Color()

    /** Dark red color. */
    data object DarkRed : Color()

    /** Light green color. */
    data object Green : Color()

    /** Dark green color. */
    data object DarkGreen : Color()

    /** Light yellow color. */
    data object Yellow : Color()

    /** Dark yellow color. */
    data object DarkYellow : Color()

    /** Light blue color. */
    data object Blue : Color()

    /** Dark blue color. */
    data object DarkBlue : Color()

    /** Light magenta color. */
    data object Magenta : Color()

    /** Dark magenta color. */
    data object DarkMagenta : Color()

    /** Light cyan color. */
    data object Cyan : Color()

    /** Dark cyan color. */
    data object DarkCyan : Color()

    /** White color. */
    data object White : Color()

    /** Grey color. */
    data object Grey : Color()

    /**
     * An RGB color. See [RGB color model](https://en.wikipedia.org/wiki/RGB_color_model) for more info.
     *
     * Most UNIX terminals and Windows 10 supported only.
     * See [Color] for platform-specific notes.
     *
     * @param r Red component (0-255)
     * @param g Green component (0-255)
     * @param b Blue component (0-255)
     */
    data class Rgb(val r: UByte, val g: UByte, val b: UByte) : Color()

    /**
     * An ANSI color. See [256 colors - cheat sheet](https://jonasjacek.github.io/colors/) for more info.
     *
     * Most UNIX terminals and Windows 10 supported only.
     * See [Color] for platform-specific notes.
     *
     * @param value The ANSI color value (0-255)
     */
    data class AnsiValue(val value: UByte) : Color()

    /**
     * Returns the ordinal value for comparison purposes.
     * Order: Reset, Black, DarkGrey, Red, DarkRed, Green, DarkGreen, Yellow, DarkYellow,
     *        Blue, DarkBlue, Magenta, DarkMagenta, Cyan, DarkCyan, White, Grey, Rgb, AnsiValue
     */
    private fun ordinal(): Int = when (this) {
        Reset -> 0
        Black -> 1
        DarkGrey -> 2
        Red -> 3
        DarkRed -> 4
        Green -> 5
        DarkGreen -> 6
        Yellow -> 7
        DarkYellow -> 8
        Blue -> 9
        DarkBlue -> 10
        Magenta -> 11
        DarkMagenta -> 12
        Cyan -> 13
        DarkCyan -> 14
        White -> 15
        Grey -> 16
        is Rgb -> 17
        is AnsiValue -> 18
    }

    override fun compareTo(other: Color): Int {
        val ordinalCmp = this.ordinal().compareTo(other.ordinal())
        if (ordinalCmp != 0) return ordinalCmp

        return when {
            this is Rgb && other is Rgb -> {
                val rCmp = this.r.compareTo(other.r)
                if (rCmp != 0) return rCmp
                val gCmp = this.g.compareTo(other.g)
                if (gCmp != 0) return gCmp
                this.b.compareTo(other.b)
            }
            this is AnsiValue && other is AnsiValue -> this.value.compareTo(other.value)
            else -> 0
        }
    }

    companion object {
        /**
         * Parses an ANSI color sequence.
         *
         * Example:
         * ```kotlin
         * import io.github.kotlinmania.crossterm.style.types.Color
         *
         * assert(Color.parseAnsi("5;0") == Color.Black)
         * assert(Color.parseAnsi("5;26") == Color.AnsiValue(26u))
         * assert(Color.parseAnsi("2;50;60;70") == Color.Rgb(50u, 60u, 70u))
         * assert(Color.parseAnsi("invalid color") == null)
         * ```
         *
         * Currently, 3/4 bit color values aren't supported so return `null`.
         *
         * @param ansi The ANSI color sequence string
         * @return The parsed [Color] or `null` if parsing fails
         *
         * See also: [Colored.parseAnsi]
         */
        fun parseAnsi(ansi: String): Color? {
            return parseAnsiIter(ansi.split(';').iterator())
        }

        /**
         * The logic for parseAnsi, takes an iterator of the sequences terms (the numbers between the
         * ';'). It's a separate function so it can be used by both Color.parseAnsi and
         * Colored.parseAnsi.
         *
         * Tested in Colored tests.
         */
        internal fun parseAnsiIter(values: Iterator<String>): Color? {
            val firstValue = parseNextU8(values) ?: return null

            val color = when (firstValue.toInt()) {
                // 8 bit colors: `5;<n>`
                5 -> {
                    val n = parseNextU8(values) ?: return null
                    val basicColors = listOf(
                        Black,       // 0
                        DarkRed,     // 1
                        DarkGreen,   // 2
                        DarkYellow,  // 3
                        DarkBlue,    // 4
                        DarkMagenta, // 5
                        DarkCyan,    // 6
                        Grey,        // 7
                        DarkGrey,    // 8
                        Red,         // 9
                        Green,       // 10
                        Yellow,      // 11
                        Blue,        // 12
                        Magenta,     // 13
                        Cyan,        // 14
                        White,       // 15
                    )
                    basicColors.getOrNull(n.toInt()) ?: AnsiValue(n)
                }

                // 24 bit colors: `2;<r>;<g>;<b>`
                2 -> {
                    val r = parseNextU8(values) ?: return null
                    val g = parseNextU8(values) ?: return null
                    val b = parseNextU8(values) ?: return null
                    Rgb(r, g, b)
                }

                else -> return null
            }

            // If there's another value, it's unexpected so return null.
            if (values.hasNext()) {
                return null
            }

            return color
        }

        /**
         * Try to create a [Color] from the string representation.
         *
         * @param src The color name (case-insensitive)
         * @return The parsed [Color] or `null` if the string does not match any known color
         */
        fun tryFrom(src: String): Color? {
            return when (src.lowercase()) {
                "reset" -> Reset
                "black" -> Black
                "dark_grey" -> DarkGrey
                "red" -> Red
                "dark_red" -> DarkRed
                "green" -> Green
                "dark_green" -> DarkGreen
                "yellow" -> Yellow
                "dark_yellow" -> DarkYellow
                "blue" -> Blue
                "dark_blue" -> DarkBlue
                "magenta" -> Magenta
                "dark_magenta" -> DarkMagenta
                "cyan" -> Cyan
                "dark_cyan" -> DarkCyan
                "white" -> White
                "grey" -> Grey
                else -> null
            }
        }

        /**
         * Creates a [Color] from the string representation.
         *
         * Note: Returns [Color.White] in case of an unknown color.
         *
         * @param src The color name (case-insensitive)
         * @return The parsed [Color], defaults to [Color.White] if unknown
         */
        fun fromString(src: String): Color {
            return tryFrom(src) ?: White
        }

        /**
         * Creates an [Rgb] color from the tuple representation.
         *
         * @param r Red component (0-255)
         * @param g Green component (0-255)
         * @param b Blue component (0-255)
         * @return An [Rgb] color
         */
        fun fromRgb(r: UByte, g: UByte, b: UByte): Color = Rgb(r, g, b)

        /**
         * Creates an [Rgb] color from a triple.
         *
         * @param rgb A triple of (r, g, b) values
         * @return An [Rgb] color
         */
        fun from(rgb: Triple<UByte, UByte, UByte>): Color = Rgb(rgb.first, rgb.second, rgb.third)
    }
}

/**
 * Parses the next value from the iterator as a UByte.
 *
 * @param iter An iterator of string values
 * @return The parsed [UByte] or `null` if parsing fails or iterator is empty
 */
internal fun parseNextU8(iter: Iterator<String>): UByte? {
    if (!iter.hasNext()) return null
    val value = iter.next().toIntOrNull() ?: return null
    if (value < 0 || value > 255) return null
    return value.toUByte()
}
