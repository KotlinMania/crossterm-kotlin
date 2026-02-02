// port-lint: source tty.rs
@file:JvmName("TtyAndroid")
package io.github.kotlinmania.crossterm

/**
 * Android implementation of isatty.
 *
 * On Android, standard apps don't run in a terminal context.
 * When running in Termux or similar terminal emulators, the
 * file descriptors may be connected to a pseudo-terminal.
 *
 * This implementation always returns false since Android apps
 * typically don't have TTY access.
 *
 * @param fd The file descriptor to check
 * @return Always returns false on Android
 */
actual fun isatty(fd: Int): Boolean {
    // Android apps typically don't run in a terminal context
    // Termux and similar apps would need native JNI calls to detect TTY
    return false
}
