// port-lint: source cursor/sys/unix.rs
@file:OptIn(kotlin.experimental.ExperimentalObjCRefinement::class)

package io.github.kotlinmania.crossterm.cursor.sys

import kotlin.native.HiddenFromObjC

@HiddenFromObjC
actual fun position(): Pair<UShort, UShort> =
    throw UnsupportedOperationException("The cursor position could not be read within a normal duration")
