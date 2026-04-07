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

    override fun executeWinapi() {
        io.github.kotlinmania.crossterm.cursor.sys.moveTo(column, row)
    }
}

/**
 * A command that moves the terminal cursor down the given number of lines,
 * and moves it to the first column.
 *
 * Notes:
 * - This command is 1 based, meaning `MoveToNextLine(1)` moves to the next line.
 * - Most terminals default 0 argument to 1.
 */
data class MoveToNextLine(val lines: UShort) : Command {
    override fun writeAnsi(writer: Appendable) {
        writer.append("\u001B[${lines}E")
    }

    override fun executeWinapi() {
        if (lines != 0.toUShort()) {
            io.github.kotlinmania.crossterm.cursor.sys.moveToNextLine(lines)
        }
    }
}

/**
 * A command that moves the terminal cursor up the given number of lines,
 * and moves it to the first column.
 *
 * Notes:
 * - This command is 1 based, meaning `MoveToPreviousLine(1)` moves to the previous line.
 * - Most terminals default 0 argument to 1.
 */
data class MoveToPreviousLine(val lines: UShort) : Command {
    override fun writeAnsi(writer: Appendable) {
        writer.append("\u001B[${lines}F")
    }

    override fun executeWinapi() {
        if (lines != 0.toUShort()) {
            io.github.kotlinmania.crossterm.cursor.sys.moveToPreviousLine(lines)
        }
    }
}

/**
 * A command that moves the cursor to the specified column in the current row.
 */
data class MoveToColumn(val column: UShort) : Command {
    override fun writeAnsi(writer: Appendable) {
        writer.append("\u001B[${column.toInt() + 1}G")
    }

    override fun executeWinapi() {
        io.github.kotlinmania.crossterm.cursor.sys.moveToColumn(column)
    }
}

/**
 * A command that moves the cursor to the specified row in the current column.
 */
data class MoveToRow(val row: UShort) : Command {
    override fun writeAnsi(writer: Appendable) {
        writer.append("\u001B[${row.toInt() + 1}d")
    }

    override fun executeWinapi() {
        io.github.kotlinmania.crossterm.cursor.sys.moveToRow(row)
    }
}

/**
 * A command that moves the cursor up by the specified number of rows.
 */
data class MoveUp(val rows: UShort) : Command {
    override fun writeAnsi(writer: Appendable) {
        writer.append("\u001B[${rows}A")
    }

    override fun executeWinapi() {
        io.github.kotlinmania.crossterm.cursor.sys.moveUp(rows)
    }
}

/**
 * A command that moves the cursor down by the specified number of rows.
 */
data class MoveDown(val rows: UShort) : Command {
    override fun writeAnsi(writer: Appendable) {
        writer.append("\u001B[${rows}B")
    }

    override fun executeWinapi() {
        io.github.kotlinmania.crossterm.cursor.sys.moveDown(rows)
    }
}

/**
 * A command that moves the cursor right by the specified number of columns.
 */
data class MoveRight(val columns: UShort) : Command {
    override fun writeAnsi(writer: Appendable) {
        writer.append("\u001B[${columns}C")
    }

    override fun executeWinapi() {
        io.github.kotlinmania.crossterm.cursor.sys.moveRight(columns)
    }
}

/**
 * A command that moves the cursor left by the specified number of columns.
 */
data class MoveLeft(val columns: UShort) : Command {
    override fun writeAnsi(writer: Appendable) {
        writer.append("\u001B[${columns}D")
    }

    override fun executeWinapi() {
        io.github.kotlinmania.crossterm.cursor.sys.moveLeft(columns)
    }
}

/**
 * A command that saves the current cursor position.
 */
data object SavePosition : Command {
    override fun writeAnsi(writer: Appendable) {
        writer.append("\u001B7")
    }

    override fun executeWinapi() {
        io.github.kotlinmania.crossterm.cursor.sys.savePosition()
    }
}

/**
 * A command that restores a previously saved cursor position.
 */
data object RestorePosition : Command {
    override fun writeAnsi(writer: Appendable) {
        writer.append("\u001B8")
    }

    override fun executeWinapi() {
        io.github.kotlinmania.crossterm.cursor.sys.restorePosition()
    }
}

/**
 * A command that hides the cursor.
 */
data object Hide : Command {
    override fun writeAnsi(writer: Appendable) {
        writer.append("\u001B[?25l")
    }

    override fun executeWinapi() {
        io.github.kotlinmania.crossterm.cursor.sys.showCursor(false)
    }
}

/**
 * A command that shows the cursor.
 */
data object Show : Command {
    override fun writeAnsi(writer: Appendable) {
        writer.append("\u001B[?25h")
    }

    override fun executeWinapi() {
        io.github.kotlinmania.crossterm.cursor.sys.showCursor(true)
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
 * Cursor style.
 */
enum class SetCursorStyle : Command {
    DefaultUserShape,
    BlinkingBlock,
    SteadyBlock,
    BlinkingUnderScore,
    SteadyUnderScore,
    BlinkingBar,
    SteadyBar;

    override fun writeAnsi(writer: Appendable) {
        val code = when (this) {
            DefaultUserShape -> 0
            BlinkingBlock -> 1
            SteadyBlock -> 2
            BlinkingUnderScore -> 3
            SteadyUnderScore -> 4
            BlinkingBar -> 5
            SteadyBar -> 6
        }
        writer.append("\u001B[$code q")
    }
}
