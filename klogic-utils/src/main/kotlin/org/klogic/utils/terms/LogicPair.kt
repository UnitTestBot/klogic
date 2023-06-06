package org.klogic.utils.terms

import org.klogic.core.Term

/**
 * Represents a logic type for simple [Pair].
 */
data class LogicPair<A : Term<A>, B : Term<B>>(
    override val first: Term<A>,
    override val second: Term<B>
) : BinaryTerm<LogicPair<A, B>, Term<A>, Term<B>>() {
    override val constructor: (Term<A>, Term<B>) -> LogicPair<A, B> = ::LogicPair

    override fun toString(): String = ("$first, $second")
}

/**
 * Constructs [LogicPair] from [this] as [LogicPair.first] element and [that] as [LogicPair.second] element,
 * just like [Pair.to] does.
 */
infix fun <A : Term<A>, B : Term<B>> Term<A>.logicTo(that: Term<B>): LogicPair<A, B> = LogicPair(this, that)
