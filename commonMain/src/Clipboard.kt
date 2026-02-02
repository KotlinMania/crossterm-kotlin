// port-lint: source clipboard.rs
package io.github.kotlinmania.crossterm

import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * # Clipboard
 *
 * The clipboard module provides functionality to work with a host clipboard.
 *
 * ## Implemented operations:
 *
 * - Copy: [CopyToClipboard]
 */

/**
 * Different clipboard types.
 *
 * Some operating systems and desktop environments support multiple buffers
 * for copy/cut/paste. Their details differ between operating systems.
 * See [clipboard specification](https://specifications.freedesktop.org/clipboard-spec/latest/)
 * for a detailed survey of supported types based on the X window system.
 */
sealed class ClipboardType {
    /**
     * Default clipboard when using Ctrl+C or Ctrl+V.
     */
    data object Clipboard : ClipboardType()

    /**
     * Clipboard on Linux/X/Wayland when using selection and middle mouse button.
     */
    data object Primary : ClipboardType()

    /**
     * Other clipboard type not explicitly supported by crossterm.
     *
     * See [XTerm Control Sequences](https://invisible-island.net/xterm/ctlseqs/ctlseqs.html#h3-Operating-System-Commands)
     * for potential values.
     *
     * Note that support for these in terminal emulators is very limited.
     */
    data class Other(val value: Char) : ClipboardType()

    /**
     * Converts this clipboard type to its OSC 52 character representation.
     */
    fun toChar(): Char = when (this) {
        is Clipboard -> 'c'
        is Primary -> 'p'
        is Other -> value
    }

    companion object {
        /**
         * Creates a [ClipboardType] from a character.
         */
        fun fromChar(c: Char): ClipboardType = when (c) {
            'c' -> Clipboard
            'p' -> Primary
            else -> Other(c)
        }
    }
}

/**
 * A sequence of clipboard types.
 *
 * @property clipboards An ordered list of clipboards which will be the destination for the copied selection.
 *
 * Order matters due to implementations deviating from the
 * [XTerm Control Sequences](https://invisible-island.net/xterm/ctlseqs/ctlseqs.html#h3-Operating-System-Commands)
 * reference. Some terminal emulators may only interpret the first character of this
 * parameter. For differences, see [CopyToClipboard] terminal support documentation.
 */
data class ClipboardSelection(val clipboards: List<ClipboardType>) {
    /**
     * Returns a String corresponding to the "Pc" parameter of the OSC52 sequence.
     */
    fun toOsc52Pc(): String = clipboards.map { it.toChar() }.joinToString("")

    override fun toString(): String = toOsc52Pc()

    companion object {
        /**
         * Parses a string into a [ClipboardSelection].
         *
         * Each character in the string is converted to a [ClipboardType].
         */
        fun fromString(s: String): ClipboardSelection =
            ClipboardSelection(s.map { ClipboardType.fromChar(it) })
    }
}

/**
 * A command that copies to clipboard.
 *
 * This command uses OSC control sequence `Pr = 5 2` (See
 * [XTerm Control Sequences](https://invisible-island.net/xterm/ctlseqs/ctlseqs.html#h3-Operating-System-Commands))
 * to copy data to the terminal host clipboard.
 *
 * This only works if it is enabled on the user's terminal emulator. If a terminal multiplexer
 * is used, the multiplexer must support it, too.
 *
 * Commands must be executed/queued for execution otherwise they do nothing.
 *
 * ## Example
 *
 * ```kotlin
 * import io.github.kotlinmania.crossterm.CopyToClipboard
 * import io.github.kotlinmania.crossterm.execute
 *
 * // Copy foo to clipboard
 * print(execute(CopyToClipboard.toClipboardFrom("foo")))
 * // Copy bar to primary
 * print(execute(CopyToClipboard.toPrimaryFrom("bar")))
 * ```
 *
 * ## Terminal Support
 *
 * The following table shows what destinations are filled by different terminal emulators when
 * asked to copy to different destination sequences.
 *
 * | Terminal (Version)    | dest ''   | dest 'c'  | dest 'p' | dest 'cp'     | dest'pc'      |
 * | --------------------- | --------- | --------- | -------- | ------------- | ------------- |
 * | xterm (397)           | primary   | clipboard | primary  | clipb., prim. | clipb., prim. |
 * | Alacritty (0.15.1)    | clipboard | clipboard | primary  | clipb.        | prim.         |
 * | Wezterm               | clipboard | clipboard | primary  | clipb.        | clipb.        |
 * | Konsole (24.12.3)     | clipboard | clipboard | primary  | clipb., prim. | clipb., prim. |
 * | Kitty (0.40.0)        | clipboard | clipboard | primary  | clipb.        | clipb.        |
 * | foot (1.20.2)         | clipboard | clipboard | primary  | clipb., prim. | clipb., prim. |
 * | tmux (3.5a)           | primary   | clipboard | primary  | clipb., prim. | clipb., prim. |
 *
 * @property content Content to be copied
 * @property destination Sequence of copy destinations. Not all sequences are equally supported
 *                       by terminal emulators.
 */
data class CopyToClipboard(
    val content: ByteArray,
    val destination: ClipboardSelection
) : Command {

    @OptIn(ExperimentalEncodingApi::class)
    override fun writeAnsi(writer: Appendable) {
        val encodedText = Base64.encode(content)
        writer.append("\u001B]52;${destination.toOsc52Pc()};$encodedText\u001B\\")
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CopyToClipboard) return false
        return content.contentEquals(other.content) && destination == other.destination
    }

    override fun hashCode(): Int {
        var result = content.contentHashCode()
        result = 31 * result + destination.hashCode()
        return result
    }

    companion object {
        /**
         * Construct a [CopyToClipboard] that writes content into the
         * "clipboard" (or 'c') clipboard selection.
         *
         * ## Example
         *
         * ```kotlin
         * import io.github.kotlinmania.crossterm.CopyToClipboard
         * import io.github.kotlinmania.crossterm.execute
         *
         * print(execute(CopyToClipboard.toClipboardFrom("foo")))
         * ```
         */
        fun toClipboardFrom(content: String): CopyToClipboard =
            CopyToClipboard(
                content = content.encodeToByteArray(),
                destination = ClipboardSelection(listOf(ClipboardType.Clipboard))
            )

        /**
         * Construct a [CopyToClipboard] that writes content into the
         * "clipboard" (or 'c') clipboard selection.
         *
         * ## Example
         *
         * ```kotlin
         * import io.github.kotlinmania.crossterm.CopyToClipboard
         * import io.github.kotlinmania.crossterm.execute
         *
         * print(execute(CopyToClipboard.toClipboardFrom(byteArrayOf(0x66, 0x6f, 0x6f))))
         * ```
         */
        fun toClipboardFrom(content: ByteArray): CopyToClipboard =
            CopyToClipboard(
                content = content,
                destination = ClipboardSelection(listOf(ClipboardType.Clipboard))
            )

        /**
         * Construct a [CopyToClipboard] that writes content into the "primary"
         * (or 'p') clipboard selection.
         *
         * ## Example
         *
         * ```kotlin
         * import io.github.kotlinmania.crossterm.CopyToClipboard
         * import io.github.kotlinmania.crossterm.execute
         *
         * print(execute(CopyToClipboard.toPrimaryFrom("foo")))
         * ```
         */
        fun toPrimaryFrom(content: String): CopyToClipboard =
            CopyToClipboard(
                content = content.encodeToByteArray(),
                destination = ClipboardSelection(listOf(ClipboardType.Primary))
            )

        /**
         * Construct a [CopyToClipboard] that writes content into the "primary"
         * (or 'p') clipboard selection.
         *
         * ## Example
         *
         * ```kotlin
         * import io.github.kotlinmania.crossterm.CopyToClipboard
         * import io.github.kotlinmania.crossterm.execute
         *
         * print(execute(CopyToClipboard.toPrimaryFrom(byteArrayOf(0x66, 0x6f, 0x6f))))
         * ```
         */
        fun toPrimaryFrom(content: ByteArray): CopyToClipboard =
            CopyToClipboard(
                content = content,
                destination = ClipboardSelection(listOf(ClipboardType.Primary))
            )
    }
}
