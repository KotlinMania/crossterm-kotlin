# Rust/crossterm-rs

In the tmp/crossterm-rs folder where the upstream Rust code lives for reference.

## Kotlin Porting Guidelines

### Semantic Parity (The "Dishonest Code" Rule)
- **Rule:** Port the *intent* and *behavior* of the code, not just the syntax.
- **Context:** Rust's `Display` trait often implies specific formatting contracts (e.g., ANSI codes, truncation, padding) that are critical for terminal output.
- **Warning:** Do **not** oversimplify `impl Display` to a simple `toString()` that returns a constant or a raw value if the original code performed formatting.
    - *Bad Example:* Replacing a `Display` impl that writes ANSI sequences with `fun toString() = "RESET"`.
    - *Good Example:* Faithfully reproducing the ANSI escape sequence generation logic.
- **Action:** When porting `Display` or `Debug`, check if the Rust code does more than just return a field. If so, the Kotlin `toString()` (or a helper method) must replicate that logic.

### Research First
- **Rule:** Do not guess at the behavior of Rust functions or traits.
- **Action:** Use the browser to look up the official Rust documentation if you are not 100% sure of the semantics.
- **Context:** Rust's type system and traits often carry subtle behaviors that are not obvious from the function signature alone.

### Provenance Tracking (REQUIRED)

**Every ported Kotlin file MUST include a port-lint header at the top:**

```kotlin
// port-lint: source <relative-path-to-rust-file>
package io.github.kotlinmania.crossterm.module
```

**Examples:**
```kotlin
// port-lint: source event.rs
package io.github.kotlinmania.crossterm.event

// port-lint: source terminal.rs
package io.github.kotlinmania.crossterm.terminal

// port-lint: source cursor.rs
package io.github.kotlinmania.crossterm.cursor
```

**Purpose:**
- Enables accurate AST comparison and similarity scoring
- Makes file matching deterministic (no heuristic guessing)
- Supports documentation coverage analysis
- Tracks which Rust file each Kotlin port came from

**Rules:**
1. Header must appear in the first 50 lines (conventionally first line)
2. Path is relative to crossterm `src/` root
3. Use exact path including subdirectories (e.g., `event/sys/unix/parse.rs`)
4. Header is case-insensitive but prefer lowercase

**Verification:**
The AST distance tool will match files by port-lint header first, then fall back to name-based heuristics. Check coverage with:
```bash
./tools/ast_distance/build/ast_distance --deep tmp/crossterm-rs/src rust commonMain/src kotlin
# Shows "Matched by header: X / Y" statistics
```

---

## AST Distance Tool

A vendored cross-language AST comparison tool for analyzing port progress and identifying priority files. **Integrated with port-lint provenance tracking.**

### Location
```
tools/ast_distance/
├── CMakeLists.txt
├── README.md
├── PORT_LINT_TESTS.md  # Test results and examples
├── include/
│   ├── ast_parser.hpp      # Tree-sitter parsing for Rust/Kotlin/C++
│   ├── codebase.hpp        # Directory scanning, dependency graphs, matching
│   ├── imports.hpp         # Import/include extraction, package detection
│   ├── node_types.hpp      # Normalized AST node type mappings
│   ├── port_lint.hpp       # Port-lint header extraction and matching
│   ├── similarity.hpp      # Cosine similarity, combined scoring
│   └── tree.hpp            # Tree data structure
└── src/
    ├── main.cpp            # CLI entry point
    ├── ast_parser.cpp
    ├── ast_normalizer.cpp
    └── similarity.cpp
```

### Build
```bash
cd tools/ast_distance
mkdir -p build && cd build
cmake .. && make -j8
```

### Commands

**Analyze this project (Rust → Kotlin):**
```bash
./ast_distance --deep tmp/crossterm-rs/src rust commonMain/src kotlin
```

This will:
1. Match files by `// port-lint: source` header (highest priority)
2. Fall back to legacy `Transliterated from:` headers
3. Use name-based heuristics for unmatched files
4. Compute AST similarity scores for all matches
5. Report statistics: "Matched by header: X / Y"

**Check what's missing:**
```bash
./ast_distance --missing tmp/crossterm-rs/src rust commonMain/src kotlin
```

**Compare two files directly:**
```bash
./ast_distance tmp/crossterm-rs/src/event.rs commonMain/src/Event.kt
```

**Dump AST structure:**
```bash
./ast_distance --dump <file> <rust|kotlin>
```

### Output Interpretation

The `--deep` command outputs:
- **Matched files** with similarity scores (0.0–1.0)
- **Priority score** = dependents × (1 - similarity) — high priority = many dependents + low similarity
- **Incomplete ports** (similarity < 60%)
- **Missing files** not yet ported

Similarity thresholds:
- `> 0.85` — Excellent port, likely complete
- `0.60–0.85` — Good port, may need refinement
- `0.40–0.60` — Partial port, significant gaps
- `< 0.40` — Stub or very different implementation

---

## Swarm Task Management

The AST distance tool includes a task assignment system for coordinating multiple agents porting files in parallel.

### Initialize Tasks

Generate a task file from missing/incomplete ports:
```bash
./ast_distance --init-tasks tmp/crossterm-rs/src rust commonMain/src kotlin tasks.json AGENTS.md
```

### View Task Status

```bash
./ast_distance --tasks tasks.json
```

### Assign a Task (for Swarm Agents)

Each agent requests the next highest-priority unassigned task:
```bash
./ast_distance --assign tasks.json agent-001
```

This command:
- Assigns the highest-priority pending task (by dependent count)
- Prevents duplicate assignments (one task per agent)
- Outputs complete porting instructions including AGENTS.md guidelines
- Updates the task file with assignment timestamp

### Complete a Task

After successfully porting a file:
```bash
./ast_distance --complete tasks.json event
```

### Release a Task

If an agent cannot complete a task, release it back to pending:
```bash
./ast_distance --release tasks.json event
```

### Task Workflow for Swarm Agents

1. **Get assignment**: `ast_distance --assign tasks.json <agent-id>`
2. **Read source file** at the path shown
3. **Create target file** with port-lint header
4. **Transliterate** following the guidelines above
5. **Verify**: `ast_distance <source.rs> rust <target.kt> kotlin` (aim for >= 0.85 similarity)
6. **Complete**: `ast_distance --complete tasks.json <source_qualified>`
7. **Repeat** from step 1

---

## Crossterm-Specific Notes

### Module Structure

The Rust crossterm crate has this structure:
- `event.rs` + `event/` - Event types and reading (KeyEvent, MouseEvent, etc.)
- `terminal.rs` + `terminal/` - Terminal control (raw mode, alternate screen)
- `cursor.rs` + `cursor/` - Cursor movement and visibility
- `style.rs` + `style/` - Colors and text attributes
- `command.rs` - Command trait for ANSI sequence generation

### Platform-Specific Code

Crossterm has platform-specific implementations in `sys/` subdirectories:
- `event/sys/unix/` - Unix event reading (termios, etc.)
- `event/sys/windows/` - Windows console API
- `terminal/sys/unix.rs` - Unix terminal control
- `terminal/sys/windows.rs` - Windows terminal control

For Kotlin Multiplatform:
- Common code goes in `commonMain/src/`
- Unix-specific code goes in `nativeMain/src/` (using POSIX cinterop)
- Windows-specific code would go in `mingwMain/src/` if needed

### ANSI Escape Sequences

Most crossterm commands generate ANSI escape sequences. These are portable across platforms when writing to stdout. The Kotlin port should:
1. Implement `Command` interface with `writeAnsi(Appendable)` method
2. Generate the same ANSI sequences as the Rust original
3. Use `\u001B` (ESC) for escape character, not `\x1B`

### Testing

Since this is a terminal library, many features can't be easily unit tested. Focus on:
- ANSI sequence generation (can be tested by checking output strings)
- Event parsing (can be tested with mock input)
- Type conversions and data structures
