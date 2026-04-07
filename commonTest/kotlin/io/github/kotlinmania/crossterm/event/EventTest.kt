// port-lint: source event.rs
package io.github.kotlinmania.crossterm.event

import kotlin.test.Test
import kotlin.test.assertEquals

class EventTest {
    @Test
    fun testEquality() {
        val lowercaseDWithShift = KeyEvent.new(KeyCode.Char('d'), KeyModifiers.SHIFT)
        val uppercaseDWithShift = KeyEvent.new(KeyCode.Char('D'), KeyModifiers.SHIFT)
        val uppercaseD = KeyEvent.new(KeyCode.Char('D'), KeyModifiers.NONE)

        assertEquals(lowercaseDWithShift, uppercaseDWithShift)
        assertEquals(uppercaseD, uppercaseDWithShift)
    }

    @Test
    fun testHash() {
        val lowercaseDWithShiftHash = KeyEvent.new(KeyCode.Char('d'), KeyModifiers.SHIFT).hashCode()
        val uppercaseDWithShiftHash = KeyEvent.new(KeyCode.Char('D'), KeyModifiers.SHIFT).hashCode()
        val uppercaseDHash = KeyEvent.new(KeyCode.Char('D'), KeyModifiers.NONE).hashCode()

        assertEquals(lowercaseDWithShiftHash, uppercaseDWithShiftHash)
        assertEquals(uppercaseDHash, uppercaseDWithShiftHash)
    }

    @Test
    fun keycodeDisplay() {
        assertEquals(keyCodeBackspaceDisplayName(), KeyCode.Backspace.toString())
        assertEquals(keyCodeDeleteDisplayName(), KeyCode.Delete.toString())
        assertEquals(keyCodeEnterDisplayName(), KeyCode.Enter.toString())

        assertEquals("Left", KeyCode.Left.toString())
        assertEquals("Right", KeyCode.Right.toString())
        assertEquals("Up", KeyCode.Up.toString())
        assertEquals("Down", KeyCode.Down.toString())
        assertEquals("Home", KeyCode.Home.toString())
        assertEquals("End", KeyCode.End.toString())
        assertEquals("Page Up", KeyCode.PageUp.toString())
        assertEquals("Page Down", KeyCode.PageDown.toString())
        assertEquals("Tab", KeyCode.Tab.toString())
        assertEquals("Back Tab", KeyCode.BackTab.toString())
        assertEquals("Insert", KeyCode.Insert.toString())
        assertEquals("F1", KeyCode.F(1u).toString())
        assertEquals("a", KeyCode.Char('a').toString())
        assertEquals("Space", KeyCode.Char(' ').toString())
        assertEquals("Null", KeyCode.Null.toString())
        assertEquals("Esc", KeyCode.Esc.toString())
        assertEquals("Caps Lock", KeyCode.CapsLock.toString())
        assertEquals("Scroll Lock", KeyCode.ScrollLock.toString())
        assertEquals("Num Lock", KeyCode.NumLock.toString())
        assertEquals("Print Screen", KeyCode.PrintScreen.toString())
        assertEquals("Pause", KeyCode.Pause.toString())
        assertEquals("Menu", KeyCode.Menu.toString())
        assertEquals("Begin", KeyCode.KeypadBegin.toString())
    }

    @Test
    fun mediaKeycodeDisplay() {
        assertEquals("Play", KeyCode.Media(MediaKeyCode.Play).toString())
        assertEquals("Pause", KeyCode.Media(MediaKeyCode.Pause).toString())
        assertEquals("Play/Pause", KeyCode.Media(MediaKeyCode.PlayPause).toString())
        assertEquals("Reverse", KeyCode.Media(MediaKeyCode.Reverse).toString())
        assertEquals("Stop", KeyCode.Media(MediaKeyCode.Stop).toString())
        assertEquals("Fast Forward", KeyCode.Media(MediaKeyCode.FastForward).toString())
        assertEquals("Rewind", KeyCode.Media(MediaKeyCode.Rewind).toString())
        assertEquals("Next Track", KeyCode.Media(MediaKeyCode.TrackNext).toString())
        assertEquals("Previous Track", KeyCode.Media(MediaKeyCode.TrackPrevious).toString())
        assertEquals("Record", KeyCode.Media(MediaKeyCode.Record).toString())
        assertEquals("Lower Volume", KeyCode.Media(MediaKeyCode.LowerVolume).toString())
        assertEquals("Raise Volume", KeyCode.Media(MediaKeyCode.RaiseVolume).toString())
        assertEquals("Mute Volume", KeyCode.Media(MediaKeyCode.MuteVolume).toString())
    }

    @Test
    fun modifierKeycodeDisplay() {
        assertEquals("Left Shift", KeyCode.Modifier(ModifierKeyCode.LeftShift).toString())
        assertEquals("Left Hyper", KeyCode.Modifier(ModifierKeyCode.LeftHyper).toString())
        assertEquals("Left Meta", KeyCode.Modifier(ModifierKeyCode.LeftMeta).toString())
        assertEquals("Right Shift", KeyCode.Modifier(ModifierKeyCode.RightShift).toString())
        assertEquals("Right Hyper", KeyCode.Modifier(ModifierKeyCode.RightHyper).toString())
        assertEquals("Right Meta", KeyCode.Modifier(ModifierKeyCode.RightMeta).toString())
        assertEquals("Iso Level 3 Shift", KeyCode.Modifier(ModifierKeyCode.IsoLevel3Shift).toString())
        assertEquals("Iso Level 5 Shift", KeyCode.Modifier(ModifierKeyCode.IsoLevel5Shift).toString())
        assertEquals(modifierKeyCodeLeftControlDisplayName(), KeyCode.Modifier(ModifierKeyCode.LeftControl).toString())
        assertEquals(modifierKeyCodeLeftAltDisplayName(), KeyCode.Modifier(ModifierKeyCode.LeftAlt).toString())
        assertEquals(modifierKeyCodeLeftSuperDisplayName(), KeyCode.Modifier(ModifierKeyCode.LeftSuper).toString())
        assertEquals(modifierKeyCodeRightControlDisplayName(), KeyCode.Modifier(ModifierKeyCode.RightControl).toString())
        assertEquals(modifierKeyCodeRightAltDisplayName(), KeyCode.Modifier(ModifierKeyCode.RightAlt).toString())
        assertEquals(modifierKeyCodeRightSuperDisplayName(), KeyCode.Modifier(ModifierKeyCode.RightSuper).toString())
    }

    @Test
    fun keyModifiersDisplay() {
        val modifiers = KeyModifiers.SHIFT + KeyModifiers.CONTROL + KeyModifiers.ALT

        val expected = listOf(
            "Shift",
            keyModifiersControlDisplayName(),
            keyModifiersAltDisplayName()
        ).joinToString("+")
        assertEquals(expected, modifiers.toString())
    }
}
