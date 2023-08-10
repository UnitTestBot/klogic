package org.klogic.core

import org.klogic.core.RecursiveStream.Companion.nilStream
import org.klogic.core.RecursiveStream.Companion.single
import org.klogic.unify.UnificationState

/**
 * Represents a logic object. It has only one direct implementor - [Var], user terms have to implement [CustomTerm].
 *
 * NOTE: sealed to prevent extending this interface by users.
 */
sealed interface Term<T : Term<T>> {
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
     * and a goal with the [nilStream] stream otherwise.
     *
     * @see [State.unifyWithConstraintsVerification] for details.
     */
    context(RelationalContext)
    infix fun unify(other: Term<T>): Goal = { st: State ->
        val stateAfter = st.unifyWithConstraintsVerification(this, other)
        unificationListeners.forEach { it.onUnification(this, other, st, stateAfter) }

        stateAfter?.let {
            single(it)
        } ?: NilStream(String.format("unification failed $this === $other"))
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
    context(RelationalContext)
    infix fun ineq(other: Term<T>): Goal = { st: State ->
        st.substitution.ineq(this, other).let {
            when (it) {
                ViolatedConstraintResult -> nilStream<State>().also {
                    disequalityListeners.forEach {
                        listener -> listener.onDisequality(this, other, st, stateAfter = null)
                    }
                }
                RedundantConstraintResult -> single(st).also {
                    disequalityListeners.forEach { listener ->
                        listener.onDisequality(this, other, st, st)
                    }
                }
                is SatisfiableConstraintResult -> {
                    val newConstraint = it.simplifiedConstraint
                    val newState = st.copy(constraints = st.constraints.add(newConstraint))

                    disequalityListeners.forEach { listener ->
                        listener.onDisequality(this, other, st, newState)
                    }

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

    /**
     * Considers this term as already reified.
     */
    @Suppress("UNCHECKED_CAST")
    fun asReified(): T = this as T

    context(RelationalContext)
    infix fun `===`(other: Term<T>): Goal = this unify other

    context(RelationalContext)
    infix fun `!==`(other: Term<T>): Goal = this ineq other

    fun isVar(): Boolean = this is Var<*>

    companion object {
        /**
         * Unifies [left] with [right] by invoking non-static method.
         */
        internal fun <T : Term<T>> unify(
            left: Term<T>,
            right: Term<T>,
            unificationState: UnificationState
        ): UnificationState? = left.unify(right, unificationState)
    }
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
        if (walkedOther is Var<T> && this == walkedOther) {
            return unificationState
        }

        if (walkedOther !is Var<T> && walkedOther.occurs(this)) {
            return null
        }

        val newAssociation = this to walkedOther
        unificationState.substitutionDifference[newAssociation.first] = newAssociation.second

        return unificationState.copy(substitution = unificationState.substitution + newAssociation)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T2 : Term<T2>> cast(): Var<T2> = this as Var<T2>

    override fun asReified(): T = error("Variable $this is not reified")

    override fun toString(): String = "_.$index"

    companion object {
        fun <T :Term<T>> Int.createTypedVar(): Var<T> = Var(this)
    }
}

/**
 * Represents a custom (i.e., defined by user) term.
 */
interface CustomTerm<T : CustomTerm<T>> : Term<T> {
    /**
     * Returns a sequence of subtrees (of any types) that should be used in unification process.
     */
    val subtreesToUnify: Array<*>
    /**
     * Returns a sequence of subtrees (of any types) that should be used in substituting variables.
     * Walked subtrees will be used for constructing an instance of this class.
     *
     * NOTE: for most of the classes equals to [subtreesToUnify], so by default has the same value.
     */
    val subtreesToWalk: Array<*>
        get() = subtreesToUnify

    override fun walk(substitution: Substitution): CustomTerm<T> {
        val walkedSubtrees = subtreesToWalk.map {
            (it as? Term<*>)?.walk(substitution) ?: it
        }

        return constructFromSubtrees(walkedSubtrees)
    }

    override fun <R : Term<R>> occurs(variable: Var<R>): Boolean = subtreesToUnify.any {
        (it as? Term<*>)?.occurs(variable) ?: false
    }

    override fun unifyImpl(walkedOther: Term<T>, unificationState: UnificationState): UnificationState? {
        if (walkedOther !is CustomTerm) {
            // This branch means that walkedOther is Var
            return walkedOther.unifyImpl(this, unificationState)
        }

        if (!(this isUnifiableWith walkedOther)) {
            return null
        }

        var currentUnificationState: UnificationState = unificationState

        subtreesToUnify.zip(walkedOther.subtreesToUnify).forEach { (curSubtree, otherSubtree) ->
            // Terms should be unified, non-logic types should be checked for equality
            val unificationResult = if (curSubtree is Term<*>) {
                // Cannot use non-static method here because of type inference error
                Term.unify(curSubtree, (otherSubtree as Term<*>).cast(), currentUnificationState)
            } else {
                currentUnificationState.takeIf { curSubtree == otherSubtree }
            }

            currentUnificationState = unificationResult ?: return null
        }

        return currentUnificationState
    }

    /**
     * Constructs an instance of this term using passed [subtrees].
     */
    fun constructFromSubtrees(subtrees: Iterable<*>): CustomTerm<T>

    /**
     * Checks whether this term can be unified with [other] term. For example, different branches of the same sealed term
     * often cannot be unified - for instance, a not empty list cannot be unified with an empty list.
     */
    infix fun isUnifiableWith(other: CustomTerm<T>): Boolean = javaClass == other.javaClass
}
