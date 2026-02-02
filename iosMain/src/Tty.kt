// Stub implementation for iOS
package io.github.kotlinmania.crossterm

/**
 * iOS implementation of isatty.
 *
 * On iOS, standard apps don't run in a terminal context.
 * Terminal emulator apps like iSH or a]check would need native integration.
 *
 * @param fd The file descriptor to check
 * @return Always returns false on iOS (for now)
 */
actual fun isatty(fd: Int): Boolean {
    // iOS apps typically don't run in a terminal context
    // Future: could detect iSH or other terminal emulators
    return false
}
