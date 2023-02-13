package org.klogic.unify

import org.klogic.core.State
import org.klogic.core.Substitution
import org.klogic.core.Term
import org.klogic.core.Var

// TODO docs.
/**
 * Represents a mutable state of unification process.
 */
data class UnificationState(
    val substitution: Substitution = Substitution.empty,
    val substitutionDifference: MutableMap<Var<Any>, Term<Any>> = mutableMapOf()
) {
    fun <T : Any> unify(left: Term<T>, right: Term<T>) = left.unify(right, this)

    companion object {
        private val EMPTY: UnificationState = UnificationState()

        val empty: UnificationState = EMPTY
    }
}

/**
 * Returns a [UnificationState] with [UnificationState.newState] equal to [this] and
 * empty [UnificationState.substitutionDifference].
 */
fun State.toUnificationState(): UnificationState = substitution.toUnificationState()

fun Substitution.toUnificationState(): UnificationState = UnificationState(this)

/**
 * Tries to unify [left] and [right] terms, starting with empty [Substitution].
 *
 * @see [UnificationState.unifyWithConstraintsVerification] for details.
 */
fun <T : Any> unifyWithConstraintsVerification(
    left: Term<T>,
    right: Term<T>
): State? = State.empty.unifyWithConstraintsVerification(left, right)
