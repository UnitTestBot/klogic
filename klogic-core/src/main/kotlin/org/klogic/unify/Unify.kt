package org.klogic.unify

import org.klogic.core.*

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
    fun <T : Term<T>> unify(left: Term<T>, right: Term<T>): UnificationState? = unify(left, right, this)

    companion object {
        private val EMPTY: UnificationState = UnificationState()

        val empty: UnificationState
            get() = EMPTY
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

/**
 * Tries to unify [first] term and [second] terms of the same type using passed [unificationState].
 */
internal fun <T : Term<T>> unify(first: Term<T>, second: Term<T>, unificationState: UnificationState): UnificationState? {
    val walkedThis = first.walk(unificationState.substitution)
    val walkedOther = second.walk(unificationState.substitution)

    return walkedThis.unifyImpl(walkedOther, unificationState)
}
