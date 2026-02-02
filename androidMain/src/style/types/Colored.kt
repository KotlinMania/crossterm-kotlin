// port-lint: source style/types/colored.rs
package io.github.kotlinmania.crossterm.style.types

/**
 * Android implementation of environment variable access.
 *
 * Uses Java's System.getenv() to access environment variables.
 *
 * @param name The name of the environment variable
 * @return The value of the environment variable, or `null` if not set
 */
internal actual fun getEnvironmentVariable(name: String): String? {
    return System.getenv(name)
}
