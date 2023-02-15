package org.klogic.unify

import org.klogic.core.State
import org.klogic.core.Substitution
import org.klogic.core.Term
import org.klogic.core.Var

/**
 * Represents a mutable state of unification process.
 *
 * @property substitution initial [Substitution] before starting unification process.
 * @property substitutionDifference difference between initial [substitution] and [Substitution] after unification process.
 */
data class UnificationState(
    val substitution: Substitution = Substitution.empty,
    val substitutionDifference: MutableMap<Var<*>, Term<*>> = mutableMapOf()
) {
    /**
     * Tries to unify two terms of the same type [left] and [right] using [substitution].
     */
    fun <T : Term<T>> unify(left: Term<T>, right: Term<T>): UnificationState? = left.unify(right, this)

    companion object {
        private val EMPTY: UnificationState = UnificationState()

        val empty: UnificationState = EMPTY
    }
}

/**
 * Returns a [UnificationState] with [UnificationState.substitution] equal to this [State.substitution] and
 * empty [UnificationState.substitutionDifference].
 */
fun State.toUnificationState(): UnificationState = substitution.toUnificationState()

/**
 * Returns a [UnificationState] with [UnificationState.substitution] equal to this and
 * empty [UnificationState.substitutionDifference].
 */
fun Substitution.toUnificationState(): UnificationState = UnificationState(this)

/**
 * Tries to unify [left] and [right] terms, starting with empty [Substitution].
 *
 * @see [State.unifyWithConstraintsVerification] for details.
 */
fun <T : Term<T>> unifyWithConstraintsVerification(
    left: Term<T>,
    right: Term<T>
): State? = State.empty.unifyWithConstraintsVerification(left, right)
