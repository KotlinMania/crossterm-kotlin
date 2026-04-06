// port-lint: source event/sys/unix/waker.rs
package io.github.kotlinmania.crossterm.event.sys.unix.waker

/**
 * Unix waker implementations for interrupting blocked event source operations.
 *
 * Ported from Rust crossterm `src/event/sys/unix/waker.rs`.
 *
 * Rust conditionally exports either `tty::Waker` or `mio::Waker`:
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
 * Kotlin Multiplatform doesn't have crate features like Rust. This port keeps both implementations
 * ([TtyWaker] and [MioWaker]) and aliases [Waker] to the default behavior (the mio-based waker).
 */
typealias Waker = MioWaker

