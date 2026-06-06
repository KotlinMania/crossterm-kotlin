// port-lint: source cursor.rs
@file:OptIn(kotlin.experimental.ExperimentalObjCRefinement::class)

package io.github.kotlinmania.crossterm.cursor

import io.github.kotlinmania.crossterm.Command

data class MoveTo(
    val column: UShort,
    val row: UShort,
) : Command {
    override fun writeAnsi(writer: Appendable) {
        writer.append("\u001B[${row.toInt() + 1};${column.toInt() + 1}H")
    }

    override fun executeWinapi() {
        WinapiCursor.moveTo(column, row)
    }
}

data class MoveToNextLine(
    val lines: UShort,
) : Command {
    override fun writeAnsi(writer: Appendable) {
        writer.append("\u001B[${lines}E")
    }

    override fun executeWinapi() {
        if (lines != 0.toUShort()) {
            WinapiCursor.moveToNextLine(lines)
        }
    }
}

data class MoveToPreviousLine(
    val lines: UShort,
) : Command {
    override fun writeAnsi(writer: Appendable) {
        writer.append("\u001B[${lines}F")
    }

    override fun executeWinapi() {
        if (lines != 0.toUShort()) {
            WinapiCursor.moveToPreviousLine(lines)
        }
    }
}

data class MoveToColumn(
    val column: UShort,
) : Command {
    override fun writeAnsi(writer: Appendable) {
        writer.append("\u001B[${column.toInt() + 1}G")
    }

    override fun executeWinapi() {
        WinapiCursor.moveToColumn(column)
    }
}

data class MoveToRow(
    val row: UShort,
) : Command {
    override fun writeAnsi(writer: Appendable) {
        writer.append("\u001B[${row.toInt() + 1}d")
    }

    override fun executeWinapi() {
        WinapiCursor.moveToRow(row)
    }
}

data class MoveUp(
    val rows: UShort,
) : Command {
    override fun writeAnsi(writer: Appendable) {
        writer.append("\u001B[${rows}A")
    }

    override fun executeWinapi() {
        WinapiCursor.moveUp(rows)
    }
}

data class MoveDown(
    val rows: UShort,
) : Command {
    override fun writeAnsi(writer: Appendable) {
        writer.append("\u001B[${rows}B")
    }

    override fun executeWinapi() {
        WinapiCursor.moveDown(rows)
    }
}

data class MoveRight(
    val columns: UShort,
) : Command {
    override fun writeAnsi(writer: Appendable) {
        writer.append("\u001B[${columns}C")
    }

    override fun executeWinapi() {
        WinapiCursor.moveRight(columns)
    }
}

data class MoveLeft(
    val columns: UShort,
) : Command {
    override fun writeAnsi(writer: Appendable) {
        writer.append("\u001B[${columns}D")
    }

    override fun executeWinapi() {
        WinapiCursor.moveLeft(columns)
    }
}

data object SavePosition : Command {
    override fun writeAnsi(writer: Appendable) {
        writer.append("\u001B7")
    }

    override fun executeWinapi() {
        WinapiCursor.savePosition()
    }
}

data object RestorePosition : Command {
    override fun writeAnsi(writer: Appendable) {
        writer.append("\u001B8")
    }

    override fun executeWinapi() {
        WinapiCursor.restorePosition()
    }
}

data object Hide : Command {
    override fun writeAnsi(writer: Appendable) {
        writer.append("\u001B[?25l")
    }

    override fun executeWinapi() {
        WinapiCursor.showCursor(false)
    }
}

data object Show : Command {
    override fun writeAnsi(writer: Appendable) {
        writer.append("\u001B[?25h")
    }

    override fun executeWinapi() {
        WinapiCursor.showCursor(true)
    }
}

data object EnableBlinking : Command {
    override fun writeAnsi(writer: Appendable) {
        writer.append("\u001B[?12h")
    }
}

data object DisableBlinking : Command {
    override fun writeAnsi(writer: Appendable) {
        writer.append("\u001B[?12l")
    }
}

enum class SetCursorStyle : Command {
    DefaultUserShape,
    BlinkingBlock,
    SteadyBlock,
    BlinkingUnderScore,
    SteadyUnderScore,
    BlinkingBar,
    SteadyBar,
    ;

    override fun writeAnsi(writer: Appendable) {
        val code =
            when (this) {
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
