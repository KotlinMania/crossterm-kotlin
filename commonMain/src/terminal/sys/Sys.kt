// port-lint: source terminal/sys.rs
package io.github.kotlinmania.crossterm.terminal.sys

/**
 * This module provides platform-related functions for terminal manipulation.
 *
 * The functions declared here are platform-specific and are implemented
 * in the respective platform source sets (nativeMain, mingwMain, etc.).
 */

/**
 * Enables raw mode for the terminal.
 *
 * Raw mode disables:
 * - Line buffering (input is available immediately)
 * - Echo (characters are not echoed to the terminal)
 * - Special character processing (Ctrl-C, Ctrl-Z, etc.)
 *
 * This is typically used for TUI applications that need full control
 * over the terminal input.
 *
 * @throws Exception if raw mode cannot be enabled
 */
expect fun enableRawMode()

/**
 * Disables raw mode for the terminal.
 *
 * This resets the terminal to its previous mode before [enableRawMode] was called.
 * If [enableRawMode] was not called, this function has no effect.
 *
 * More precisely, this resets the whole termios mode to what it was before
 * the first call to [enableRawMode]. If you don't modify termios outside of
 * crossterm, it effectively disables raw mode and does nothing else.
 *
 * @throws Exception if the terminal mode cannot be restored
 */
expect fun disableRawMode()

/**
 * Checks whether raw mode is currently enabled.
 *
 * @return `true` if raw mode is enabled, `false` otherwise
 */
expect fun isRawModeEnabled(): Boolean

/**
 * Returns the terminal size as a pair of (columns, rows).
 *
 * This function first tries to query the terminal size via ioctl,
 * and falls back to `tput` if that fails.
 *
 * @return A pair of (columns, rows)
 * @throws Exception if the terminal size cannot be determined
 */
expect fun size(): Pair<UShort, UShort>

/**
 * Returns the terminal window size including pixel dimensions.
 *
 * This function queries the terminal for its size in both characters
 * and pixels using platform-specific mechanisms.
 *
 * @return The window size with columns, rows, width, and height
 * @throws Exception if the window size cannot be determined
 */
expect fun windowSize(): io.github.kotlinmania.crossterm.terminal.WindowSize

/**
 * Queries the terminal's support for progressive keyboard enhancement.
 *
 * On Unix systems, this function will block and possibly time out while
 * [io.github.kotlinmania.crossterm.event.read] or [io.github.kotlinmania.crossterm.event.poll]
 * are being called.
 *
 * @return `true` if keyboard enhancement is supported, `false` otherwise
 * @throws Exception if the query times out or fails
 */
expect fun supportsKeyboardEnhancement(): Boolean
