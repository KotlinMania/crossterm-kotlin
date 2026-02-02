// port-lint: source lib.rs
package io.github.kotlinmania.crossterm

/**
 * # Cross-platform Terminal Manipulation Library
 *
 * Crossterm-kotlin is a Kotlin Multiplatform terminal manipulation library that makes it possible
 * to write cross-platform text-based interfaces.
 *
 * This library supports Unix (macOS, Linux) and Windows terminals.
 *
 * ## Command API
 *
 * The command API makes the use of crossterm much easier and offers more control over when and how a
 * command is executed. A command is just an action you can perform on the terminal e.g. cursor movement.
 *
 * The command API offers:
 *
 * * Better Performance
 * * Complete control over when to flush
 * * Complete control over where the ANSI escape commands are executed to
 * * Easier and more idiomatic Kotlin API
 *
 * ### Supported Commands
 *
 * - Module `cursor`
 *   - Visibility - `Show`, `Hide`
 *   - Appearance - `EnableBlinking`, `DisableBlinking`, `SetCursorStyle`
 *   - Position - `SavePosition`, `RestorePosition`, `MoveUp`, `MoveDown`,
 *     `MoveLeft`, `MoveRight`, `MoveTo`, `MoveToColumn`, `MoveToRow`,
 *     `MoveToNextLine`, `MoveToPreviousLine`
 *
 * - Module `event`
 *   - Keyboard events - `PushKeyboardEnhancementFlags`, `PopKeyboardEnhancementFlags`
 *   - Mouse events - `EnableMouseCapture`, `DisableMouseCapture`
 *
 * - Module `style`
 *   - Colors - `SetForegroundColor`, `SetBackgroundColor`, `ResetColor`, `SetColors`
 *   - Attributes - `SetAttribute`, `SetAttributes`, `PrintStyledContent`
 *
 * - Module `terminal`
 *   - Scrolling - `ScrollUp`, `ScrollDown`
 *   - Miscellaneous - `Clear`, `SetSize`, `SetTitle`, `DisableLineWrap`, `EnableLineWrap`
 *   - Alternate screen - `EnterAlternateScreen`, `LeaveAlternateScreen`
 *
 * - Module `clipboard`
 *   - Clipboard - `CopyToClipboard`
 *
 * ### Command Execution
 *
 * Commands can be executed by calling their `writeAnsi` method to an `Appendable`:
 *
 * ```kotlin
 * import io.github.kotlinmania.crossterm.cursor.MoveTo
 *
 * val command = MoveTo(10u, 5u)
 * val output = StringBuilder()
 * command.writeAnsi(output)
 * print(output)
 * ```
 *
 * Or by using the convenience `ansiString()` extension:
 *
 * ```kotlin
 * import io.github.kotlinmania.crossterm.cursor.MoveTo
 *
 * print(MoveTo(10u, 5u).ansiString())
 * ```
 *
 * ## Examples
 *
 * Print a rectangle colored with magenta:
 *
 * ```kotlin
 * import io.github.kotlinmania.crossterm.cursor.MoveTo
 * import io.github.kotlinmania.crossterm.style.Color
 * import io.github.kotlinmania.crossterm.style.SetForegroundColor
 * import io.github.kotlinmania.crossterm.terminal.Clear
 * import io.github.kotlinmania.crossterm.terminal.ClearType
 *
 * fun main() {
 *     print(Clear(ClearType.All).ansiString())
 *     print(SetForegroundColor(Color.Magenta).ansiString())
 *
 *     for (y in 0 until 40) {
 *         for (x in 0 until 150) {
 *             if (y == 0 || y == 39 || x == 0 || x == 149) {
 *                 print(MoveTo(x.toUShort(), y.toUShort()).ansiString())
 *                 print("█")
 *             }
 *         }
 *     }
 * }
 * ```
 */

// Re-export commonly used types
// In Kotlin, these are available through their respective packages:
// - io.github.kotlinmania.crossterm.Command
// - io.github.kotlinmania.crossterm.cursor.*
// - io.github.kotlinmania.crossterm.event.*
// - io.github.kotlinmania.crossterm.style.*
// - io.github.kotlinmania.crossterm.terminal.*
// - io.github.kotlinmania.crossterm.clipboard.*
// - io.github.kotlinmania.crossterm.tty.*
