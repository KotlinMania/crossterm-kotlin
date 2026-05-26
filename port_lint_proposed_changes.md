# port-lint Proposed Changes

**Generated:** 2026-05-26
**Source:** tmp
**Target:** commonMain/src

These are review proposals only. They are emitted when a Rust -> Kotlin pair matches only after fallback normalization, so the existing `port-lint` header is not an exact provenance match.

| Target file | Current header | Proposed header | Source path | Reason |
|-------------|----------------|-----------------|-------------|--------|
| `Command.kt` | `// port-lint: source command.rs` | `// port-lint: source command.rs` | `command.rs` | `port-lint provenance header matched only after fallback normalization: 'command.rs' vs expected 'command.rs'` |
| `event/Event.kt` | `// port-lint: source event.rs` | `// port-lint: source event.rs` | `event.rs` | `port-lint provenance header matched only after fallback normalization: 'event.rs' vs expected 'event.rs'` |
| `Clipboard.kt` | `// port-lint: source clipboard.rs` | `// port-lint: source clipboard.rs` | `clipboard.rs` | `port-lint provenance header matched only after fallback normalization: 'clipboard.rs' vs expected 'clipboard.rs'` |
| `style/types/Attribute.kt` | `// port-lint: source style/types/attribute.rs` | `// port-lint: source style/types/attribute.rs` | `style/types/attribute.rs` | `port-lint provenance header matched only after fallback normalization: 'style/types/attribute.rs' vs expected 'style/types/attribute.rs'` |
| `event/Read.kt` | `// port-lint: source event/read.rs` | `// port-lint: source event/read.rs` | `event/read.rs` | `port-lint provenance header matched only after fallback normalization: 'event/read.rs' vs expected 'event/read.rs'` |
| `style/types/Color.kt` | `// port-lint: source style/types/color.rs` | `// port-lint: source style/types/color.rs` | `style/types/color.rs` | `port-lint provenance header matched only after fallback normalization: 'style/types/color.rs' vs expected 'style/types/color.rs'` |
| `style/types/Colored.kt` | `// port-lint: source style/types/colored.rs` | `// port-lint: source style/types/colored.rs` | `style/types/colored.rs` | `port-lint provenance header matched only after fallback normalization: 'style/types/colored.rs' vs expected 'style/types/colored.rs'` |
| `style/types/AnsiColorDisabledOverride.kt` | `// port-lint: source style/types/colored.rs` | `// port-lint: source style/types/colored.rs` | `style/types/colored.rs` | `port-lint provenance header matched only after fallback normalization: 'style/types/colored.rs' vs expected 'style/types/colored.rs'` |
| `style/Style.kt` | `// port-lint: source style.rs` | `// port-lint: source style.rs` | `style.rs` | `port-lint provenance header matched only after fallback normalization: 'style.rs' vs expected 'style.rs'` |
| `cursor/Cursor.kt` | `// port-lint: source cursor.rs` | `// port-lint: source cursor.rs` | `cursor.rs` | `port-lint provenance header matched only after fallback normalization: 'cursor.rs' vs expected 'cursor.rs'` |
| `style/Attributes.kt` | `// port-lint: source style/attributes.rs` | `// port-lint: source style/attributes.rs` | `style/attributes.rs` | `port-lint provenance header matched only after fallback normalization: 'style/attributes.rs' vs expected 'style/attributes.rs'` |
| `event/Filter.kt` | `// port-lint: source event/filter.rs` | `// port-lint: source event/filter.rs` | `event/filter.rs` | `port-lint provenance header matched only after fallback normalization: 'event/filter.rs' vs expected 'event/filter.rs'` |
| `event/Timeout.kt` | `// port-lint: source event/timeout.rs` | `// port-lint: source event/timeout.rs` | `event/timeout.rs` | `port-lint provenance header matched only after fallback normalization: 'event/timeout.rs' vs expected 'event/timeout.rs'` |
| `event/Stream.kt` | `// port-lint: source event/stream.rs` | `// port-lint: source event/stream.rs` | `event/stream.rs` | `port-lint provenance header matched only after fallback normalization: 'event/stream.rs' vs expected 'event/stream.rs'` |
| `terminal/Terminal.kt` | `// port-lint: source terminal.rs` | `// port-lint: source terminal.rs` | `terminal.rs` | `port-lint provenance header matched only after fallback normalization: 'terminal.rs' vs expected 'terminal.rs'` |
| `style/Stylize.kt` | `// port-lint: source style/stylize.rs` | `// port-lint: source style/stylize.rs` | `style/stylize.rs` | `port-lint provenance header matched only after fallback normalization: 'style/stylize.rs' vs expected 'style/stylize.rs'` |
| `style/ContentStyle.kt` | `// port-lint: source style/content_style.rs` | `// port-lint: source style/content_style.rs` | `style/content_style.rs` | `port-lint provenance header matched only after fallback normalization: 'style/content_style.rs' vs expected 'style/content_style.rs'` |
| `style/StyledContentImpl.kt` | `// port-lint: source style/styled_content.rs` | `// port-lint: source style/styled_content.rs` | `style/styled_content.rs` | `port-lint provenance header matched only after fallback normalization: 'style/styled_content.rs' vs expected 'style/styled_content.rs'` |
| `style/types/Colors.kt` | `// port-lint: source style/types/colors.rs` | `// port-lint: source style/types/colors.rs` | `style/types/colors.rs` | `port-lint provenance header matched only after fallback normalization: 'style/types/colors.rs' vs expected 'style/types/colors.rs'` |
| `AnsiSupport.kt` | `// port-lint: source ansi_support.rs` | `// port-lint: source ansi_support.rs` | `ansi_support.rs` | `port-lint provenance header matched only after fallback normalization: 'ansi_support.rs' vs expected 'ansi_support.rs'` |
| `event/Internal.kt` | `// port-lint: source event/internal.rs` | `// port-lint: source event/internal.rs` | `event/internal.rs` | `port-lint provenance header matched only after fallback normalization: 'event/internal.rs' vs expected 'event/internal.rs'` |
| `Tty.kt` | `// port-lint: source tty.rs` | `// port-lint: source tty.rs` | `tty.rs` | `port-lint provenance header matched only after fallback normalization: 'tty.rs' vs expected 'tty.rs'` |
| `event/source/Source.kt` | `// port-lint: source event/source.rs` | `// port-lint: source event/source.rs` | `event/source.rs` | `port-lint provenance header matched only after fallback normalization: 'event/source.rs' vs expected 'event/source.rs'` |
| `event/sys/Waker.kt` | `// port-lint: source event/sys.rs` | `// port-lint: source event/sys.rs` | `event/sys.rs` | `port-lint provenance header matched only after fallback normalization: 'event/sys.rs' vs expected 'event/sys.rs'` |
| `Macros.kt` | `// port-lint: source macros.rs` | `// port-lint: source macros.rs` | `macros.rs` | `port-lint provenance header matched only after fallback normalization: 'macros.rs' vs expected 'macros.rs'` |
| `terminal/sys/Sys.kt` | `// port-lint: source terminal/sys.rs` | `// port-lint: source terminal/sys.rs` | `terminal/sys.rs` | `port-lint provenance header matched only after fallback normalization: 'terminal/sys.rs' vs expected 'terminal/sys.rs'` |
| `cursor/sys/Sys.kt` | `// port-lint: source cursor/sys.rs` | `// port-lint: source cursor/sys.rs` | `cursor/sys.rs` | `port-lint provenance header matched only after fallback normalization: 'cursor/sys.rs' vs expected 'cursor/sys.rs'` |
