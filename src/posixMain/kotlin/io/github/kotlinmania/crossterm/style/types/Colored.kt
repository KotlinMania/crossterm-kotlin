// port-lint: source style/types/colored.rs
@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package io.github.kotlinmania.crossterm.style.types

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString
import platform.posix.getenv

internal actual fun getEnvironmentVariable(name: String): String? = getenv(name)?.toKString()
