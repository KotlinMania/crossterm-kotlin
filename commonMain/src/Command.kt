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
 */
interface Command {
    /**
     * Write an ANSI representation of this command to the given writer.
     * An ANSI code can manipulate the terminal by writing it to the terminal buffer.
     * However, only Windows 10 and UNIX systems support this.
     *
     * This method does not need to be accessed manually, as it is used by the crossterm's
     * Command API.
     */
    fun writeAnsi(writer: Appendable)

    /**
     * Execute this command.
     *
     * Windows versions lower than windows 10 do not support ANSI escape codes,
     * therefore a direct WinAPI call is made.
     *
     * This method does not need to be accessed manually, as it is used by the crossterm's
     * Command API.
     */
    fun executeWinapi() {
        // no-op
    }

    /**
     * Returns whether the ANSI code representation of this command is supported by windows.
     *
     * A list of supported ANSI escape codes
     * can be found in Microsoft's "Console Virtual Terminal Sequences" reference.
     */
    fun isAnsiCodeSupported(): Boolean = AnsiSupport.supportsAnsi()
}

/**
 * An interface for types that can queue commands for further execution.
 */
interface QueueableCommand : Appendable {
    /**
     * Queues the given command for further execution.
     *
     * Queued commands will be executed in the following cases:
     *
     * * When `flush` is called manually on the given type implementing the writer interface.
     * * The terminal will `flush` automatically if the buffer is full.
     * * Each line is flushed in case of `stdout`, because it is line buffered.
     *
     * # Arguments
     *
     * - [Command]
     *
     *   The command that you want to queue for later execution.
     *
     * # Examples
     *
     * ```kotlin
     * val stdout = System.out
     *
     * // `Print` will be executed when `flush` is called.
     * stdout
     *     .queue(Print("foo 1\n"))
     *     .queue(Print("foo 2"))
     *
     * // some other code (no execution happening here) ...
     *
     * // when calling `flush` on `stdout`, all commands will be written to the stdout
     * // and therefore executed.
     * stdout.flush()
     *
     * // ==== Output ====
     * // foo 1
     * // foo 2
     * ```
     *
     * Have a look over at the Command API for more details.
     *
     * # Notes
     *
     * * On UNIX and Windows 10, ANSI codes are written to the given writer.
     * * In case of Windows versions lower than 10, a direct WinAPI call will be made.
     *   The reason for this is that Windows versions lower than 10 do not support ANSI codes,
     *   and can therefore not be written to the given `writer`.
     *   Therefore, there is no difference between [ExecutableCommand.execute] and
     *   [QueueableCommand.queue] for those old Windows versions.
     */
    fun queue(command: Command): QueueableCommand {
        if (!command.isAnsiCodeSupported()) {
            // There may be queued commands in this writer, but `executeWinapi` will execute the
            // command immediately. To prevent commands being executed out of order we flush the
            // writer now.
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
 */
interface ExecutableCommand : QueueableCommand {
    /**
     * Executes the given command directly.
     *
     * The given command's ANSI escape code will be written and flushed onto this writer.
     *
     * # Arguments
     *
     * - [Command]
     *
     *   The command that you want to execute directly.
     *
     * # Example
     *
     * ```kotlin
     * // will be executed directly
     * System.out
     *     .execute(Print("sum:\n"))
     *     .execute(Print("1 + 1= ${1 + 1} "))
     *
     * // ==== Output ====
     * // sum:
     * // 1 + 1 = 2
     * ```
     *
     * Have a look over at the Command API for more details.
     *
     * # Notes
     *
     * * On UNIX and Windows 10, ANSI codes are written to the given writer.
     * * In case of Windows versions lower than 10, a direct WinAPI call will be made.
     *   The reason for this is that Windows versions lower than 10 do not support ANSI codes,
     *   and can therefore not be written to the given `writer`.
     *   Therefore, there is no difference between [ExecutableCommand.execute] and
     *   [QueueableCommand.queue] for those old Windows versions.
     */
    fun execute(command: Command): ExecutableCommand {
        queue(command)
        flush()
        return this
    }
}

/**
 * An interface for types that support synchronized updates.
 */
interface SynchronizedUpdate : ExecutableCommand {
    /**
     * Performs a set of actions within a synchronous update.
     *
     * Updates will be suspended in the terminal, the function will be executed against self,
     * updates will be resumed, and a flush will be performed.
     *
     * # Arguments
     *
     * - Function
     *
     *     A function that performs the operations that must execute in a synchronized update.
     *
     * # Examples
     *
     * ```kotlin
     * val stdout = System.out
     *
     * stdout.syncUpdate { stdout ->
     *     stdout.execute(Print("foo 1\n"))
     *     stdout.execute(Print("foo 2"))
     *     // The effects of the print command will be present in the terminal
     *     // buffer, but not visible in the terminal.
     * }
     *
     * // The effects of the commands will be visible.
     * ```
     *
     * # Notes
     *
     * This command is performed only using ANSI codes, and will do nothing on terminals that
     * do not support ANSI codes, or this specific extension.
     *
     * When rendering the screen of the terminal, the Emulator usually iterates through each
     * visible grid cell and renders its current state. With applications updating the screen
     * at a higher frequency this can cause tearing.
     *
     * This mode attempts to mitigate that.
     *
     * When the synchronization mode is enabled following render calls will keep rendering the
     * last rendered state. The terminal Emulator keeps processing incoming text and sequences.
     * When the synchronized update mode is disabled again the renderer may fetch the latest
     * screen buffer state again, effectively avoiding the tearing effect by unintentionally
     * rendering in the middle of an application screen update.
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
 */
fun writeCommandAnsi(writer: Appendable, command: Command) {
    command.writeAnsi(writer)
}

/**
 * Executes the ANSI representation of a command, using the given [Appendable].
 */
internal fun executeFmt(f: Appendable, command: Command) {
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
 * This is the Kotlin command helper for combined ANSI output.
 */
fun execute(vararg commands: Command): String = buildString {
    for (command in commands) {
        command.writeAnsi(this)
    }
}
