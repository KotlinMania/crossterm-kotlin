// port-lint: source event/sys.rs
package io.github.kotlinmania.crossterm.event.sys

/**
 * Interface for waking up blocked event reading operations.
 *
 * This corresponds to the Rust type re-exported in `event::sys::Waker` (selected per-platform via
 * `#[cfg(unix)]` / `#[cfg(windows)]` and the `event-stream` feature).
 *
 * Kotlin Multiplatform does not have Rust-style crate feature selection, so each platform provides
 * its own concrete waker implementation. Callers use this interface for the shared `wake()` API.
 */
interface Waker {
    /**
     * Wakes up any blocked poll/read operation.
     *
     * This method is safe to call from any thread.
     */
    fun wake()
}
