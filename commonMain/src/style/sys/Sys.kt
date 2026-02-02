// port-lint: source style/sys.rs
package io.github.kotlinmania.crossterm.style.sys

/**
 * Platform-specific style system module.
 *
 * This module contains platform-specific implementations for styling text
 * on terminals that don't support ANSI escape codes.
 *
 * ## Platform Support
 *
 * - **Windows (legacy)**: Uses the Windows Console API to set text colors
 *   and attributes. This is required for Windows versions before Windows 10
 *   that don't support ANSI escape sequences.
 *
 * - **Unix/POSIX**: No platform-specific implementation needed as all modern
 *   Unix terminals support ANSI escape sequences.
 *
 * ## Submodules
 *
 * - `windows` - Windows Console API color implementation
 *   (available in `mingwMain/src/style/sys/Windows.kt`)
 *
 * ## Usage
 *
 * Most applications should use the high-level style API which automatically
 * selects the appropriate implementation based on platform and terminal
 * capabilities:
 *
 * ```kotlin
 * import io.github.kotlinmania.crossterm.style.*
 *
 * // This works on all platforms
 * print(SetForegroundColor(Color.Red).ansiString())
 * ```
 *
 * The platform-specific implementations in this module are used internally
 * by the style commands when ANSI is not supported.
 */

// Note: In Kotlin Multiplatform, platform-specific implementations are
// provided through expect/actual declarations and separate source sets:
// - mingwMain/src/style/sys/Windows.kt - Windows implementation
// - posixMain has no sys module (uses ANSI codes directly)
