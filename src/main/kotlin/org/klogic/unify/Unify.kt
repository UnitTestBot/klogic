package org.klogic.unify

import org.klogic.types.Cons
import org.klogic.types.Nil
import org.klogic.types.Symbol
import org.klogic.types.Term
import org.klogic.types.Var

typealias Substitution = Map<Int, Term>

fun occurs(substitution: Substitution, variableIndex: Int, term: Term): Boolean = false

fun walk(term: Term, substitution: Substitution): Term {
    return when (term) {
        is Var -> {
            val substituted = substitution[term.index]

            substituted?.let {
                walk(substituted, substitution)
            } ?: term
        }
        is Cons -> Cons(walk(term.head, substitution), walk(term.tail, substitution))
        Nil, is Symbol -> term
    }
}

fun unify(substitution: Substitution, left: Term, right: Term): Substitution? {
    val left = walk(left, substitution)
    val right = walk(right, substitution)

    return when (left) {
        is Var -> {
            val result = substitution.toMutableMap()

            when (right) {
                is Var -> {
                    if (left == right) {
                        substitution
                    } else {
                        result[left.index] = right
                        result
                    }
                }
                else -> {
                    if (occurs(substitution, left.index, right)) {
                        null
                    } else {
                        result[left.index] = right
                        result
                    }
                }
            }
        }
        is Cons -> when (right) {
            is Cons -> {
                unify(substitution, left.head, right.head)?.let {
                    unify(it, left.tail, right.tail)
                }
            }
            is Var -> unify(substitution, right, left)
            else -> null
        }
        Nil -> when (right) {
            is Var -> unify(substitution, right, left)
            Nil -> substitution
            else -> null
        }
        is Symbol -> when (right) {
            is Var -> unify(substitution, right, left)
            is Symbol -> if (left == right) {
                substitution
            } else {
                null
            }
            else -> null
        }
    }
}
