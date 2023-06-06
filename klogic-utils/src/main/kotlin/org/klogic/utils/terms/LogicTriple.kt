package org.klogic.utils.terms

import org.klogic.core.CustomTerm
import org.klogic.core.Term

/**
 * Represents a logic type for simple [Triple].
 */
data class LogicTriple<A : Term<A>, B : Term<B>, C : Term<C>>(
    val first: Term<A>,
    val second: Term<B>,
    val third: Term<C>
) : CustomTerm<LogicTriple<A, B, C>> {
    override val subtreesToUnify: Array<*>
        get() = arrayOf(first, second, third)

    @Suppress("UNCHECKED_CAST")
    override fun constructFromSubtrees(subtrees: Iterable<*>): CustomTerm<LogicTriple<A, B, C>> {
        val iterator = subtrees.iterator()

        // We use by-hand iteration here to avoid losing performance.
        val first = iterator.next()
        val second = iterator.next()
        val third = iterator.next()
        // Do not check other values for better performance

        return LogicTriple(first as Term<A>, second as Term<B>, third as Term<C>)
    }

    override fun toString(): String = ("$first, $second, $third")
}
