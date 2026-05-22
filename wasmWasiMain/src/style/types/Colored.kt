// port-lint: source style/types/colored.rs
package io.github.kotlinmania.crossterm.style.types

/**
 * WASM WASI implementation of environment variable access.
 *
 * In the WASM context, environment variables are not available.
 *
 * @param name The name of the environment variable
 * @return Always `null` as environment variables are not accessible in WASM context
 */
internal actual fun getEnvironmentVariable(name: String): String? {
    // Environment variables are not accessible in WASM context
    return null
}
