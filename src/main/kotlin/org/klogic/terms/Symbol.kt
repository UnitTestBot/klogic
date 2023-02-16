package org.klogic.terms

import org.klogic.core.CustomTerm

/**
 * Represents a simple string constant.
 */
@JvmInline
value class Symbol(private val name: String) : CustomTerm<Symbol> {
    override val subtreesToUnify: Sequence<*>
        get() = sequenceOf(name)

    override fun constructFromSubtrees(subtrees: List<*>): CustomTerm<Symbol> =
        // A performance optimization - a symbol never changes, so we can omit arguments
        this

    override fun toString(): String = name

    companion object {
        fun String.toSymbol(): Symbol = Symbol(this)
    }
}
