// port-lint: source tty.rs
package io.github.kotlinmania.crossterm

/**
 * JavaScript implementation of isatty.
 *
 * In Node.js environments, checks if the given file descriptor is a TTY.
 * In browser environments, always returns false.
 *
 * @param fd The file descriptor to check (0=stdin, 1=stdout, 2=stderr)
 * @return true if the file descriptor refers to a TTY in a Node.js environment
 */
actual fun isatty(fd: Int): Boolean {
    return try {
        val process = js("(typeof process !== 'undefined' ? process : null)")
        if (process == null) {
            // Browser environment - no TTY concept
            return false
        }

        // In Node.js, check the appropriate stream based on fd
        val stream = when (fd) {
            0 -> process.stdin
            1 -> process.stdout
            2 -> process.stderr
            else -> null
        }

        // Check if the stream exists and has isTTY property set to true
        stream?.isTTY == true
    } catch (_: Throwable) {
        false
    }
}
