package io.github.kotlinmania.crossterm.terminal.sys

import kotlinx.cinterop.CArrayPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import platform.posix.TIOCGWINSZ
import platform.posix.TCSANOW
import platform.posix.poll
import platform.posix.pollfd
import platform.posix.tcgetattr
import platform.posix.tcsetattr
import platform.posix.termios

// Linux TIOCGWINSZ value from <sys/ioctl.h>
internal actual val TIOCGWINSZ_VALUE: ULong = TIOCGWINSZ.toULong()

// Termios flag constants for Linux (tcflag_t is UInt/32-bit on Linux)
private const val IGNBRK: UInt = 0x00000001u
private const val BRKINT: UInt = 0x00000002u
private const val PARMRK: UInt = 0x00000008u
private const val ISTRIP: UInt = 0x00000020u
private const val INLCR: UInt = 0x00000040u
private const val IGNCR: UInt = 0x00000080u
private const val ICRNL: UInt = 0x00000100u
private const val IXON: UInt = 0x00000400u
private const val OPOST: UInt = 0x00000001u
private const val ECHO: UInt = 0x00000008u
private const val ECHONL: UInt = 0x00000040u
private const val ICANON: UInt = 0x00000002u  // Note: Linux value differs from macOS
private const val ISIG: UInt = 0x00000001u    // Note: Linux value differs from macOS
private const val IEXTEN: UInt = 0x00008000u
private const val CSIZE: UInt = 0x00000030u   // Note: Linux value differs from macOS
private const val PARENB: UInt = 0x00000100u  // Note: Linux value differs from macOS
private const val CS8: UInt = 0x00000030u     // Note: Linux value differs from macOS

/**
 * Stores original termios settings for restoration.
 */
private data class SavedTermios(
    val c_iflag: UInt,
    val c_oflag: UInt,
    val c_cflag: UInt,
    val c_lflag: UInt
)

private var savedTermios: SavedTermios? = null

/**
 * Linux implementation of enabling raw mode.
 * On Linux, tcflag_t is UInt (32-bit).
 */
@OptIn(ExperimentalForeignApi::class)
internal actual fun enableRawModeImpl() {
    memScoped {
        val fd = getTtyFd()
        val originalTermios = alloc<termios>()

        if (tcgetattr(fd, originalTermios.ptr) != 0) {
            throw IllegalStateException("Failed to get terminal attributes")
        }

        // Save original mode
        savedTermios = SavedTermios(
            c_iflag = originalTermios.c_iflag,
            c_oflag = originalTermios.c_oflag,
            c_cflag = originalTermios.c_cflag,
            c_lflag = originalTermios.c_lflag
        )

        val rawTermios = alloc<termios>()
        rawTermios.c_iflag = originalTermios.c_iflag
        rawTermios.c_oflag = originalTermios.c_oflag
        rawTermios.c_cflag = originalTermios.c_cflag
        rawTermios.c_lflag = originalTermios.c_lflag

        // Apply raw mode settings (equivalent to cfmakeraw)
        val iflagMask = IGNBRK or BRKINT or PARMRK or ISTRIP or INLCR or IGNCR or ICRNL or IXON
        rawTermios.c_iflag = rawTermios.c_iflag and iflagMask.inv()

        rawTermios.c_oflag = rawTermios.c_oflag and OPOST.inv()

        val lflagMask = ECHO or ECHONL or ICANON or ISIG or IEXTEN
        rawTermios.c_lflag = rawTermios.c_lflag and lflagMask.inv()

        val cflagMask = CSIZE or PARENB
        rawTermios.c_cflag = (rawTermios.c_cflag and cflagMask.inv()) or CS8

        if (tcsetattr(fd, TCSANOW, rawTermios.ptr) != 0) {
            throw IllegalStateException("Failed to set terminal to raw mode")
        }
    }
}

/**
 * Linux implementation of disabling raw mode.
 */
@OptIn(ExperimentalForeignApi::class)
internal actual fun disableRawModeImpl() {
    val saved = savedTermios ?: return

    memScoped {
        val fd = getTtyFd()
        val termiosToRestore = alloc<termios>()

        if (tcgetattr(fd, termiosToRestore.ptr) != 0) {
            throw IllegalStateException("Failed to get terminal attributes")
        }

        termiosToRestore.c_iflag = saved.c_iflag
        termiosToRestore.c_oflag = saved.c_oflag
        termiosToRestore.c_cflag = saved.c_cflag
        termiosToRestore.c_lflag = saved.c_lflag

        if (tcsetattr(fd, TCSANOW, termiosToRestore.ptr) != 0) {
            throw IllegalStateException("Failed to restore terminal mode")
        }

        savedTermios = null
    }
}

/**
 * Platform-specific poll wrapper for Linux.
 * On Linux, nfds_t is ULong (64-bit).
 */
@OptIn(ExperimentalForeignApi::class)
internal actual fun pollWrapper(fds: CArrayPointer<pollfd>, numFds: Int, timeoutMs: Int): Int {
    return poll(fds, numFds.toULong(), timeoutMs)
}
