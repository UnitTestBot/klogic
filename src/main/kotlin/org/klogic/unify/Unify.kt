package org.klogic.unify

import org.klogic.core.Cons
import org.klogic.core.Nil
import org.klogic.core.State
import org.klogic.core.Substitution
import org.klogic.core.Symbol
import org.klogic.core.Term
import org.klogic.core.Var

/**
 * Checks whether [variable] occurs in [term].
 */
internal fun occurs(term: Term, variable: Var): Boolean =
    when (term) {
        is Var -> term.index == variable.index
        is Cons -> occurs(term.head, variable) || occurs(term.tail, variable)
        is Symbol, Nil -> false
    }

/**
 * Substitutes all occurrences of [term] to its value in [substitution].
 */
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

/**
 * Represents a result of [UnificationResult.unify].
 */
data class UnificationResult(val newState: State, val substitutionDifference: MutableMap<Var, Term> = mutableMapOf()) {
    /**
     * Tries to unify two terms [left] and [right]. If it is possible, returns [UnificationResult] with [State] with
     * corresponding logic bounds for variables in [State.substitution] and [substitutionDifference] as a difference
     * between [State.substitution] before unification and after. Otherwise, returns null.
     */
    fun unify(left: Term, right: Term): UnificationResult? {
        val walkedLeft = walk(left, newState.substitution)
        val walkedRight = walk(right, newState.substitution)

        return when (walkedLeft) {
            is Var -> {
                when (walkedRight) {
                    is Var -> {
                        if (walkedLeft == walkedRight) {
                            this
                        } else {
                            val newAssociation = walkedLeft to walkedRight
                            substitutionDifference += newAssociation

                            copy(newState = newState + newAssociation)
                        }
                    }
                    is Symbol, is Cons, Nil -> {
                        if (occurs(walkedRight, walkedLeft)) {
                            null
                        } else {
                            val newAssociation = walkedLeft to walkedRight
                            substitutionDifference += newAssociation

                            copy(newState = newState + newAssociation)
                        }
                    }
                }
            }
            is Cons -> when (walkedRight) {
                is Cons -> {
                    unify(walkedLeft.head, walkedRight.head)?.unify(walkedLeft.tail, walkedRight.tail)
                }
                is Var -> unify(walkedRight, walkedLeft)
                is Symbol, Nil -> null
            }
            Nil -> when (walkedRight) {
                is Var -> unify(walkedRight, walkedLeft)
                Nil -> this
                is Symbol, is Cons -> null
            }
            is Symbol -> when (walkedRight) {
                is Var -> unify(walkedRight, walkedLeft)
                is Symbol -> if (walkedLeft == walkedRight) this else null
                is Cons, Nil -> null
            }
        }
    }

    companion object {
        private val EMPTY: UnificationResult = UnificationResult(State.empty)

        val empty: UnificationResult = EMPTY
    }
}

/**
 * Returns a [UnificationResult] with [UnificationResult.newState] equal to [this] and
 * empty [UnificationResult.substitutionDifference].
 */
fun State.toUnificationResult(): UnificationResult = UnificationResult(this)

/**
 * Tries to unify [left] and [right] terms, starting with empty [Substitution].
 *
 * @see [UnificationResult.unify] for details.
 */
fun unify(left: Term, right: Term): UnificationResult? = UnificationResult.empty.unify(left, right)
