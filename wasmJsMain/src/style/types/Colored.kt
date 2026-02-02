// port-lint: source style/types/colored.rs
package io.github.kotlinmania.crossterm.style.types

/**
 * Wasm JS implementation of environment variable access.
 *
 * In the browser/Wasm context, environment variables are not available.
 *
 * @param name The name of the environment variable
 * @return Always `null` as environment variables are not accessible in Wasm context
 */
internal actual fun getEnvironmentVariable(name: String): String? {
    // Environment variables are not accessible in Wasm browser context
    return null
}
