// port-lint: source tty.rs
package io.github.kotlinmania.crossterm

/**
 * External JS function to check if a file descriptor is a TTY in Node.js.
 */
@JsName("isNodeTty")
private external fun jsIsNodeTty(fd: Int): Boolean

/**
 * WASM JavaScript implementation of isatty.
 *
 * In Node.js environments, delegates to a JS function that checks process.stdin/stdout/stderr.isTTY.
 * In browser/non-Node environments, returns false.
 *
 * Note: This requires a JS glue file that provides isNodeTty(fd) function.
 *
 * @param fd The file descriptor to check (0=stdin, 1=stdout, 2=stderr)
 * @return true if the file descriptor refers to a TTY in a Node.js environment
 */
actual fun isatty(fd: Int): Boolean {
    return try {
        jsIsNodeTty(fd)
    } catch (_: Throwable) {
        false
    }
}
