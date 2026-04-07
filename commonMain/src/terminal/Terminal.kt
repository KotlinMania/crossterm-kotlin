// port-lint: source terminal.rs
package io.github.kotlinmania.crossterm.terminal

import io.github.kotlinmania.crossterm.Command
import io.github.kotlinmania.crossterm.csi

/**
 * Tells whether the raw mode is enabled.
 */
fun isRawModeEnabled(): Boolean = io.github.kotlinmania.crossterm.terminal.sys.isRawModeEnabled()

/**
 * Enables raw mode.
 */
fun enableRawMode() = io.github.kotlinmania.crossterm.terminal.sys.enableRawMode()

/**
 * Disables raw mode.
 */
fun disableRawMode() = io.github.kotlinmania.crossterm.terminal.sys.disableRawMode()

/**
 * Returns the terminal size `(columns, rows)`.
 *
 * The top left cell is represented `(1, 1)`.
 */
fun size(): Pair<UShort, UShort> = io.github.kotlinmania.crossterm.terminal.sys.size()

data class WindowSize(
    val rows: UShort,
    val columns: UShort,
    val width: UShort,
    val height: UShort
)

/**
 * Returns the terminal size [WindowSize].
 */
fun windowSize(): WindowSize = io.github.kotlinmania.crossterm.terminal.sys.windowSize()

/**
 * Queries the terminal's support for progressive keyboard enhancement.
 */
fun supportsKeyboardEnhancement(): Boolean =
    io.github.kotlinmania.crossterm.terminal.sys.supportsKeyboardEnhancement()

/**
 * Disables line wrapping.
 */
data object DisableLineWrap : Command {
    override fun writeAnsi(writer: Appendable) {
        writer.append(csi("?7l"))
    }
}

/**
 * Enable line wrapping.
 */
data object EnableLineWrap : Command {
    override fun writeAnsi(writer: Appendable) {
        writer.append(csi("?7h"))
    }
}

/**
 * A command that switches to alternate screen.
 */
data object EnterAlternateScreen : Command {
    override fun writeAnsi(writer: Appendable) {
        writer.append(csi("?1049h"))
    }
}

/**
 * A command that switches back to the main screen.
 */
data object LeaveAlternateScreen : Command {
    override fun writeAnsi(writer: Appendable) {
        writer.append(csi("?1049l"))
    }
}

/**
 * Different ways to clear the terminal buffer.
 */
enum class ClearType {
    /** All cells. */
    All,
    /** All plus history. */
    Purge,
    /** All cells from the cursor position downwards. */
    FromCursorDown,
    /** All cells from the cursor position upwards. */
    FromCursorUp,
    /** All cells at the cursor row. */
    CurrentLine,
    /** All cells from the cursor position until the new line. */
    UntilNewLine,
}

/**
 * A command that scrolls the terminal screen a given number of rows up.
 */
data class ScrollUp(val rows: UShort) : Command {
    override fun writeAnsi(writer: Appendable) {
        if (rows != 0.toUShort()) {
            writer.append(csi("${rows}S"))
        }
    }
}

/**
 * A command that scrolls the terminal screen a given number of rows down.
 */
data class ScrollDown(val rows: UShort) : Command {
    override fun writeAnsi(writer: Appendable) {
        if (rows != 0.toUShort()) {
            writer.append(csi("${rows}T"))
        }
    }
}

/**
 * A command that clears the terminal screen buffer.
 *
 * See the [ClearType] enum.
 */
data class Clear(val clearType: ClearType) : Command {
    override fun writeAnsi(writer: Appendable) {
        writer.append(
            when (clearType) {
                ClearType.All -> csi("2J")
                ClearType.Purge -> csi("3J")
                ClearType.FromCursorDown -> csi("J")
                ClearType.FromCursorUp -> csi("1J")
                ClearType.CurrentLine -> csi("2K")
                ClearType.UntilNewLine -> csi("K")
            }
        )
    }
}

/**
 * A command that sets the terminal buffer size `(columns, rows)`.
 */
data class SetSize(val columns: UShort, val rows: UShort) : Command {
    override fun writeAnsi(writer: Appendable) {
        writer.append(csi("8;${rows};${columns}t"))
    }
}

/**
 * A command that sets the terminal title.
 */
data class SetTitle(val title: String) : Command {
    override fun writeAnsi(writer: Appendable) {
        writer.append("\u001B]0;")
        writer.append(title)
        writer.append("\u0007")
    }
}

/**
 * A command that instructs the terminal emulator to begin a synchronized frame.
 */
data object BeginSynchronizedUpdate : Command {
    override fun writeAnsi(writer: Appendable) {
        writer.append(csi("?2026h"))
    }
}

/**
 * A command that instructs the terminal emulator to end a synchronized frame.
 */
data object EndSynchronizedUpdate : Command {
    override fun writeAnsi(writer: Appendable) {
        writer.append(csi("?2026l"))
    }
}

