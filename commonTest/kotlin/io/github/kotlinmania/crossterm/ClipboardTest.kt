// port-lint: tests clipboard.rs
package io.github.kotlinmania.crossterm

import kotlin.test.Test
import kotlin.test.assertEquals

class ClipboardTest {

    @Test
    fun testClipboardStringToSelection() {
        assertEquals(
            ClipboardSelection(listOf(ClipboardType.Primary)),
            ClipboardSelection.fromString("p")
        )
        assertEquals(
            ClipboardSelection(emptyList()),
            ClipboardSelection.fromString("")
        )
        assertEquals(
            ClipboardSelection(listOf(ClipboardType.Clipboard, ClipboardType.Primary)),
            ClipboardSelection.fromString("cp")
        )
    }

    @Test
    fun testClipboardSelectionToOsc52Pc() {
        assertEquals("", ClipboardSelection(emptyList()).toOsc52Pc())
        assertEquals("c", ClipboardSelection(listOf(ClipboardType.Clipboard)).toOsc52Pc())
        assertEquals("p", ClipboardSelection(listOf(ClipboardType.Primary)).toOsc52Pc())
        assertEquals(
            "pc",
            ClipboardSelection(listOf(ClipboardType.Primary, ClipboardType.Clipboard)).toOsc52Pc()
        )
        assertEquals(
            "cp",
            ClipboardSelection(listOf(ClipboardType.Clipboard, ClipboardType.Primary)).toOsc52Pc()
        )
        assertEquals("s", ClipboardSelection(listOf(ClipboardType.Other('s'))).toOsc52Pc())
    }

    @Test
    fun testClipboardCopyStringOsc52() {
        assertEquals(
            "\u001B]52;c;Zm9v\u001B\\",
            CopyToClipboard(
                content = "foo".encodeToByteArray(),
                destination = ClipboardSelection(listOf(ClipboardType.Clipboard))
            ).ansiString()
        )

        assertEquals(
            "\u001B]52;p;Zm9v\u001B\\",
            CopyToClipboard(
                content = "foo".encodeToByteArray(),
                destination = ClipboardSelection(listOf(ClipboardType.Primary))
            ).ansiString()
        )

        assertEquals(
            "\u001B]52;pc;Zm9v\u001B\\",
            CopyToClipboard(
                content = "foo".encodeToByteArray(),
                destination = ClipboardSelection(listOf(ClipboardType.Primary, ClipboardType.Clipboard))
            ).ansiString()
        )

        assertEquals(
            "\u001B]52;;Zm9v\u001B\\",
            CopyToClipboard(
                content = "foo".encodeToByteArray(),
                destination = ClipboardSelection(emptyList())
            ).ansiString()
        )
    }

    @Test
    fun testClipboardCopyStringOsc52Constructor() {
        assertEquals(
            "\u001B]52;c;Zm9v\u001B\\",
            CopyToClipboard.toClipboardFrom("foo").ansiString()
        )
        assertEquals(
            "\u001B]52;p;Zm9v\u001B\\",
            CopyToClipboard.toPrimaryFrom("foo").ansiString()
        )
    }
}

