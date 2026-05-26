// port-lint: source cursor/sys.rs
@file:OptIn(kotlin.experimental.ExperimentalObjCRefinement::class)

package io.github.kotlinmania.crossterm.cursor.sys

import kotlin.native.HiddenFromObjC

@HiddenFromObjC
expect fun position(): Pair<UShort, UShort>