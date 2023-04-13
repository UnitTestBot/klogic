package org.klogic.utils.terms

import org.klogic.core.CustomTerm

/**
 * A template for any [CustomTerm] that does not store any values - e.g., objects like [LogicBool].
 */
abstract class EmptyTerm<Term : CustomTerm<Term>> : CustomTerm<Term> {
    override val subtreesToUnify: Array<*> = emptyArray<Any?>()

    override fun constructFromSubtrees(subtrees: Iterable<*>): CustomTerm<Term> = this
}

/**
 * A template fpr any [CustomTerm] that stores one value of an arbitrary type.
 */
abstract class UnaryTerm<Term : CustomTerm<Term>, Value> : CustomTerm<Term> {
    abstract val value: Value
    abstract val constructor: (Value) -> UnaryTerm<Term, Value>

    override val subtreesToUnify: Array<*>
        get() = arrayOf<Any?>(value)

    override fun constructFromSubtrees(subtrees: Iterable<*>): CustomTerm<Term> {
        val value = subtrees.iterator().next()
        // Do not check other values for better performance

        @Suppress("UNCHECKED_CAST")
        return constructor(value as Value)
    }
}

/**
 * A template fpr any [CustomTerm] that stores two values of arbitrary types.
 */
abstract class BinaryTerm<Term : CustomTerm<Term>, FirstValue, SecondValue> : CustomTerm<Term> {
    abstract val first: FirstValue
    abstract val second: SecondValue
    abstract val constructor: (FirstValue, SecondValue) -> BinaryTerm<Term, FirstValue, SecondValue>

    override val subtreesToUnify: Array<*>
        get() = arrayOf(first, second)

    override fun constructFromSubtrees(subtrees: Iterable<*>): CustomTerm<Term> {
        val (first, second) = subtrees.takeFirstAndSecondElements()
        // Do not check other values for better performance

        @Suppress("UNCHECKED_CAST")
        return constructor(first as FirstValue, second as SecondValue)
    }

    private fun <T> Iterable<T>.takeFirstAndSecondElements(): Pair<T, T> {
        val iterator = iterator()

        // We use by-hand iteration here to avoid losing performance.
        return iterator.next() to iterator.next()
    }
}
