// port-lint: source ansi_support.rs
package io.github.kotlinmania.crossterm

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString
import platform.posix.getenv

/**
 * iOS implementation of ANSI support detection.
 *
 * iOS apps running in terminal emulators (like iSH) may support ANSI.
 * Returns true unless TERM is set to "dumb".
 */
@OptIn(ExperimentalForeignApi::class)
internal actual fun detectAnsiSupport(): Boolean {
    val term = getenv("TERM")?.toKString()
    return term == null || term != "dumb"
}
