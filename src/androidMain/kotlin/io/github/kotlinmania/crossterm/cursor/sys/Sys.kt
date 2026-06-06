package io.github.kotlinmania.crossterm.cursor.sys

actual fun position(): Pair<UShort, UShort> =
    throw UnsupportedOperationException("The cursor position could not be read within a normal duration")
