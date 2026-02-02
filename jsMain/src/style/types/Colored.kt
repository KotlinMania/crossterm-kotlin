// port-lint: source style/types/colored.rs
package io.github.kotlinmania.crossterm.style.types

/**
 * JS implementation of environment variable access.
 *
 * In the browser, environment variables are not available, so this always returns null.
 * In Node.js, this accesses process.env.
 *
 * @param name The name of the environment variable
 * @return The value of the environment variable, or `null` if not set or in browser context
 */
internal actual fun getEnvironmentVariable(name: String): String? {
    return try {
        js("process.env[name]") as? String
    } catch (_: Throwable) {
        // In browser context, process is not defined
        null
    }
}
