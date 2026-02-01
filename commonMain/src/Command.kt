// port-lint: source command.rs
package io.github.kotlinmania.crossterm

/**
 * A command that can be executed on the terminal.
 *
 * Commands represent terminal operations that can be serialized to ANSI escape sequences.
 */
interface Command {
    /**
     * Writes the ANSI escape sequence for this command to the given writer.
     */
    fun writeAnsi(writer: Appendable)

    /**
     * Returns the ANSI escape sequence as a string.
     */
    fun ansiString(): String = buildString { writeAnsi(this) }
}

/**
 * Executes multiple commands in sequence.
 */
fun execute(vararg commands: Command): String = buildString {
    for (command in commands) {
        command.writeAnsi(this)
    }
}
