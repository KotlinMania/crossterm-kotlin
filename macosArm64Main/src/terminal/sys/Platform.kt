// port-lint: source terminal/sys/unix.rs
package io.github.kotlinmania.crossterm.terminal.sys

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import platform.posix.TCSANOW
import platform.posix.tcgetattr
import platform.posix.tcsetattr
import platform.posix.termios

// Termios flag constants for macOS
private const val IGNBRK: ULong = 0x00000001u
private const val BRKINT: ULong = 0x00000002u
private const val PARMRK: ULong = 0x00000008u
private const val ISTRIP: ULong = 0x00000020u
private const val INLCR: ULong = 0x00000040u
private const val IGNCR: ULong = 0x00000080u
private const val ICRNL: ULong = 0x00000100u
private const val IXON: ULong = 0x00000400u
private const val OPOST: ULong = 0x00000001u
private const val ECHO: ULong = 0x00000008u
private const val ECHONL: ULong = 0x00000040u
private const val ICANON: ULong = 0x00000100u
private const val ISIG: ULong = 0x00000080u
private const val IEXTEN: ULong = 0x00008000u
private const val CSIZE: ULong = 0x00000300u
private const val PARENB: ULong = 0x00001000u
private const val CS8: ULong = 0x00000300u

private data class SavedTermios(
    val c_iflag: ULong,
    val c_oflag: ULong,
    val c_cflag: ULong,
    val c_lflag: ULong
)

private var savedTermios: SavedTermios? = null

@OptIn(ExperimentalForeignApi::class)
internal actual fun enableRawModeImpl() {
    memScoped {
        val fd = getTtyFd()
        val originalTermios = alloc<termios>()

        if (tcgetattr(fd, originalTermios.ptr) != 0) {
            throw IllegalStateException("Failed to get terminal attributes")
        }

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

