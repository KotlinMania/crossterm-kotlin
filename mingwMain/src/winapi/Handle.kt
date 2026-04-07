// Platform-specific Windows console handle wrapper
// Corresponds to crossterm_winapi::Handle
@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
package io.github.kotlinmania.crossterm.winapi

import kotlinx.cinterop.ExperimentalForeignApi
import platform.windows.GetStdHandle
import platform.windows.HANDLE
import platform.windows.INVALID_HANDLE_VALUE
import platform.windows.STD_INPUT_HANDLE

/**
 * Windows console handle wrapper.
 *
 * This corresponds to crossterm_winapi::Handle in Rust.
 */
class Handle private constructor(internal val value: HANDLE?) {
    companion object {
        /**
         * Gets the standard input handle.
         *
         * Corresponds to `Handle::current_in_handle()` in Rust.
         */
        fun currentInHandle(): Handle {
            val handle = GetStdHandle(STD_INPUT_HANDLE)
            if (handle == INVALID_HANDLE_VALUE) {
                throw IllegalStateException("Failed to get standard input handle")
            }
            return Handle(handle)
        }
    }
}
