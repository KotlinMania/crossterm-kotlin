// port-lint: source event/sys/unix/waker.rs
package io.github.kotlinmania.crossterm.event.sys.unix

/**
 * Unix waker implementations for interrupting blocked event source operations.
 *
 * This module provides waker implementations for Unix platforms that allow
 * external signals to interrupt blocking poll/read operations.
 *
 * In Rust, this module conditionally exports either tty::Waker or mio::Waker
 * based on the "use-dev-tty" feature flag:
 * ```rust
 * #[cfg(feature = "use-dev-tty")]
 * pub(crate) mod tty;
 *
 * #[cfg(not(feature = "use-dev-tty"))]
 * pub(crate) mod mio;
 *
 * #[cfg(feature = "use-dev-tty")]
 * pub(crate) use self::tty::Waker;
 *
 * #[cfg(not(feature = "use-dev-tty"))]
 * pub(crate) use self::mio::Waker;
 * ```
 *
 * In Kotlin Multiplatform, the actual implementations are in platform-specific
 * source sets (posixMain):
 * - `io.github.kotlinmania.crossterm.event.sys.unix.waker.TtyWaker` - TTY-based waker using Unix socket
 * - `io.github.kotlinmania.crossterm.event.sys.unix.waker.MioWaker` - Mio-based waker using poll/kqueue
 *
 * The appropriate implementation is selected at runtime based on the
 * event source configuration.
 *
 * @see io.github.kotlinmania.crossterm.event.sys.Waker
 * @see io.github.kotlinmania.crossterm.event.source.EventSource
 */
