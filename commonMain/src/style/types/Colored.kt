// port-lint: source style/types/colored.rs
package io.github.kotlinmania.crossterm.style.types

import kotlin.concurrent.Volatile

/**
 * Represents a foreground or background color.
 *
 * This can be converted to a [Colors] by calling the appropriate conversion function and applied
 * using the SetColors command.
 */
sealed class Colored : Comparable<Colored> {

    /**
     * A foreground color.
     *
     * @param color The color to use for the foreground
     */
    data class ForegroundColor(val color: Color) : Colored()

    /**
     * A background color.
     *
     * @param color The color to use for the background
     */
    data class BackgroundColor(val color: Color) : Colored()

    /**
     * An underline color.
     *
     * Important: doesn't work on Windows 10 or lower.
     *
     * @param color The color to use for underlines
     */
    data class UnderlineColor(val color: Color) : Colored()

    /**
     * Returns the ordinal value for comparison purposes.
     * Order: ForegroundColor, BackgroundColor, UnderlineColor
     */
    private fun ordinal(): Int = when (this) {
        is ForegroundColor -> 0
        is BackgroundColor -> 1
        is UnderlineColor -> 2
    }

    /**
     * Extracts the contained color from this Colored instance.
     */
    fun extractColor(): Color = when (this) {
        is ForegroundColor -> color
        is BackgroundColor -> color
        is UnderlineColor -> color
    }

    override fun compareTo(other: Colored): Int {
        val ordinalCmp = this.ordinal().compareTo(other.ordinal())
        if (ordinalCmp != 0) return ordinalCmp

        // Compare the inner colors
        return this.extractColor().compareTo(other.extractColor())
    }

    /**
     * Writes the ANSI escape sequence for this colored value to the given [Appendable].
     *
     * If ANSI colors are disabled (via [ansiColorDisabledMemoized]), this function does nothing.
     *
     * @param writer The appendable to write the ANSI sequence to
     */
    fun writeAnsi(writer: Appendable) {
        if (ansiColorDisabledMemoized()) {
            return
        }

        val color: Color

        when (this) {
            is ForegroundColor -> {
                if (this.color == Color.Reset) {
                    writer.append("39")
                    return
                } else {
                    writer.append("38;")
                    color = this.color
                }
            }
            is BackgroundColor -> {
                if (this.color == Color.Reset) {
                    writer.append("49")
                    return
                } else {
                    writer.append("48;")
                    color = this.color
                }
            }
            is UnderlineColor -> {
                if (this.color == Color.Reset) {
                    writer.append("59")
                    return
                } else {
                    writer.append("58;")
                    color = this.color
                }
            }
        }

        when (color) {
            Color.Black -> writer.append("5;0")
            Color.DarkGrey -> writer.append("5;8")
            Color.Red -> writer.append("5;9")
            Color.DarkRed -> writer.append("5;1")
            Color.Green -> writer.append("5;10")
            Color.DarkGreen -> writer.append("5;2")
            Color.Yellow -> writer.append("5;11")
            Color.DarkYellow -> writer.append("5;3")
            Color.Blue -> writer.append("5;12")
            Color.DarkBlue -> writer.append("5;4")
            Color.Magenta -> writer.append("5;13")
            Color.DarkMagenta -> writer.append("5;5")
            Color.Cyan -> writer.append("5;14")
            Color.DarkCyan -> writer.append("5;6")
            Color.White -> writer.append("5;15")
            Color.Grey -> writer.append("5;7")
            is Color.Rgb -> writer.append("2;${color.r};${color.g};${color.b}")
            is Color.AnsiValue -> writer.append("5;${color.value}")
            Color.Reset -> { /* Already handled above */ }
        }
    }

    /**
     * Returns the ANSI escape sequence string for this colored value.
     *
     * If ANSI colors are disabled (via [ansiColorDisabledMemoized]), returns an empty string.
     *
     * @return The ANSI escape sequence string
     */
    override fun toString(): String {
        val sb = StringBuilder()
        writeAnsi(sb)
        return sb.toString()
    }

    companion object {
        /**
         * Memoized lazy value for whether ANSI colors are disabled.
         * Initialized once from the NO_COLOR environment variable.
         */
        private val initialAnsiColorDisabled: Boolean by lazy { ansiColorDisabled() }

        /**
         * Volatile override for the disabled state.
         * When null, uses the lazy-initialized value. When set, overrides it.
         */
        @Volatile
        private var ansiColorDisabledOverride: Boolean? = null

        /**
         * Parse an ANSI foreground or background color.
         *
         * This is the string that would appear within an `ESC [ <str> m` escape sequence, as found in
         * various configuration files.
         *
         * Example:
         * ```kotlin
         * import io.github.kotlinmania.crossterm.style.types.Colored
         * import io.github.kotlinmania.crossterm.style.types.Colored.*
         * import io.github.kotlinmania.crossterm.style.types.Color
         *
         * assert(Colored.parseAnsi("38;5;0") == ForegroundColor(Color.Black))
         * assert(Colored.parseAnsi("38;5;26") == ForegroundColor(Color.AnsiValue(26u)))
         * assert(Colored.parseAnsi("48;2;50;60;70") == BackgroundColor(Color.Rgb(50u, 60u, 70u)))
         * assert(Colored.parseAnsi("49") == BackgroundColor(Color.Reset))
         * assert(Colored.parseAnsi("invalid color") == null)
         * ```
         *
         * Currently, 3/4 bit color values aren't supported so return `null`.
         *
         * @param ansi The ANSI color sequence string
         * @return The parsed [Colored] or `null` if parsing fails
         *
         * See also: [Color.parseAnsi]
         */
        fun parseAnsi(ansi: String): Colored? {
            val values = ansi.split(';').iterator()

            val firstValue = parseNextU8(values) ?: return null

            val output = when (firstValue.toInt()) {
                38 -> Color.parseAnsiIter(values)?.let { ForegroundColor(it) } ?: return null
                48 -> Color.parseAnsiIter(values)?.let { BackgroundColor(it) } ?: return null
                58 -> Color.parseAnsiIter(values)?.let { UnderlineColor(it) } ?: return null

                39 -> ForegroundColor(Color.Reset)
                49 -> BackgroundColor(Color.Reset)
                59 -> UnderlineColor(Color.Reset)

                else -> return null
            }

            // For reset codes (39, 49, 59), ensure there are no trailing values
            if (firstValue.toInt() in listOf(39, 49, 59) && values.hasNext()) {
                return null
            }

            return output
        }

        /**
         * Checks whether ANSI color sequences are disabled by setting of NO_COLOR
         * in environment as per [https://no-color.org/](https://no-color.org/)
         *
         * @return `true` if NO_COLOR environment variable is set and non-empty
         */
        fun ansiColorDisabled(): Boolean {
            val noColor = getEnvironmentVariable("NO_COLOR")
            return noColor != null && noColor.isNotEmpty()
        }

        /**
         * Checks whether ANSI color sequences are disabled, with memoization.
         *
         * The first call initializes the cached value from the environment.
         * Subsequent calls return the cached value (unless overridden via [setAnsiColorDisabled]).
         *
         * @return `true` if ANSI colors are disabled
         */
        fun ansiColorDisabledMemoized(): Boolean {
            return ansiColorDisabledOverride ?: initialAnsiColorDisabled
        }

        /**
         * Sets whether ANSI color sequences are disabled.
         *
         * This overrides the value that was initially read from the environment.
         *
         * @param value `true` to disable ANSI colors, `false` to enable them
         */
        fun setAnsiColorDisabled(value: Boolean) {
            // Force the one-time initializer to run first (mimics Rust behavior)
            @Suppress("UNUSED_VARIABLE")
            val unused = initialAnsiColorDisabled
            ansiColorDisabledOverride = value
        }
    }
}

/**
 * Platform-specific function to get an environment variable.
 *
 * @param name The name of the environment variable
 * @return The value of the environment variable, or `null` if not set
 */
internal expect fun getEnvironmentVariable(name: String): String?
