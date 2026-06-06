// port-lint: tests macros.rs
package io.github.kotlinmania.crossterm

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MacrosTest {
    @Test
    fun testQueueOne() {
        val result = FakeWrite()
        queue(result, FakeCommand())
        assertEquals("cmd", result.content)
        assertFalse(result.flushed)
    }

    @Test
    fun testQueueMany() {
        val result = FakeWrite()
        queue(result, FakeCommand(), FakeCommand())
        assertEquals("cmdcmd", result.content)
        assertFalse(result.flushed)
    }

    @Test
    fun testQueueTrailingComma() {
        val result = FakeWrite()
        queue(
            result,
            FakeCommand(),
            FakeCommand(),
        )
        assertEquals("cmdcmd", result.content)
        assertFalse(result.flushed)
    }

    @Test
    fun testExecuteOne() {
        val result = FakeWrite()
        execute(result, FakeCommand())
        assertEquals("cmd", result.content)
        assertTrue(result.flushed)
    }

    @Test
    fun testExecuteMany() {
        val result = FakeWrite()
        execute(result, FakeCommand(), FakeCommand())
        assertEquals("cmdcmd", result.content)
        assertTrue(result.flushed)
    }

    @Test
    fun testExecuteTrailingComma() {
        val result = FakeWrite()
        execute(
            result,
            FakeCommand(),
            FakeCommand(),
        )
        assertEquals("cmdcmd", result.content)
        assertTrue(result.flushed)
    }

    private class FakeWrite : ExecutableCommand {
        private val buffer = StringBuilder()
        var flushed: Boolean = false
            private set

        val content: String
            get() = buffer.toString()

        override fun append(value: Char): Appendable {
            buffer.append(value)
            flushed = false
            return this
        }

        override fun append(value: CharSequence?): Appendable {
            buffer.append(value)
            flushed = false
            return this
        }

        override fun append(
            value: CharSequence?,
            startIndex: Int,
            endIndex: Int,
        ): Appendable {
            buffer.append(value, startIndex, endIndex)
            flushed = false
            return this
        }

        override fun flush() {
            flushed = true
        }
    }

    private class FakeCommand(
        private val value: String = "cmd",
    ) : Command {
        override fun writeAnsi(writer: Appendable) {
            writer.append(value)
        }

        override fun isAnsiCodeSupported(): Boolean = true
    }
}
