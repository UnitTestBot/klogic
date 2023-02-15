package org.klogic.core

import org.klogic.core.RecursiveStream.Companion.nil
import org.klogic.core.RecursiveStream.Companion.single
import org.klogic.unify.UnificationState

/**
 * Represents a logic object.
 */
interface Term<T : Term<T>> {
    /**
     * Checks whether [variable] occurs in this term.
     */
    fun <R : Term<R>> occurs(variable: Var<R>): Boolean

    /**
     * Substitutes all occurrences of this term to its value in [substitution].
     */
    fun walk(substitution: Substitution): Term<T>

    /**
     * Tries to unify this term and [other] term with the same type using passed [unificationState].
     */
    fun unify(other: Term<T>, unificationState: UnificationState): UnificationState? {
        val walkedThis = walk(unificationState.substitution)
        val walkedOther = other.walk(unificationState.substitution)

        return walkedThis.unifyImpl(walkedOther, unificationState)
    }

    fun unifyImpl(walkedOther: Term<T>, unificationState: UnificationState): UnificationState?

    /**
     * Tries to unify this term to [other] term of the same type.
     * If succeeds, returns a [Goal] with [RecursiveStream] containing single [State] with a corresponding [Substitution],
     * and a goal with the [nil] stream otherwise.
     *
     * @see [State.unifyWithConstraintsVerification] for details.
     */
    infix fun unify(other: Term<T>): Goal = { st: State ->
        st.unifyWithConstraintsVerification(this, other)?.let {
            single(it)
        } ?: nil()
    }

    /**
     * Returns a goal that contains one of the following:
     * - Copy of the passed state with an [InequalityConstraint] of this term and [other] term of the same type,
     * if this constraint can be satisfied somehow;
     * - Passed state (the same reference), if the mentioned above constraint can never be violated (i.e., it is redundant);
     * - No state at all, if this constraint is violated.
     *
     * @see [Substitution.ineq] for details.
     */
    infix fun ineq(other: Term<T>): Goal = { st: State ->
        st.substitution.ineq(this, other).let {
            when (it) {
                ViolatedConstraintResult -> nil()
                RedundantConstraintResult -> single(st)
                is SatisfiableConstraintResult -> {
                    val newConstraint = it.simplifiedConstraint
                    val newState = st.copy(constraints = st.constraints.add(newConstraint))

                    single(newState)
                }
            }
        }
    }

    /**
     * Unsafely casts this term to the term of the passed type.
     *
     * NOTE: this API is NOT safe and should be used very carefully.
     */
    @Suppress("UNCHECKED_CAST")
    fun <R : Term<R>> cast(): Term<R> = this as Term<R>

    infix fun `===`(other: Term<T>): Goal = this unify other
    infix fun `!==`(other: Term<T>): Goal = this ineq other
}

/**
 * Represents a symbolic term with the specified term that can be equal to any other [Term] of the same type.
 */
@JvmInline
value class Var<T : Term<T>>(val index: Int) : Term<T> {
    override fun <R : Term<R>> occurs(variable: Var<R>): Boolean = this == variable

    override fun walk(substitution: Substitution): Term<T> = substitution[this]?.let {
        (it.walk(substitution))
    } ?: this

    override fun unifyImpl(walkedOther: Term<T>, unificationState: UnificationState): UnificationState? {
        return if (walkedOther is Var<T>) {
            if (this == walkedOther) {
                unificationState
            } else {
                val newAssociation: Pair<Var<T>, Var<T>> = this to walkedOther

                unificationState.substitutionDifference[newAssociation.first.cast()] = newAssociation.second.cast()

                unificationState.copy(substitution = unificationState.substitution + newAssociation)
            }
        } else {
            if (walkedOther.occurs(this)) {
                null
            } else {
                val newAssociation = this to walkedOther
                unificationState.substitutionDifference[newAssociation.first] = newAssociation.second.cast()

                unificationState.copy(substitution = unificationState.substitution + newAssociation)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T2 : Term<T2>> cast(): Var<T2> = this as Var<T2>

    override fun toString(): String = "_.$index"

    companion object {
        fun <T :Term<T>> Int.createTypedVar(): Var<T> = Var(this)
    }
}

/**
 * Represents a custom (i.e., defined by user) term.
 */
interface CustomTerm<T : CustomTerm<T>> : Term<T> {
    override fun unifyImpl(walkedOther: Term<T>, unificationState: UnificationState): UnificationState? {
        if (walkedOther !is CustomTerm) {
            return walkedOther.unify(this, unificationState)
        }

        return unifyCustomTermImpl(walkedOther, unificationState)
    }

    /**
     * Tries to unify this user's term with another user's term with the same type.
     */
    fun unifyCustomTermImpl(walkedOther: CustomTerm<T>, unificationState: UnificationState): UnificationState?
}
