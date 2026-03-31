// port-lint: source style/types/colored.rs
package io.github.kotlinmania.crossterm.style.types

/**
 * Platform-specific storage for the ANSI color disabled override.
 *
 * Kotlin/Native requires thread-local or otherwise safe global mutation for objects that are
 * written from tests or library code. We keep the API surface in common code and delegate the
 * backing storage to platform source sets.
 */
internal expect object AnsiColorDisabledOverride {
    fun get(): Boolean?
    fun set(value: Boolean?)
}

