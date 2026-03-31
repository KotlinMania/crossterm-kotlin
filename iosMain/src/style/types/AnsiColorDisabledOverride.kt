// port-lint: source style/types/colored.rs
package io.github.kotlinmania.crossterm.style.types

import kotlin.native.concurrent.ThreadLocal

@ThreadLocal
internal actual object AnsiColorDisabledOverride {
    private var value: Boolean? = null

    actual fun get(): Boolean? = value

    actual fun set(value: Boolean?) {
        this.value = value
    }
}

