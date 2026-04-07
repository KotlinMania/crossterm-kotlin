// port-lint: tests style/attributes.rs
package io.github.kotlinmania.crossterm.style

import io.github.kotlinmania.crossterm.style.types.Attribute
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for [Attributes].
 *
 * These tests correspond to the tests in the Rust style/attributes.rs module.
 */
class AttributesTest {

    @Test
    fun testAttributes() {
        var attributes: Attributes = Attributes.from(Attribute.Bold)
        assertTrue(attributes.has(Attribute.Bold))

        attributes = attributes.set(Attribute.Italic)
        assertTrue(attributes.has(Attribute.Italic))

        attributes = attributes.unset(Attribute.Italic)
        assertFalse(attributes.has(Attribute.Italic))

        attributes = attributes.toggle(Attribute.Bold)
        assertTrue(attributes.isEmpty())
    }

    @Test
    fun testAttributesConst() {
        // In Kotlin, we use chained method calls instead of const evaluation
        val attributes = Attributes.none()
            .with(Attribute.Bold)
            .with(Attribute.Italic)
            .without(Attribute.Bold)

        assertFalse(attributes.has(Attribute.Bold))
        assertTrue(attributes.has(Attribute.Italic))
    }
}
