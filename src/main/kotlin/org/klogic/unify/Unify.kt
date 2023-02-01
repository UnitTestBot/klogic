package org.klogic.unify

import org.klogic.core.Cons
import org.klogic.core.Nil
import org.klogic.core.State
import org.klogic.core.Substitution
import org.klogic.core.Symbol
import org.klogic.core.Term
import org.klogic.core.Var

internal fun occurs(variable: Var, term: Term): Boolean {
    return when (term) {
        is Var -> term.index == variable.index
        is Cons -> occurs(variable, term.head) || occurs(variable, term.tail)
        is Symbol, Nil -> false
    }
}

internal fun walk(term: Term, substitution: Substitution): Term =
    when (term) {
        is Var -> {
            substitution[term]?.let {
                walk(it, substitution)
            } ?: term
        }
        is Cons -> {
            val head = walk(term.head, substitution)
            val tail = walk(term.tail, substitution)

            Cons(head, tail)
        }
        is Symbol, Nil -> term
    }

fun unify(left: Term, right: Term): State? = State.empty.unify(left, right)
