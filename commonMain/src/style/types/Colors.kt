// port-lint: source style/types/colors.rs
package io.github.kotlinmania.crossterm.style.types

/**
 * Represents, optionally, a foreground and/or a background color.
 *
 * It can be applied using the `SetColors` command.
 *
 * It can also be created from a [Colored] value or a tuple of
 * `(Color, Color)` in the order `(foreground, background)`.
 *
 * The [then] method can be used to combine `Colors` values.
 *
 * Example:
 * ```kotlin
 * import io.github.kotlinmania.crossterm.style.types.Color
 * import io.github.kotlinmania.crossterm.style.types.Colors
 * import io.github.kotlinmania.crossterm.style.types.Colored
 *
 * // An example color, loaded from a config, file in ANSI format.
 * val configColor = "38;2;23;147;209"
 *
 * // Default to green text on a black background.
 * val defaultColors = Colors.new(Color.Green, Color.Black)
 * // Load a colored value from a config and override the default colors
 * val colors = Colored.parseAnsi(configColor)?.let { colored ->
 *     defaultColors.then(Colors.from(colored))
 * } ?: defaultColors
 * ```
 *
 * @property foreground The optional foreground color
 * @property background The optional background color
 *
 * @see Color
 */
data class Colors(
    val foreground: Color?,
    val background: Color?
) {
    /**
     * Returns a new [Colors] which, when applied, has the same effect as applying `this` and *then*
     * [other].
     *
     * @param other The colors to apply after this one
     * @return A new [Colors] combining both, with [other] taking precedence where specified
     */
    fun then(other: Colors): Colors {
        return Colors(
            foreground = other.foreground ?: this.foreground,
            background = other.background ?: this.background
        )
    }

    companion object {
        /**
         * Creates a new [Colors] with both foreground and background set.
         *
         * @param foreground The foreground color
         * @param background The background color
         * @return A new [Colors] with both colors set
         */
        fun new(foreground: Color, background: Color): Colors {
            return Colors(
                foreground = foreground,
                background = background
            )
        }

        /**
         * Creates a [Colors] from a [Colored] value.
         *
         * @param colored The [Colored] value to convert
         * @return A [Colors] with either foreground or background set based on the [Colored] type
         */
        fun from(colored: Colored): Colors {
            return when (colored) {
                is Colored.ForegroundColor -> Colors(
                    foreground = colored.color,
                    background = null
                )
                is Colored.BackgroundColor -> Colors(
                    foreground = null,
                    background = colored.color
                )
                is Colored.UnderlineColor -> Colors(
                    foreground = null,
                    background = colored.color
                )
            }
        }

        /**
         * Creates a [Colors] from a pair of colors in the order (foreground, background).
         *
         * @param colors A pair of (foreground, background) colors
         * @return A [Colors] with both colors set
         */
        fun from(colors: Pair<Color, Color>): Colors {
            return Colors(
                foreground = colors.first,
                background = colors.second
            )
        }
    }
}
