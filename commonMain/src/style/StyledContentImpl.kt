// port-lint: source style/styled_content.rs

package io.github.kotlinmania.crossterm.style

import io.github.kotlinmania.crossterm.executeFmt

/**
 * The style with the content to be styled.
 */
data class StyledContent(
    /** The style (colors, content attributes). */
    private var style: ContentStyle,
    /** The content to apply the style on, stored as its string representation. */
    private val content: String
) {
    companion object {
        /**
         * Creates a new [StyledContent].
         */
        fun new(style: ContentStyle, content: String): StyledContent = StyledContent(style, content)
    }

    /**
     * Returns the content.
     */
    fun content(): String = content

    /**
     * Returns the style.
     */
    fun style(): ContentStyle = style

    /**
     * Returns a mutable reference to the style, so that it can be further manipulated.
     */
    fun styleMut(): ContentStyle = style

    fun asRef(): ContentStyle = style

    fun asMut(): ContentStyle = style

    override fun toString(): String {
        val sb = StringBuilder()
        executeFmt(sb, PrintStyledContent(this))
        return sb.toString()
    }
}