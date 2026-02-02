// port-lint: source event/sys/unix.rs
package io.github.kotlinmania.crossterm.event.sys

/**
 * Unix-specific event system module.
 *
 * This module provides Unix-specific functionality for terminal event handling.
 * It corresponds to the Rust `event/sys/unix.rs` module which conditionally
 * includes the following submodules based on feature flags:
 *
 * ## Submodules
 *
 * - `waker` - Event waker for signaling between threads (available when "event-stream" feature is enabled)
 * - `parse` - ANSI escape sequence parser (available when "events" feature is enabled)
 *
 * In Kotlin, both modules are always available since we don't have compile-time
 * feature flags. The modules are organized as follows:
 *
 * - `io.github.kotlinmania.crossterm.event.sys.unix.waker` - Waker implementations
 * - `io.github.kotlinmania.crossterm.event.sys.unix.parse` - Event parsing utilities
 *
 * ## Waker Module
 *
 * The waker module provides mechanisms to wake up event polling from another thread.
 * Two implementations are available:
 *
 * - `mio` - Mio-style waker using a pipe for I/O notification
 * - `tty` - TTY-based waker using Unix socket pairs
 *
 * ## Parse Module
 *
 * The parse module provides ANSI escape sequence parsing for terminal input.
 * It handles parsing of keyboard events, mouse events, and other terminal
 * control sequences.
 *
 * ## Usage
 *
 * These modules are typically used internally by the event reading system.
 * Users should use the high-level event API instead:
 *
 * ```kotlin
 * import io.github.kotlinmania.crossterm.event.poll
 * import io.github.kotlinmania.crossterm.event.read
 * import kotlin.time.Duration.Companion.seconds
 *
 * // Poll for an event with timeout
 * if (poll(1.seconds)) {
 *     val event = read()
 *     // Handle the event
 * }
 * ```
 *
 * @see io.github.kotlinmania.crossterm.event.source.EventSource
 * @see io.github.kotlinmania.crossterm.event.source.unix
 */

// This file serves as the module root for Unix-specific event system functionality.
// The actual implementations are in the unix subpackage:
// - io.github.kotlinmania.crossterm.event.sys.unix.waker (waker implementations)
// - io.github.kotlinmania.crossterm.event.sys.unix.parse (event parsing)
//
// Rust source:
// #[cfg(feature = "event-stream")]
// pub(crate) mod waker;
//
// #[cfg(feature = "events")]
// pub(crate) mod parse;
