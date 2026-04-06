// port-lint: source terminal/sys/unix.rs
package io.github.kotlinmania.crossterm.terminal.sys

/**
 * Platform-specific implementation of enabling raw mode.
 */
internal expect fun enableRawModeImpl()

/**
 * Platform-specific implementation of disabling raw mode.
 */
internal expect fun disableRawModeImpl()

