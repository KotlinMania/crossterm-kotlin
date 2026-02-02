// port-lint: source event/sys.rs
package io.github.kotlinmania.crossterm.event.sys

/**
 * Platform-specific event system implementations.
 *
 * This module provides the low-level platform-specific functionality for event handling.
 * The actual implementations are provided in platform-specific source sets:
 *
 * - **Unix platforms** (macOS, Linux): Implementations in `nativeMain/src/event/sys/unix/`
 *   - `Waker`: Event loop waker implementation using either mio or TTY-based approaches
 *   - `parse`: ANSI escape sequence parsing
 *
 * - **Windows**: Implementations in `mingwMain/src/event/sys/windows/`
 *   - `Waker`: Event loop waker implementation using Windows console APIs
 *   - `parse`: Windows console input parsing
 *   - `poll`: Windows-specific polling implementation
 *   - Mouse capture support via console mode manipulation
 *
 * ## Event Stream Support
 *
 * The [Waker] type is used by the event-stream feature to allow external signals
 * to interrupt blocking poll operations. This enables:
 * - Graceful shutdown of event loops
 * - Integration with async runtimes
 * - Cross-thread event notification
 *
 * ## Example Usage
 *
 * The Waker is typically accessed through the [InternalEventReader]:
 * ```kotlin
 * val reader = InternalEventReader()
 * val waker = reader.waker()
 * // In another thread/coroutine:
 * waker.wake()
 * ```
 *
 * @see io.github.kotlinmania.crossterm.event.Waker
 * @see io.github.kotlinmania.crossterm.event.InternalEventReader
 */

// This file serves as documentation for the sys module structure.
// Platform-specific implementations are provided in:
// - nativeMain/src/event/sys/unix/ for Unix platforms
// - mingwMain/src/event/sys/windows/ for Windows

/**
 * Re-exports and platform-specific type aliases would be declared here
 * if Kotlin supported conditional compilation like Rust's #[cfg] attributes.
 *
 * In Kotlin Multiplatform, platform-specific types are resolved through
 * expect/actual declarations in the respective source sets.
 */
