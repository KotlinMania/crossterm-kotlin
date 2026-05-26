// port-lint: source style/types/colored.rs
@file:OptIn(kotlin.concurrent.atomics.ExperimentalAtomicApi::class)

package io.github.kotlinmania.crossterm.style.types

import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi

internal object AnsiColorDisabledOverride {
    private val value: AtomicReference<Boolean?> = AtomicReference(null)

    fun get(): Boolean? = value.load()

    fun set(newValue: Boolean?) {
        value.store(newValue)
    }
}