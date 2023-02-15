package org.klogic.terms

import org.klogic.core.CustomTerm
import org.klogic.core.Substitution
import org.klogic.core.Term
import org.klogic.core.Var
import org.klogic.unify.UnificationState

/**
 * Represents a simple string constant.
 */
@JvmInline
value class Symbol(private val name: String) : CustomTerm<Symbol> {
    override fun occurs(variable: Var<out Any>): Boolean = false

    override fun walk(substitution: Substitution): CustomTerm<Symbol> = this

    override fun unifyCustomTermImpl(walkedOther: CustomTerm<Symbol>, unificationState: UnificationState): UnificationState? =
        if (this == walkedOther) unificationState else null

    override fun toString(): String = name

    companion object {
        fun String.toSymbol(): Symbol = Symbol(this)
    }
}
