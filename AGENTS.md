# Crossterm-Kotlin Porting Guide

Upstream Rust source is in `tmp/crossterm-rs/` for reference.

---

## CRITICAL: Folder Structure Must Mirror Packages

**The Kotlin folder structure MUST mirror the package hierarchy exactly.**

### Rust → Kotlin Mapping

| Rust Path | Kotlin Path | Package |
|-----------|-------------|---------|
| `src/event.rs` | `commonMain/src/event/Event.kt` | `io.github.kotlinmania.crossterm.event` |
| `src/event/filter.rs` | `commonMain/src/event/Filter.kt` | `io.github.kotlinmania.crossterm.event` |
| `src/event/sys/unix/parse.rs` | `nativeMain/src/event/sys/unix/Parse.kt` | `io.github.kotlinmania.crossterm.event.sys.unix` |
| `src/cursor.rs` | `commonMain/src/cursor/Cursor.kt` | `io.github.kotlinmania.crossterm.cursor` |
| `src/cursor/sys/unix.rs` | `nativeMain/src/cursor/sys/Unix.kt` | `io.github.kotlinmania.crossterm.cursor.sys` |
| `src/style.rs` | `commonMain/src/style/Style.kt` | `io.github.kotlinmania.crossterm.style` |
| `src/style/types/color.rs` | `commonMain/src/style/types/Color.kt` | `io.github.kotlinmania.crossterm.style.types` |
| `src/terminal.rs` | `commonMain/src/terminal/Terminal.kt` | `io.github.kotlinmania.crossterm.terminal` |
| `src/command.rs` | `commonMain/src/Command.kt` | `io.github.kotlinmania.crossterm` |

### Rules

1. **Package = folder path**: `io.github.kotlinmania.crossterm.event.sys.unix` → `commonMain/src/event/sys/unix/`
2. **Platform-specific code**:
   - Common (all platforms): `commonMain/src/`
   - Native (macOS/Linux/Windows): `nativeMain/src/`
   - Unix-only: `unixMain/src/` (if needed)
   - Windows-only: `mingwMain/src/` (if needed)
3. **File naming**: Rust `snake_case.rs` → Kotlin `PascalCase.kt`
4. **One primary type per file**: Match Rust's module structure

---

## REQUIRED: Port-Lint Provenance Headers

**Every Kotlin file MUST have a port-lint header as the FIRST line:**

```kotlin
// port-lint: source <path-relative-to-src/>
package io.github.kotlinmania.crossterm.<module>
```

### Examples

```kotlin
// port-lint: source event.rs
package io.github.kotlinmania.crossterm.event

// port-lint: source event/filter.rs
package io.github.kotlinmania.crossterm.event

// port-lint: source event/sys/unix/parse.rs
package io.github.kotlinmania.crossterm.event.sys.unix

// port-lint: source style/types/color.rs
package io.github.kotlinmania.crossterm.style.types

// port-lint: source terminal/sys/unix.rs
package io.github.kotlinmania.crossterm.terminal.sys
```

### Why This Matters

- **AST tool tracking**: The tool matches files by header first
- **Verification**: `ast_distance --deep` shows "Matched by header: X / Y"
- **Swarm coordination**: Prevents duplicate work
- **Provenance**: Know exactly which Rust file each Kotlin file came from

### Verification Command

```bash
cd /Volumes/stuff/Projects/kotlinmania/crossterm-kotlin
./tools/ast_distance/build/ast_distance --deep tmp/crossterm-rs/src rust commonMain/src kotlin
```

---

## Documentation Requirements

**Port ALL documentation from Rust to Kotlin KDoc format.**

### Rust → Kotlin Doc Conversion

```rust
/// A command that moves the cursor to the specified position.
///
/// # Arguments
///
/// * `column` - The column (0-indexed)
/// * `row` - The row (0-indexed)
///
/// # Example
///
/// ```rust
/// use crossterm::cursor::MoveTo;
/// println!("{}", MoveTo(10, 5));
/// ```
pub struct MoveTo(pub u16, pub u16);
```

Becomes:

```kotlin
/**
 * A command that moves the cursor to the specified position.
 *
 * @param column The column (0-indexed)
 * @param row The row (0-indexed)
 *
 * Example:
 * ```kotlin
 * import io.github.kotlinmania.crossterm.cursor.MoveTo
 * print(MoveTo(10u, 5u).ansiString())
 * ```
 */
data class MoveTo(val column: UShort, val row: UShort) : Command
```

### Documentation Checklist

- [ ] Module-level docs (top of file)
- [ ] All public types (classes, objects, enums, sealed classes)
- [ ] All public functions/methods
- [ ] All public properties
- [ ] Examples where the Rust has them
- [ ] Convert Rust doc tests to Kotlin examples

---

## Semantic Parity Rules

### The "Dishonest Code" Rule

Port the *intent* and *behavior*, not just syntax.

**Bad:**
```kotlin
// Rust had: impl Display for ResetColor { fn fmt(..) { write!(f, "\x1B[0m") } }
// DON'T DO THIS:
data object ResetColor {
    override fun toString() = "ResetColor"  // WRONG!
}
```

**Good:**
```kotlin
data object ResetColor : Command {
    override fun writeAnsi(writer: Appendable) {
        writer.append("\u001B[0m")  // Correct ANSI sequence
    }
}
```

### Research First

- Don't guess Rust trait behavior
- Look up `std::` types in Rust docs
- Check bitflags behavior for flag types
- Verify ANSI escape sequences

---

## Swarm Task Management

### Initialize Tasks

```bash
cd /Volumes/stuff/Projects/kotlinmania/crossterm-kotlin
./tools/ast_distance/build/ast_distance --init-tasks tmp/crossterm-rs/src rust commonMain/src kotlin tasks.json AGENTS.md
```

### Swarm Agent Workflow

Each agent follows this loop:

```
1. GET ASSIGNMENT
   ./tools/ast_distance/build/ast_distance --assign tasks.json <agent-id>

2. READ SOURCE
   Read the Rust file at the path shown

3. CREATE KOTLIN FILE
   - Create proper folder structure matching package
   - Add port-lint header as FIRST line
   - Port all types, functions, and docs

4. VERIFY
   ./tools/ast_distance/build/ast_distance tmp/crossterm-rs/src/<file>.rs commonMain/src/<path>/<File>.kt
   Target: >= 0.85 similarity

5. COMPLETE
   ./tools/ast_distance/build/ast_distance --complete tasks.json <qualified-name>

6. REPEAT from step 1
```

### Task Commands

```bash
# View all tasks
./tools/ast_distance/build/ast_distance --tasks tasks.json

# Assign next task to an agent
./tools/ast_distance/build/ast_distance --assign tasks.json agent-001

# Mark task complete
./tools/ast_distance/build/ast_distance --complete tasks.json event.filter

# Release task back to pool (if agent can't finish)
./tools/ast_distance/build/ast_distance --release tasks.json event.filter
```

---

## Type Mapping Reference

### Primitives

| Rust | Kotlin |
|------|--------|
| `u8` | `UByte` |
| `u16` | `UShort` |
| `u32` | `UInt` |
| `u64` | `ULong` |
| `i8` | `Byte` |
| `i16` | `Short` |
| `i32` | `Int` |
| `i64` | `Long` |
| `char` | `Char` |
| `bool` | `Boolean` |
| `String` | `String` |
| `&str` | `String` |

### Common Patterns

| Rust | Kotlin |
|------|--------|
| `pub enum Foo { A, B(u8) }` | `sealed class Foo { data object A : Foo(); data class B(val value: UByte) : Foo() }` |
| `pub struct Bar { x: u16 }` | `data class Bar(val x: UShort)` |
| `impl Default for Foo` | `companion object { fun default(): Foo = ... }` |
| `impl Display for Foo` | `override fun toString(): String` or `fun writeAnsi(w: Appendable)` |
| `bitflags! { struct Flags: u8 { ... } }` | `value class Flags(val bits: UByte) { companion object { val FLAG_A = Flags(0x01u) } }` |
| `Option<T>` | `T?` |
| `Result<T, E>` | `Result<T>` or sealed class |
| `Vec<T>` | `List<T>` or `MutableList<T>` |
| `HashMap<K, V>` | `Map<K, V>` or `MutableMap<K, V>` |

### ANSI Sequences

```kotlin
// ESC character
const val ESC = "\u001B"

// CSI (Control Sequence Introducer)
const val CSI = "\u001B["

// Common sequences
"${CSI}H"        // Move to home
"${CSI}2J"       // Clear screen
"${CSI}?25h"     // Show cursor
"${CSI}?25l"     // Hide cursor
"${CSI}?1049h"   // Enter alternate screen
"${CSI}?1049l"   // Leave alternate screen
```

---

## Project Structure

```
crossterm-kotlin/
├── AGENTS.md                    # This file
├── README.md
├── build.gradle.kts
├── settings.gradle.kts
├── commonMain/src/              # Cross-platform code
│   ├── Command.kt               # Base Command interface
│   ├── cursor/                  # Cursor module
│   │   └── Cursor.kt
│   ├── event/                   # Event module
│   │   ├── Event.kt
│   │   ├── Filter.kt
│   │   └── ...
│   ├── style/                   # Style module
│   │   ├── Style.kt
│   │   └── types/
│   │       ├── Color.kt
│   │       └── Attribute.kt
│   └── terminal/                # Terminal module
│       └── Terminal.kt
├── nativeMain/src/              # Native-specific (POSIX)
│   ├── event/sys/unix/
│   │   └── Parse.kt
│   └── terminal/sys/
│       └── Unix.kt
├── mingwMain/src/               # Windows-specific
│   └── ...
├── tmp/crossterm-rs/            # Upstream Rust source (gitignored)
└── tools/ast_distance/          # Porting verification tool
```

---

## Quality Checklist

Before marking a file complete:

- [ ] Port-lint header is FIRST line
- [ ] Package matches folder structure
- [ ] All public types ported
- [ ] All public functions ported
- [ ] All documentation ported (KDoc format)
- [ ] ANSI sequences match exactly
- [ ] Examples converted to Kotlin
- [ ] Similarity score >= 0.85
- [ ] Compiles without errors
