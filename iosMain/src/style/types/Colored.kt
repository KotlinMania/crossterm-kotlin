// port-lint: source style/types/colored.rs
package io.github.kotlinmania.crossterm.style.types

/**
 * iOS implementation of environment variable access.
 *
 * @param name The name of the environment variable
 * @return The value of the environment variable, or `null` if not set
 */
internal actual fun getEnvironmentVariable(name: String): String? {
    // Avoid using platform.posix during iosMain metadata compilation.
    return null
}
