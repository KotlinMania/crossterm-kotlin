// port-lint: source style/types/colored.rs
package io.github.kotlinmania.crossterm.style.types

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString
import platform.posix.getenv

/**
 * iOS implementation of environment variable access.
 *
 * @param name The name of the environment variable
 * @return The value of the environment variable, or `null` if not set
 */
@OptIn(ExperimentalForeignApi::class)
internal actual fun getEnvironmentVariable(name: String): String? {
    return getenv(name)?.toKString()
}
