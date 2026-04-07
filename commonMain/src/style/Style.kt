// port-lint: source style.rs
package io.github.kotlinmania.crossterm.style

import io.github.kotlinmania.crossterm.AnsiSupport
import io.github.kotlinmania.crossterm.Command
import io.github.kotlinmania.crossterm.csi
import io.github.kotlinmania.crossterm.executeFmt
import io.github.kotlinmania.crossterm.style.types.Attribute
import io.github.kotlinmania.crossterm.style.types.Color
import io.github.kotlinmania.crossterm.style.types.Colored
import io.github.kotlinmania.crossterm.style.types.Colors
import io.github.kotlinmania.crossterm.style.types.getEnvironmentVariable

/**
 * Creates a [StyledContent].
 *
 * This could be used to style any type by applying colors and text attributes.
 */
fun <D> style(value: D): StyledContent<D> = ContentStyle.new().apply(value)

/**
 * Returns available color count.
 *
 * Notes:
 * This does not always provide a good result.
 */
fun availableColorCount(): UShort {
    // NOTE: The upstream Rust code returns u16::MAX on Windows if ANSI is supported.
    // We cannot reliably detect the target platform from commonMain, so we keep the env-var
    // based fallback which works on Unix and on most modern Windows terminals too.
    val defaultCount: UShort = 8u

    val colorTerm = getEnvironmentVariable("COLORTERM")
    val term = colorTerm ?: getEnvironmentVariable("TERM")

    if (AnsiSupport.supportsAnsi() && term == null && colorTerm == null) {
        // Mirror Rust behavior for terminals that report ANSI but don't expose TERM/COLORTERM.
        return UShort.MAX_VALUE
    }

    val value = term ?: return defaultCount
    return when {
        value.contains("24bit") || value.contains("truecolor") -> UShort.MAX_VALUE
        value.contains("256") -> 256u
        else -> defaultCount
    }
}

/**
 * Forces colored output on or off globally, overriding NO_COLOR.
 */
fun forceColorOutput(enabled: Boolean) {
    Colored.setAnsiColorDisabled(!enabled)
}

/**
 * A command that sets the foreground color.
 */
data class SetForegroundColor(val color: Color) : Command {
    override fun writeAnsi(writer: Appendable) {
        writer.append(csi("${Colored.ForegroundColor(color)}m"))
    }
}

/**
 * A command that sets the background color.
 */
data class SetBackgroundColor(val color: Color) : Command {
    override fun writeAnsi(writer: Appendable) {
        writer.append(csi("${Colored.BackgroundColor(color)}m"))
    }
}

/**
 * A command that sets the underline color.
 */
data class SetUnderlineColor(val color: Color) : Command {
    override fun writeAnsi(writer: Appendable) {
        writer.append(csi("${Colored.UnderlineColor(color)}m"))
    }

    override fun executeWinapi() {
        throw IllegalStateException("SetUnderlineColor not supported by winapi.")
    }
}

/**
 * A command that optionally sets the foreground and/or background color.
 */
data class SetColors(val colors: Colors) : Command {
    override fun writeAnsi(writer: Appendable) {
        val foreground = colors.foreground
        val background = colors.background
        when {
            foreground != null && background != null -> writer.append(
                csi("${Colored.ForegroundColor(foreground)};${Colored.BackgroundColor(background)}m")
            )
            foreground != null -> writer.append(csi("${Colored.ForegroundColor(foreground)}m"))
            background != null -> writer.append(csi("${Colored.BackgroundColor(background)}m"))
            else -> Unit
        }
    }
}

/**
 * A command that sets an attribute.
 */
data class SetAttribute(val attribute: Attribute) : Command {
    override fun writeAnsi(writer: Appendable) {
        writer.append(csi("${attribute.sgr()}m"))
    }
}

/**
 * A command that sets several attributes.
 */
data class SetAttributes(val attributes: Attributes) : Command {
    override fun writeAnsi(writer: Appendable) {
        for (attr in Attribute.iterator()) {
            if (attributes.has(attr)) {
                SetAttribute(attr).writeAnsi(writer)
            }
        }
    }
}

/**
 * A command that sets a style (colors and attributes).
 */
data class SetStyle(val style: ContentStyle) : Command {
    override fun writeAnsi(writer: Appendable) {
        style.backgroundColor?.let { bg ->
            SetBackgroundColor(bg).writeAnsi(writer)
        }
        style.foregroundColor?.let { fg ->
            SetForegroundColor(fg).writeAnsi(writer)
        }
        style.underlineColor?.let { ul ->
            SetUnderlineColor(ul).writeAnsi(writer)
        }
        if (!style.attributes.isEmpty()) {
            SetAttributes(style.attributes).writeAnsi(writer)
        }
    }

    override fun executeWinapi() {
        throw IllegalStateException("tried to execute SetStyle command using WinAPI, use ANSI instead")
    }

    override fun isAnsiCodeSupported(): Boolean = true
}

/**
 * A command that prints styled content.
 */
data class PrintStyledContent<D>(val styledContent: StyledContent<D>) : Command {
    override fun writeAnsi(writer: Appendable) {
        val style = styledContent.style()

        var resetBackground = false
        var resetForeground = false
        var reset = false

        style.backgroundColor?.let { bg ->
            executeFmt(writer, SetBackgroundColor(bg))
            resetBackground = true
        }
        style.foregroundColor?.let { fg ->
            executeFmt(writer, SetForegroundColor(fg))
            resetForeground = true
        }
        style.underlineColor?.let { ul ->
            executeFmt(writer, SetUnderlineColor(ul))
            resetForeground = true
        }

        if (!style.attributes.isEmpty()) {
            executeFmt(writer, SetAttributes(style.attributes))
            reset = true
        }

        writer.append(styledContent.content().toString())

        if (reset) {
            // NOTE: This will reset colors even though self has no colors, hence produce unexpected resets.
            // NOTE: The upstream implementation notes this as a known issue (attributes-only reset).
            executeFmt(writer, ResetColor)
        } else {
            // NOTE: Since the above bug, we do not need to reset colors when we reset attributes.
            if (resetBackground) {
                executeFmt(writer, SetBackgroundColor(Color.Reset))
            }
            if (resetForeground) {
                executeFmt(writer, SetForegroundColor(Color.Reset))
            }
        }
    }
}

/**
 * A command that resets the colors back to default.
 */
data object ResetColor : Command {
    override fun writeAnsi(writer: Appendable) {
        writer.append(csi("0m"))
    }
}

/**
 * A command that prints the given displayable type.
 */
data class Print<D>(val value: D) : Command {
    override fun writeAnsi(writer: Appendable) {
        writer.append(value.toString())
    }

    override fun executeWinapi() {
        throw IllegalStateException("tried to execute Print command using WinAPI, use ANSI instead")
    }

    override fun isAnsiCodeSupported(): Boolean = true
}
