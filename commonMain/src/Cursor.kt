// port-lint: source cursor.rs
package io.github.kotlinmania.crossterm.cursor

import io.github.kotlinmania.crossterm.Command

/**
 * A command that moves the cursor to the specified position.
 *
 * @param column The column (0-indexed)
 * @param row The row (0-indexed)
 */
data class MoveTo(val column: UShort, val row: UShort) : Command {
    override fun writeAnsi(writer: Appendable) {
        // ANSI uses 1-indexed positions
        writer.append("\u001B[${row.toInt() + 1};${column.toInt() + 1}H")
    }
}

/**
 * A command that moves the cursor to the specified column in the current row.
 */
data class MoveToColumn(val column: UShort) : Command {
    override fun writeAnsi(writer: Appendable) {
        writer.append("\u001B[${column.toInt() + 1}G")
    }
}

/**
 * A command that moves the cursor to the specified row in the current column.
 */
data class MoveToRow(val row: UShort) : Command {
    override fun writeAnsi(writer: Appendable) {
        writer.append("\u001B[${row.toInt() + 1}d")
    }
}

/**
 * A command that moves the cursor up by the specified number of rows.
 */
data class MoveUp(val rows: UShort) : Command {
    override fun writeAnsi(writer: Appendable) {
        writer.append("\u001B[${rows}A")
    }
}

/**
 * A command that moves the cursor down by the specified number of rows.
 */
data class MoveDown(val rows: UShort) : Command {
    override fun writeAnsi(writer: Appendable) {
        writer.append("\u001B[${rows}B")
    }
}

/**
 * A command that moves the cursor right by the specified number of columns.
 */
data class MoveRight(val columns: UShort) : Command {
    override fun writeAnsi(writer: Appendable) {
        writer.append("\u001B[${columns}C")
    }
}

/**
 * A command that moves the cursor left by the specified number of columns.
 */
data class MoveLeft(val columns: UShort) : Command {
    override fun writeAnsi(writer: Appendable) {
        writer.append("\u001B[${columns}D")
    }
}

/**
 * A command that saves the current cursor position.
 */
data object SavePosition : Command {
    override fun writeAnsi(writer: Appendable) {
        writer.append("\u001B[s")
    }
}

/**
 * A command that restores a previously saved cursor position.
 */
data object RestorePosition : Command {
    override fun writeAnsi(writer: Appendable) {
        writer.append("\u001B[u")
    }
}

/**
 * A command that hides the cursor.
 */
data object Hide : Command {
    override fun writeAnsi(writer: Appendable) {
        writer.append("\u001B[?25l")
    }
}

/**
 * A command that shows the cursor.
 */
data object Show : Command {
    override fun writeAnsi(writer: Appendable) {
        writer.append("\u001B[?25h")
    }
}

/**
 * A command that enables cursor blinking.
 */
data object EnableBlinking : Command {
    override fun writeAnsi(writer: Appendable) {
        writer.append("\u001B[?12h")
    }
}

/**
 * A command that disables cursor blinking.
 */
data object DisableBlinking : Command {
    override fun writeAnsi(writer: Appendable) {
        writer.append("\u001B[?12l")
    }
}

/**
 * Cursor shape.
 */
enum class CursorShape {
    /** Default cursor shape configured by the user */
    Default,
    /** A blinking block cursor: █ */
    BlinkingBlock,
    /** A non-blinking block cursor: █ */
    SteadyBlock,
    /** A blinking underline cursor: _ */
    BlinkingUnderScore,
    /** A non-blinking underline cursor: _ */
    SteadyUnderScore,
    /** A blinking bar cursor: | */
    BlinkingBar,
    /** A non-blinking bar cursor: | */
    SteadyBar
}

/**
 * A command that sets the cursor shape.
 */
data class SetCursorShape(val shape: CursorShape) : Command {
    override fun writeAnsi(writer: Appendable) {
        val code = when (shape) {
            CursorShape.Default -> 0
            CursorShape.BlinkingBlock -> 1
            CursorShape.SteadyBlock -> 2
            CursorShape.BlinkingUnderScore -> 3
            CursorShape.SteadyUnderScore -> 4
            CursorShape.BlinkingBar -> 5
            CursorShape.SteadyBar -> 6
        }
        writer.append("\u001B[$code q")
    }
}
