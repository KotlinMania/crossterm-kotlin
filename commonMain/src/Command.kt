// port-lint: source command.rs
package io.github.kotlinmania.crossterm

import io.github.kotlinmania.crossterm.terminal.BeginSynchronizedUpdate
import io.github.kotlinmania.crossterm.terminal.EndSynchronizedUpdate

/**
 * An interface for a command that performs an action on the terminal.
 *
 * Crossterm provides a set of commands,
 * and there is no immediate reason to implement a command yourself.
 * In order to understand how to use and execute commands,
 * it is recommended that you take a look at the Command API chapter.
 *
 * Ported from Rust crossterm `src/command.rs` `Command` trait.
 */
interface Command {
    /**
     * Write an ANSI representation of this command to the given writer.
     * An ANSI code can manipulate the terminal by writing it to the terminal buffer.
     * However, only Windows 10 and UNIX systems support this.
     *
     * This method does not need to be accessed manually, as it is used by the crossterm's Command API.
     */
    fun writeAnsi(writer: Appendable)

    /**
     * Execute this command using WinAPI calls (Windows-only behavior).
     *
     * Ported from Rust crossterm `src/command.rs` `Command::execute_winapi` (behind `#[cfg(windows)]`).
     *
     * In this Kotlin port the default behavior is a no-op, because most commands are executed as ANSI
     * strings. Platform-specific commands can override this where needed.
     */
    fun executeWinapi() {
        // no-op
    }

    /**
     * Returns whether this command's ANSI representation is supported on Windows.
     *
     * Ported from Rust crossterm `src/command.rs` `Command::is_ansi_code_supported` (behind `#[cfg(windows)]`).
     */
    fun isAnsiCodeSupported(): Boolean = AnsiSupport.supportsAnsi()
}

/**
 * An interface for types that can queue commands for further execution.
 *
 * Ported from Rust crossterm `src/command.rs` `QueueableCommand` trait.
 */
interface QueueableCommand : Appendable {
    /**
     * Queues the given command for further execution.
     *
     * Queued commands will be executed when [flush] is called.
     */
    fun queue(command: Command): QueueableCommand {
        if (!command.isAnsiCodeSupported()) {
            flush()
            command.executeWinapi()
            return this
        }

        writeCommandAnsi(this, command)
        return this
    }

    /**
     * Flushes any queued commands.
     */
    fun flush()
}

/**
 * An interface for types that can directly execute commands.
 *
 * Ported from Rust crossterm `src/command.rs` `ExecutableCommand` trait.
 */
interface ExecutableCommand : QueueableCommand {
    /**
     * Executes the given command directly.
     *
     * The given command's ANSI escape code will be written and flushed.
     */
    fun execute(command: Command): ExecutableCommand {
        queue(command)
        flush()
        return this
    }
}

/**
 * An interface for types that support synchronized updates.
 *
 * Ported from Rust crossterm `src/command.rs` `SynchronizedUpdate` trait.
 */
interface SynchronizedUpdate : ExecutableCommand {
    /**
     * Performs a set of actions within a synchronous update.
     *
     * Updates will be suspended in the terminal, the function will be executed against self,
     * updates will be resumed, and a flush will be performed.
     *
     * When rendering the screen of the terminal, the Emulator usually iterates through each
     * visible grid cell and renders its current state. With applications updating the screen
     * at a higher frequency this can cause tearing.
     *
     * This mode attempts to mitigate that.
     */
    fun <T> syncUpdate(operations: (SynchronizedUpdate) -> T): T {
        queue(BeginSynchronizedUpdate)
        val result = operations(this)
        execute(EndSynchronizedUpdate)
        return result
    }
}

/**
 * Writes the ANSI representation of a command to the given writer.
 *
 * Ported from Rust crossterm `src/command.rs` `write_command_ansi`.
 */
fun writeCommandAnsi(writer: Appendable, command: Command) {
    command.writeAnsi(writer)
}

/**
 * Executes the ANSI representation of a command, using the given [Appendable].
 *
 * Ported from Rust crossterm `src/command.rs` `execute_fmt`.
 */
fun executeFmt(f: Appendable, command: Command) {
    if (!command.isAnsiCodeSupported()) {
        command.executeWinapi()
        return
    }

    command.writeAnsi(f)
}

/**
 * Returns the ANSI escape sequence for this command as a string.
 */
fun Command.ansiString(): String = buildString { writeAnsi(this) }

/**
 * Executes multiple commands in sequence and returns the combined ANSI string.
 *
 * This is the Kotlin analogue of Rust's `execute!` macro when targeting ANSI output.
 */
fun execute(vararg commands: Command): String = buildString {
    for (command in commands) {
        command.writeAnsi(this)
    }
}
