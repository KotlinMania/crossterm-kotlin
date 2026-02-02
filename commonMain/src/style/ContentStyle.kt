// port-lint: source style/content_style.rs
package io.github.kotlinmania.crossterm.style

/**
 * The style that can be put on content.
 *
 * This class contains all the style information that can be applied to text content,
 * including foreground color, background color, underline color, and text attributes.
 *
 * Example:
 * ```kotlin
 * import io.github.kotlinmania.crossterm.style.ContentStyle
 * import io.github.kotlinmania.crossterm.style.Color
 * import io.github.kotlinmania.crossterm.style.Attribute
 * import io.github.kotlinmania.crossterm.style.Attributes
 *
 * // Create a content style with colors and attributes
 * val style = ContentStyle(
 *     foregroundColor = Color.Yellow,
 *     backgroundColor = Color.Blue,
 *     attributes = Attributes.from(Attribute.Bold)
 * )
 *
 * // Apply the style to content
 * val styledContent = style.apply("Hello, World!")
 * ```
 *
 * @property foregroundColor The foreground color.
 * @property backgroundColor The background color.
 * @property underlineColor The underline color.
 * @property attributes List of attributes.
 */
data class ContentStyle(
    /** The foreground color. */
    val foregroundColor: Color? = null,
    /** The background color. */
    val backgroundColor: Color? = null,
    /** The underline color. */
    val underlineColor: Color? = null,
    /** List of attributes. */
    val attributes: Attributes = Attributes.default()
) {

    /**
     * Creates a [StyledContent] by applying the style to the given [value].
     *
     * @param value The content to apply the style to.
     * @return A [StyledContent] containing this style and the given content.
     */
    fun <D> apply(value: D): StyledContent<D> = StyledContent(this, value)

    companion object {
        /**
         * Creates a new [ContentStyle] with default values.
         *
         * @return A new [ContentStyle] with no colors and no attributes.
         */
        fun new(): ContentStyle = ContentStyle()

        /**
         * Returns the default [ContentStyle].
         *
         * @return A new [ContentStyle] with no colors and no attributes.
         */
        fun default(): ContentStyle = ContentStyle()
    }
}

/**
 * The style with the content to be styled.
 *
 * This class combines a [ContentStyle] with content of type [D], allowing the
 * style to be applied when the content is rendered.
 *
 * Example:
 * ```kotlin
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
 *
 * val styledContent = StyledContent(style, "Hello there")
 * println(styledContent.content()) // "Hello there"
 * ```
 *
 * @param D The type of the content to be styled.
 * @property style The style (colors, content attributes).
 * @property content The content to apply the style on.
 */
data class StyledContent<D>(
    /** The style (colors, content attributes). */
    private val style: ContentStyle,
    /** A content to apply the style on. */
    private val content: D
) {

    /**
     * Returns the content.
     *
     * @return The content without any style information.
     */
    fun content(): D = content

    /**
     * Returns the style.
     *
     * @return The [ContentStyle] applied to this content.
     */
    fun style(): ContentStyle = style

    /**
     * Returns a copy with a modified style.
     *
     * @param transform A function that takes the current style and returns a modified style.
     * @return A new [StyledContent] with the transformed style.
     */
    fun withStyle(transform: (ContentStyle) -> ContentStyle): StyledContent<D> =
        StyledContent(transform(style), content)
}
