// port-lint: source style/types/colored.rs
package io.github.kotlinmania.crossterm.style.types

internal actual object AnsiColorDisabledOverride {
    @Volatile
    private var value: Boolean? = null

    actual fun get(): Boolean? = value

    actual fun set(value: Boolean?) {
        this.value = value
    }
}

