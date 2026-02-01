# Crossterm-Kotlin

[![Kotlin](https://img.shields.io/badge/Kotlin-2.1+-blue.svg?logo=kotlin)](https://kotlinlang.org)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](#license)
[![GitHub](https://img.shields.io/badge/github-KotlinMania%2Fcrossterm--kotlin-blue?logo=github)](https://github.com/KotlinMania/crossterm-kotlin)

A **Kotlin Multiplatform Native** terminal manipulation library. This is a port of the Rust [crossterm](https://github.com/crossterm-rs/crossterm) crate.

## Overview

Crossterm-Kotlin provides cross-platform terminal manipulation capabilities:

- **Cursor** - Move the cursor, hide/show, change shape
- **Styling** - Colors (16, 256, RGB) and text attributes (bold, italic, etc.)
- **Terminal** - Clear screen, alternate screen buffer, raw mode
- **Events** - Keyboard, mouse, resize, and focus events

## Supported Platforms

- macOS (arm64, x64)
- Linux (x64)
- Windows (x64 via MinGW)

## Installation

### Maven Central

```kotlin
dependencies {
    implementation("io.github.kotlinmania:crossterm-kotlin:0.1.0")
}
```

### As a Git Submodule

```bash
git submodule add https://github.com/KotlinMania/crossterm-kotlin.git
```

Then in your `settings.gradle.kts`:

```kotlin
include(":crossterm-kotlin")
```

## Quick Start

### Cursor Movement

```kotlin
import io.github.kotlinmania.crossterm.cursor.*
import io.github.kotlinmania.crossterm.execute

// Move cursor to position (10, 5)
print(MoveTo(10u, 5u).ansiString())

// Hide cursor
print(Hide.ansiString())

// Execute multiple commands
print(execute(MoveTo(0u, 0u), Show))
```

### Styling

```kotlin
import io.github.kotlinmania.crossterm.style.*

// Set foreground color
print(SetForegroundColor(Color.Red).ansiString())
print("Red text")
print(ResetColor.ansiString())

// RGB colors
print(SetForegroundColor(Color.Rgb(255u, 128u, 0u)).ansiString())
print("Orange text")

// Text attributes
print(SetAttribute(Attribute.Bold).ansiString())
print("Bold text")
```

### Terminal Control

```kotlin
import io.github.kotlinmania.crossterm.terminal.*

// Enter alternate screen buffer
print(EnterAlternateScreen.ansiString())

// Clear screen
print(Clear(ClearType.All).ansiString())

// Leave alternate screen
print(LeaveAlternateScreen.ansiString())
```

### Events

```kotlin
import io.github.kotlinmania.crossterm.event.*

// Event types
val keyEvent = KeyEvent(
    code = KeyCode.Char('q'),
    modifiers = KeyModifiers.CONTROL
)

when (val event = readEvent()) {
    is Event.Key -> handleKey(event.keyEvent)
    is Event.Mouse -> handleMouse(event.mouseEvent)
    is Event.Resize -> handleResize(event.columns, event.rows)
    Event.FocusGained -> handleFocus(true)
    Event.FocusLost -> handleFocus(false)
    is Event.Paste -> handlePaste(event.content)
}
```

## Porting Status

This is an early port. See [AGENTS.md](AGENTS.md) for porting guidelines and progress tracking.

### Completed
- Event types (Event, KeyEvent, KeyCode, KeyModifiers, MouseEvent)
- Cursor commands (MoveTo, Hide, Show, etc.)
- Style commands (SetForegroundColor, SetBackgroundColor, colors, attributes)
- Terminal commands (EnterAlternateScreen, Clear, etc.)

### In Progress
- Event reading (platform-specific)
- Raw mode (platform-specific termios/console API)
- Terminal size detection

## License

Licensed under the MIT license ([LICENSE-MIT](./LICENSE-MIT)).

### Contribution

Unless you explicitly state otherwise, any contribution intentionally submitted for inclusion in the work by you shall be licensed as above, without any additional terms or conditions.

---

## Acknowledgments

This Kotlin Multiplatform port was created by **Sydney Renee** of [The Solace Project](mailto:sydney@solace.ofharmony.ai) for [KotlinMania](https://github.com/KotlinMania).

Special thanks to the [crossterm](https://github.com/crossterm-rs/crossterm) maintainers and contributors.
