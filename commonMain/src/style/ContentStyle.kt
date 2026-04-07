// port-lint: source style/content_style.rs
package io.github.kotlinmania.crossterm.style

import io.github.kotlinmania.crossterm.style.types.Color

/**
 * The style that can be put on content.
 */
data class ContentStyle(
    /** The foreground color. */
    var foregroundColor: Color? = null,
    /** The background color. */
    var backgroundColor: Color? = null,
    /** The underline color. */
    var underlineColor: Color? = null,
    /** List of attributes. */
    var attributes: Attributes = Attributes.default()
) {
    /**
     * Creates a [StyledContent] by applying the style to the given [value].
     */
    fun <D> apply(value: D): StyledContent<D> = StyledContent.new(this, value)

    companion object {
        /**
         * Creates a new [ContentStyle].
         */
        fun new(): ContentStyle = ContentStyle()
    }
}
