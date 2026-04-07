<div align="center">

# Crossterm-Kotlin

**Cross-platform terminal manipulation for Kotlin Multiplatform**

[![Kotlin](https://img.shields.io/badge/Kotlin-2.3.0-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](./LICENSE)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.kotlinmania/crossterm-kotlin?color=blue)](https://central.sonatype.com/artifact/io.github.kotlinmania/crossterm-kotlin)
[![CI](https://github.com/KotlinMania/crossterm-kotlin/actions/workflows/ci.yml/badge.svg)](https://github.com/KotlinMania/crossterm-kotlin/actions/workflows/ci.yml)

[Installation](#installation) | [Quick Start](#quick-start) | [Platforms](#supported-platforms) | [API Reference](#api-reference) | [License](#license)

</div>

---

Crossterm-Kotlin is a Kotlin Multiplatform library for terminal manipulation. It is a faithful port of the Rust [crossterm](https://github.com/crossterm-rs/crossterm) crate, providing cross-platform APIs for cursor control, styling, terminal management, and event handling.

## Features

- **Cursor** - Move, hide/show, save/restore position, change cursor shape
- **Styling** - 16 colors, 256 colors, RGB/true colors, text attributes (bold, italic, underline, etc.)
- **Terminal** - Clear screen, scroll, alternate screen buffer, raw mode, window title
- **Events** - Keyboard input, mouse events, terminal resize, focus tracking, bracketed paste

## Supported Platforms

| Platform | Status | Notes |
|----------|--------|-------|
| macOS (arm64, x64) | Full | Native terminal via POSIX |
| Linux (x64) | Full | Native terminal via POSIX |
| Windows (x64) | Full | Native console API via MinGW |
| iOS | Partial | Styling only (no TTY) |
| Android | Partial | Styling only (no TTY) |
| JS/Browser | Partial | ANSI output only |
| WasmJS | Partial | ANSI output only |

## Installation

### Gradle (Kotlin DSL)

```kotlin
dependencies {
    implementation("io.github.kotlinmania:crossterm-kotlin:0.1.3")
}
```

### Gradle (Groovy)

```groovy
dependencies {
    implementation 'io.github.kotlinmania:crossterm-kotlin:0.1.3'
}
```

## Quick Start

### Cursor Movement

```kotlin
import io.github.kotlinmania.crossterm.cursor.*
import io.github.kotlinmania.crossterm.execute

// Move cursor to column 10, row 5
print(MoveTo(10u, 5u).ansiString())

// Hide and show cursor
print(Hide.ansiString())
print(Show.ansiString())

// Save and restore position
print(SavePosition.ansiString())
print(MoveTo(0u, 0u).ansiString())
print(RestorePosition.ansiString())
```

### Text Styling

```kotlin
import io.github.kotlinmania.crossterm.style.*
import io.github.kotlinmania.crossterm.style.types.*

// Basic colors
print(SetForegroundColor(Color.Red).ansiString())
print("Red text")
print(ResetColor.ansiString())

// RGB colors
print(SetForegroundColor(Color.Rgb(255u, 128u, 0u)).ansiString())
print("Orange text")

// 256-color palette
print(SetBackgroundColor(Color.AnsiValue(220u)).ansiString())

// Text attributes
print(SetAttribute(Attribute.Bold).ansiString())
print(SetAttribute(Attribute.Italic).ansiString())
print("Bold and italic")
print(SetAttribute(Attribute.Reset).ansiString())

// Styled content (chainable)
val styled = "Hello".stylize()
    .with(Color.Cyan)
    .on(Color.DarkBlue)
    .bold()
    .italic()
```

### Terminal Control

```kotlin
import io.github.kotlinmania.crossterm.terminal.*

// Enter alternate screen buffer (like vim/less)
print(EnterAlternateScreen.ansiString())

// Clear screen
print(Clear(ClearType.All).ansiString())

// Set window title
print(SetTitle("My App").ansiString())

// Enable raw mode for character-by-character input
enableRawMode()
// ... handle input ...
disableRawMode()

// Leave alternate screen
print(LeaveAlternateScreen.ansiString())
```

### Event Handling

```kotlin
import io.github.kotlinmania.crossterm.event.*

// Enable mouse capture
print(EnableMouseCapture.ansiString())

// Poll for events with timeout
if (poll(100.milliseconds)) {
    when (val event = read()) {
        is Event.Key -> {
            val key = event.keyEvent
            when (key.code) {
                is KeyCode.Char -> println("Key: ${(key.code as KeyCode.Char).char}")
                KeyCode.Enter -> println("Enter pressed")
                KeyCode.Esc -> println("Escape pressed")
                else -> {}
            }
        }
        is Event.Mouse -> {
            val mouse = event.mouseEvent
            println("Mouse ${mouse.kind} at (${mouse.column}, ${mouse.row})")
        }
        is Event.Resize -> {
            println("Terminal resized to ${event.columns}x${event.rows}")
        }
        Event.FocusGained -> println("Focus gained")
        Event.FocusLost -> println("Focus lost")
        is Event.Paste -> println("Pasted: ${event.content}")
    }
}

// Disable mouse capture
print(DisableMouseCapture.ansiString())
```

## API Reference

### Modules

| Module | Description |
|--------|-------------|
| `io.github.kotlinmania.crossterm.cursor` | Cursor movement and visibility |
| `io.github.kotlinmania.crossterm.style` | Colors, attributes, and styled content |
| `io.github.kotlinmania.crossterm.terminal` | Screen control, raw mode, terminal info |
| `io.github.kotlinmania.crossterm.event` | Keyboard, mouse, and system events |

### Command Pattern

All terminal commands implement a common pattern:

```kotlin
interface Command {
    fun ansiString(): String      // Get ANSI escape sequence
    fun writeAnsi(writer: Writer) // Write to a writer
}
```

Commands can be executed individually or batched:

```kotlin
// Individual
print(MoveTo(0u, 0u).ansiString())

// Batched
print(execute(
    MoveTo(0u, 0u),
    Clear(ClearType.All),
    SetForegroundColor(Color.Green)
))
```

## Building from Source

```bash
# Clone the repository
git clone https://github.com/KotlinMania/crossterm-kotlin.git
cd crossterm-kotlin

# Build all targets
./gradlew assemble

# Run tests
./gradlew allTests

# Build for specific platform
./gradlew macosArm64MainKlibrary
./gradlew linuxX64MainKlibrary
./gradlew mingwX64MainKlibrary
```

## Contributing

Contributions are welcome! Please feel free to submit issues and pull requests.

This project uses:
- Kotlin 2.3.0
- Gradle 9.2.1
- kotlinx-coroutines for async operations
- POSIX APIs on Unix-like systems
- Windows Console API on Windows

## Acknowledgements

This Kotlin Multiplatform library is a port of the excellent [crossterm](https://github.com/crossterm-rs/crossterm) Rust crate. Special thanks to [Timon](https://github.com/TimonPost) and the crossterm-rs maintainers for creating such a well-designed cross-platform terminal library.

## License

This project is licensed under the [MIT License](./LICENSE).

```
Copyright (c) 2019 Timon (crossterm-rs)
Copyright (c) 2024-2026 Sydney Renee, The Solace Project
```

---

<div align="center">

**Maintained by [Sydney Renee](mailto:sydney@solace.ofharmony.ai) of [The Solace Project](https://github.com/TheSolaceProject)**

Part of the [KotlinMania](https://github.com/KotlinMania) organization

</div>
