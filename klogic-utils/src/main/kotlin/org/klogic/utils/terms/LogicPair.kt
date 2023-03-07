package org.klogic.utils.terms

import org.klogic.core.CustomTerm
import org.klogic.core.Term

/**
 * Represents a logic type for simple [Pair].
 */
data class LogicPair<A : Term<A>, B : Term<B>>(val first: Term<A>, val second: Term<B>) : CustomTerm<LogicPair<A, B>> {
    override val subtreesToUnify: Sequence<*> = sequenceOf(first, second)

    override fun constructFromSubtrees(subtrees: Iterable<*>): CustomTerm<LogicPair<A, B>> {
        // We use by-hand iteration here to avoid losing performance.
        val iterator = subtrees.iterator()
        val first = iterator.next()
        val second = iterator.next()

        require(!iterator.hasNext()) {
            "Expected only 2 elements for constructing LogicPair but got more elements"
        }

        @Suppress("UNCHECKED_CAST")
        return LogicPair(first as Term<A>, second as Term<B>)
    }
}

/**
 * Constructs [LogicPair] from [this] as [LogicPair.first] element and [that] as [LogicPair.second] element.
 */
infix fun <A : Term<A>, B : Term<B>> Term<A>.logicTo(that: Term<B>): LogicPair<A, B> = LogicPair(this, that)
