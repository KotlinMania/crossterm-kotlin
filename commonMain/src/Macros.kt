// port-lint: source macros.rs
package io.github.kotlinmania.crossterm

/**
 * ANSI escape sequence constants and helpers.
 *
 * This file provides constants and utility functions for constructing ANSI
 * escape sequences, similar to the Rust macros `csi!` and `osc!`.
 *
 * ## Constants
 *
 * - [ESC] - The escape character (0x1B)
 * - [CSI] - Control Sequence Introducer (ESC[)
 * - [OSC] - Operating System Command introducer (ESC])
 * - [ST] - String Terminator (ESC\)
 * - [BEL] - Bell character (0x07)
 *
 * ## Usage
 *
 * ```kotlin
 * // CSI sequence example (move cursor to position)
 * val moveTo = "${CSI}10;5H"
 *
 * // OSC sequence example (set window title)
 * val setTitle = "${OSC}0;My Title${ST}"
 * ```
 */

/**
 * The escape character (ESC, 0x1B).
 */
const val ESC: String = "\u001B"

/**
 * Control Sequence Introducer (CSI).
 *
 * Used for cursor movement, text formatting, and other control sequences.
 * Equivalent to ESC[.
 */
const val CSI: String = "\u001B["

/**
 * Operating System Command introducer (OSC).
 *
 * Used for setting window titles, clipboard operations, and other
 * operating system level commands. Equivalent to ESC].
 */
const val OSC: String = "\u001B]"

/**
 * String Terminator (ST).
 *
 * Used to terminate OSC and other escape sequences.
 * Equivalent to ESC\.
 */
const val ST: String = "\u001B\\"

/**
 * Bell character (BEL, 0x07).
 *
 * Can be used as an alternative string terminator for OSC sequences.
 */
const val BEL: String = "\u0007"

/**
 * Creates a CSI (Control Sequence Introducer) sequence.
 *
 * This is equivalent to the Rust `csi!` macro.
 *
 * @param sequence The sequence content after CSI
 * @return The complete CSI sequence
 *
 * Example:
 * ```kotlin
 * val clearScreen = csi("2J")  // Returns "\u001B[2J"
 * val moveTo = csi("10;5H")    // Returns "\u001B[10;5H"
 * ```
 */
fun csi(sequence: String): String = "$CSI$sequence"

/**
 * Creates an OSC (Operating System Command) sequence.
 *
 * This is equivalent to the Rust `osc!` macro. Uses ST (String Terminator)
 * to terminate the sequence.
 *
 * @param sequence The sequence content after OSC
 * @return The complete OSC sequence with ST terminator
 *
 * Example:
 * ```kotlin
 * val setTitle = osc("0;Window Title")  // Returns "\u001B]0;Window Title\u001B\\"
 * ```
 */
fun osc(sequence: String): String = "$OSC$sequence$ST"

/**
 * Creates an OSC (Operating System Command) sequence with BEL terminator.
 *
 * Some terminals prefer BEL as the terminator instead of ST.
 *
 * @param sequence The sequence content after OSC
 * @return The complete OSC sequence with BEL terminator
 *
 * Example:
 * ```kotlin
 * val setTitle = oscBel("0;Window Title")  // Returns "\u001B]0;Window Title\u0007"
 * ```
 */
fun oscBel(sequence: String): String = "$OSC$sequence$BEL"
