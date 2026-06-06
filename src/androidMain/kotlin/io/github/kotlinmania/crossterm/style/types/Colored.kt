// port-lint: source style/types/colored.rs
package io.github.kotlinmania.crossterm.style.types

internal actual fun getEnvironmentVariable(name: String): String? = System.getenv(name)
