// port-lint: source event.rs
package io.github.kotlinmania.crossterm.event

internal actual fun keyCodeBackspaceDisplayName(): String = "Delete"
internal actual fun keyCodeDeleteDisplayName(): String = "Fwd Del"
internal actual fun keyCodeEnterDisplayName(): String = "Return"

internal actual fun keyModifiersControlDisplayName(): String = "Control"
internal actual fun keyModifiersAltDisplayName(): String = "Option"
internal actual fun keyModifiersSuperDisplayName(): String = "Command"

internal actual fun modifierKeyCodeLeftControlDisplayName(): String = "Left Control"
internal actual fun modifierKeyCodeLeftAltDisplayName(): String = "Left Option"
internal actual fun modifierKeyCodeLeftSuperDisplayName(): String = "Left Command"
internal actual fun modifierKeyCodeRightControlDisplayName(): String = "Right Control"
internal actual fun modifierKeyCodeRightAltDisplayName(): String = "Right Option"
internal actual fun modifierKeyCodeRightSuperDisplayName(): String = "Right Command"
