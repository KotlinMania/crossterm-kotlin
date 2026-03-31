// port-lint: source terminal.rs
package io.github.kotlinmania.crossterm.terminal

import io.github.kotlinmania.crossterm.Command

/**
 * A command that switches to the alternate screen buffer.
 *
 * Use [LeaveAlternateScreen] to return to the main screen buffer.
 *
 * Note: The alternate screen buffer doesn't support scrollback.
 */
data object EnterAlternateScreen : Command {
    override fun writeAnsi(writer: Appendable) {
        writer.append("\u001B[?1049h")
    }
}

/**
 * A command that switches back to the main screen buffer.
 */
data object LeaveAlternateScreen : Command {
    override fun writeAnsi(writer: Appendable) {
        writer.append("\u001B[?1049l")
    }
}

/**
 * A command that enables raw mode.
 *
 * Raw mode disables:
 * - Input line buffering
 * - Echo of input characters
 * - Special character processing (Ctrl-C, etc.)
 *
 * This is typically used for TUI applications.
 */
data object EnableRawMode : Command {
    override fun writeAnsi(writer: Appendable) {
        // Raw mode is typically enabled via termios, not ANSI sequences
        // This is a placeholder - actual implementation is platform-specific
    }
}

/**
 * A command that disables raw mode.
 */
data object DisableRawMode : Command {
    override fun writeAnsi(writer: Appendable) {
        // Placeholder - actual implementation is platform-specific
    }
}

/**
 * Different ways to clear the terminal.
 */
enum class ClearType {
    /** Clear the entire screen */
    All,
    /** Clear everything after the cursor */
    Purge,
    /** Clear from cursor to end of screen */
    FromCursorDown,
    /** Clear from cursor to beginning of screen */
    FromCursorUp,
    /** Clear the current line */
    CurrentLine,
    /** Clear from cursor to end of line */
    UntilNewLine
}

/**
 * A command that clears the terminal screen.
 */
data class Clear(val clearType: ClearType) : Command {
    override fun writeAnsi(writer: Appendable) {
        when (clearType) {
            ClearType.All -> writer.append("\u001B[2J")
            ClearType.Purge -> writer.append("\u001B[3J")
            ClearType.FromCursorDown -> writer.append("\u001B[J")
            ClearType.FromCursorUp -> writer.append("\u001B[1J")
            ClearType.CurrentLine -> writer.append("\u001B[2K")
            ClearType.UntilNewLine -> writer.append("\u001B[K")
        }
    }
}

/**
 * A command that scrolls the terminal up by the specified number of rows.
 */
data class ScrollUp(val rows: UShort) : Command {
    override fun writeAnsi(writer: Appendable) {
        writer.append("\u001B[${rows}S")
    }
}

/**
 * A command that scrolls the terminal down by the specified number of rows.
 */
data class ScrollDown(val rows: UShort) : Command {
    override fun writeAnsi(writer: Appendable) {
        writer.append("\u001B[${rows}T")
    }
}

/**
 * A command that sets the terminal title.
 */
data class SetTitle(val title: String) : Command {
    override fun writeAnsi(writer: Appendable) {
        writer.append("\u001B]0;$title\u0007")
    }
}

/**
 * A command that enables line wrapping.
 */
data object EnableLineWrap : Command {
    override fun writeAnsi(writer: Appendable) {
        writer.append("\u001B[?7h")
    }
}

/**
 * A command that disables line wrapping.
 */
data object DisableLineWrap : Command {
    override fun writeAnsi(writer: Appendable) {
        writer.append("\u001B[?7l")
    }
}

/**
 * A command that begins a synchronized update.
 *
 * When the synchronization mode is enabled, following render calls will keep
 * rendering the last rendered state. The terminal emulator keeps processing
 * incoming text and sequences. When the synchronized update mode is disabled
 * again the renderer may fetch the latest screen buffer state again,
 * effectively avoiding the tearing effect.
 *
 * Use [EndSynchronizedUpdate] to end the synchronized update.
 *
 * Ported from Rust crossterm/src/terminal.rs BeginSynchronizedUpdate.
 */
data object BeginSynchronizedUpdate : Command {
    override fun writeAnsi(writer: Appendable) {
        writer.append("\u001B[?2026h")
    }
}

/**
 * A command that ends a synchronized update.
 *
 * Ported from Rust crossterm/src/terminal.rs EndSynchronizedUpdate.
 */
data object EndSynchronizedUpdate : Command {
    override fun writeAnsi(writer: Appendable) {
        writer.append("\u001B[?2026l")
    }
}

/**
 * Terminal size in columns and rows.
 */
data class TerminalSize(
    val columns: UShort,
    val rows: UShort
)
