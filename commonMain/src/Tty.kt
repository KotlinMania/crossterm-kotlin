// port-lint: source tty.rs
package io.github.kotlinmania.crossterm

/**
 * Making it a little more convenient and safe to query whether
 * something is a terminal teletype or not.
 *
 * This module defines the [IsTty] interface and the [isTty] function to
 * return true if the item represents a terminal.
 */

/**
 * Adds the [isTty] method to types that might represent a terminal.
 *
 * Example:
 * ```kotlin
 * import io.github.kotlinmania.crossterm.IsTty
 *
 * // Check if a file descriptor represents a terminal
 * val fd: Int = 1 // stdout
 * val isTty: Boolean = fd.isTty()
 * ```
 */
interface IsTty {
    /**
     * Returns true when an instance is a terminal teletype, otherwise false.
     */
    fun isTty(): Boolean
}

/**
 * Checks if the given file descriptor is a terminal.
 *
 * On UNIX, this uses the `isatty()` function which returns true if a file
 * descriptor is a terminal.
 *
 * On Windows, this uses `GetConsoleMode()` which returns true if we are in a terminal.
 *
 * @param fd The file descriptor to check
 * @return true if the file descriptor is a terminal, false otherwise
 */
expect fun isatty(fd: Int): Boolean

/**
 * Extension function to check if a file descriptor is a terminal.
 *
 * Example:
 * ```kotlin
 * import io.github.kotlinmania.crossterm.isTty
 *
 * val stdout = 1
 * if (stdout.isTty()) {
 *     println("Running in a terminal")
 * }
 * ```
 */
fun Int.isTty(): Boolean = isatty(this)

/**
 * Standard file descriptor constants.
 */
object FileDescriptor {
    /** Standard input file descriptor */
    const val STDIN: Int = 0

    /** Standard output file descriptor */
    const val STDOUT: Int = 1

    /** Standard error file descriptor */
    const val STDERR: Int = 2
}
