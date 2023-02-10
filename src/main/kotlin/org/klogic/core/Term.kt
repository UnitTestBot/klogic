package org.klogic.core

import org.klogic.core.RecursiveStream.Companion.nil
import org.klogic.core.RecursiveStream.Companion.single

/**
 * Represents a logic object.
 */
sealed interface Term {
    operator fun plus(other: Term): Cons = Cons(this, other)

    /**
     * Tries to unify this term to [other]. If succeeds, returns a [Goal] with [RecursiveStream] containing single [State] with a
     * corresponding [Substitution], and a goal with the [nil] stream otherwise.
     *
     * @see [State.unifyWithConstraintsVerification] for details.
     */
    infix fun unify(other: Term): Goal = { st: State ->
        st.unifyWithConstraintsVerification(this, other)?.let {
            single(it)
        } ?: nil()
    }

    /**
     * Returns a goal that contains one of the following:
     * - Copy of the passed state with an [InequalityConstraint] of this term and [other], if this constraint can be
     * satisfied somehow;
     * - Passed state (the same reference), if the mentioned above constraint can never be violated (i.e., it is redundant);
     * - No state at all, if this constraint is violated.
     *
     * @see [Substitution.ineq] for details.
     */
    infix fun ineq(other: Term): Goal = { st: State ->
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

    infix fun `===`(other: Term): Goal = this unify other
    infix fun `!==`(other: Term): Goal = this ineq other
}

/**
 * Represents a simple string constant.
 */
@JvmInline
value class Symbol(private val name: String) : Term {
    override fun toString(): String = name

    companion object {
        fun String.toSymbol(): Symbol = Symbol(this)
    }
}

/**
 * Represents classic recursive lists.
 */
sealed class RecursiveList : Term

/**
 * Represents an empty [RecursiveList].
 */
object Nil : RecursiveList() {
    val empty: RecursiveList = this
    val nil: RecursiveList = this

    override fun toString(): String = "Nil"
}

/**
 * Represents a [RecursiveList] consisting of element [head] at the beginning and [tail] as the rest.
 */
data class Cons(val head: Term, val tail: Term) : RecursiveList() {
    override fun toString(): String = "($head ++ $tail)"
}

/**
 * Represents a symbolic term that can be equal to any other [Term].
 */
@JvmInline
value class Var(val index: Int) : Term {
    override fun toString(): String = "_.$index"

    companion object {
        fun Int.toVar(): Var = Var(this)
    }
}

fun Any.toTerm(): Term = when (this) {
    is Int -> Var(this)
    is String -> Symbol(this)
    is Term -> this
    else -> error("Could not transform $this to term")
}
