// port-lint: source style/attributes.rs
package io.github.kotlinmania.crossterm.style

import io.github.kotlinmania.crossterm.style.types.Attribute
import io.github.kotlinmania.crossterm.style.types.Color

/**
 * A bitset for all possible attributes.
 *
 * This class provides efficient storage and manipulation of multiple [Attribute]s
 * using bitwise operations on an underlying [UInt] value.
 *
 * Example:
 * ```kotlin
 * import io.github.kotlinmania.crossterm.style.Attributes
 * import io.github.kotlinmania.crossterm.style.types.Attribute
 *
 * // Create from a single attribute
 * val attrs = Attributes.from(Attribute.Bold)
 *
 * // Build using fluent API
 * val styled = Attributes.none()
 *     .with(Attribute.Bold)
 *     .with(Attribute.Italic)
 *     .without(Attribute.Bold)
 *
 * // Check attributes
 * println(styled.has(Attribute.Italic)) // true
 * println(styled.has(Attribute.Bold))   // false
 * ```
 */
data class Attributes(val bits: UInt = 0u) {

    /**
     * Returns a copy of the bitset with the given attribute set.
     * If it's already set, this returns the bitset unmodified.
     */
    fun with(attribute: Attribute): Attributes =
        Attributes(bits or attribute.bytes())

    /**
     * Returns a copy of the bitset with the given attribute unset.
     * If it's not set, this returns the bitset unmodified.
     */
    fun without(attribute: Attribute): Attributes =
        Attributes(bits and attribute.bytes().inv())

    /**
     * Sets the attribute.
     * If it's already set, this does nothing.
     *
     * Note: Since this is a value class, this returns a new instance.
     * Use the returned value.
     */
    fun set(attribute: Attribute): Attributes =
        Attributes(bits or attribute.bytes())

    /**
     * Unsets the attribute.
     * If it's not set, this changes nothing.
     *
     * Note: Since this is a value class, this returns a new instance.
     * Use the returned value.
     */
    fun unset(attribute: Attribute): Attributes =
        Attributes(bits and attribute.bytes().inv())

    /**
     * Sets the attribute if it's unset, unsets it if it is set.
     *
     * Note: Since this is a value class, this returns a new instance.
     * Use the returned value.
     */
    fun toggle(attribute: Attribute): Attributes =
        Attributes(bits xor attribute.bytes())

    /**
     * Returns whether the attribute is set.
     */
    fun has(attribute: Attribute): Boolean =
        (bits and attribute.bytes()) != 0u

    /**
     * Sets all the passed attributes. Removes none.
     *
     * Note: Since this is a value class, this returns a new instance.
     * Use the returned value.
     */
    fun extend(attributes: Attributes): Attributes =
        Attributes(bits or attributes.bits)

    /**
     * Returns whether there is no attribute set.
     */
    fun isEmpty(): Boolean = bits == 0u

    /**
     * Bitwise AND with an attribute.
     */
    infix fun and(attribute: Attribute): Attributes =
        Attributes(bits and attribute.bytes())

    /**
     * Bitwise AND with another Attributes.
     */
    infix fun and(other: Attributes): Attributes =
        Attributes(bits and other.bits)

    /**
     * Bitwise OR with an attribute.
     */
    infix fun or(attribute: Attribute): Attributes =
        Attributes(bits or attribute.bytes())

    /**
     * Bitwise OR with another Attributes.
     */
    infix fun or(other: Attributes): Attributes =
        Attributes(bits or other.bits)

    /**
     * Bitwise XOR with an attribute.
     */
    infix fun xor(attribute: Attribute): Attributes =
        Attributes(bits xor attribute.bytes())

    /**
     * Bitwise XOR with another Attributes.
     */
    infix fun xor(other: Attributes): Attributes =
        Attributes(bits xor other.bits)

    companion object {
        /**
         * Returns the empty bitset.
         */
        fun none(): Attributes = Attributes(0u)

        /**
         * Returns the default (empty) bitset.
         */
        fun default(): Attributes = none()

        /**
         * Creates an Attributes bitset from a single attribute.
         */
        fun from(attribute: Attribute): Attributes =
            Attributes(attribute.bytes())

        /**
         * Creates an Attributes bitset from a list of attributes.
         */
        fun from(attributes: List<Attribute>): Attributes {
            var result = none()
            for (attr in attributes) {
                result = result.set(attr)
            }
            return result
        }

        /**
         * Creates an Attributes bitset from vararg attributes.
         */
        fun of(vararg attributes: Attribute): Attributes =
            from(attributes.toList())
    }
}
