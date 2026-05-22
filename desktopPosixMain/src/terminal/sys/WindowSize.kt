// port-lint: ignore
package io.github.kotlinmania.crossterm.terminal.sys

import io.github.kotlinmania.crossterm.terminal.WindowSize
import kotlinx.cinterop.ExperimentalForeignApi

/**
 * Platform-specific implementation to get window size via ioctl.
 */
@OptIn(ExperimentalForeignApi::class)
internal expect fun windowSizeViaIoctl(fd: Int): WindowSize
