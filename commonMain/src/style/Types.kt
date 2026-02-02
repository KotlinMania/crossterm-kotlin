// port-lint: source style/types.rs
package io.github.kotlinmania.crossterm.style

/**
 * Style types module.
 *
 * This module re-exports the core style types used throughout the crossterm library:
 *
 * - [Attribute] - Text attributes like bold, italic, underline, etc.
 * - [Color] - Terminal colors including basic colors, 256-color palette, and RGB
 * - [Colored] - Represents colored content (foreground or background)
 * - [Colors] - A pair of foreground and background colors
 *
 * These types are defined in their respective files in the `style/types/` package:
 * - `style/types/Attribute.kt`
 * - `style/types/Color.kt`
 * - `style/types/Colored.kt`
 * - `style/types/Colors.kt`
 *
 * Example:
 * ```kotlin
 * import io.github.kotlinmania.crossterm.style.types.Color
 * import io.github.kotlinmania.crossterm.style.types.Attribute
 * import io.github.kotlinmania.crossterm.style.types.Colored
 * import io.github.kotlinmania.crossterm.style.types.Colors
 *
 * // Using colors
 * val fgColor = Color.Red
 * val bgColor = Color.Rgb(255u, 128u, 0u)
 *
 * // Using attributes
 * val attr = Attribute.Bold
 *
 * // Using Colored for foreground/background
 * val colored = Colored.ForegroundColor(Color.Green)
 *
 * // Using Colors for a color pair
 * val colors = Colors(foreground = Color.White, background = Color.Black)
 * ```
 */

// Note: In Kotlin, we don't need explicit re-exports like Rust's `pub use`.
// All public types from the style.types package are automatically accessible
// when importing io.github.kotlinmania.crossterm.style.types.*
//
// The types are defined in:
// - types/Attribute.kt - Text attributes (Bold, Italic, etc.)
// - types/Color.kt - Terminal colors (basic, 256-color, RGB)
// - types/Colored.kt - Foreground/background color wrapper
// - types/Colors.kt - Foreground and background color pair
