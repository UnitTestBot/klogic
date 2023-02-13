package org.klogic.core

import org.klogic.core.RecursiveStream.Companion.nil
import org.klogic.core.RecursiveStream.Companion.single
import org.klogic.unify.UnificationState
import kotlin.reflect.KClass

/**
 * Represents a logic object.
 */
interface Term<T : Any> {
    /**
     * Checks whether [variable] occurs in [term].
     */
    fun occurs(variable: Var<out Any>): Boolean

    /**
     * Substitutes all occurrences of [term] to its value in [substitution].
     */
    fun walk(substitution: Substitution): Term<T>

    fun unify(other: Term<T>, unificationState: UnificationState): UnificationState? {
        val walkedThis = walk(unificationState.substitution)
        val walkedOther = other.walk(unificationState.substitution)

        return if (walkedOther is Var<*>) {
            walkedOther.unifyImpl(walkedThis, unificationState)
        } else {
            walkedThis.unifyImpl(walkedOther, unificationState)
        }
    }

    fun unifyImpl(walkedOther: Term<T>, unificationState: UnificationState): UnificationState?

    /**
     * Tries to unify this term to [other]. If succeeds, returns a [Goal] with [RecursiveStream] containing single [State] with a
     * corresponding [Substitution], and a goal with the [nil] stream otherwise.
     *
     * @see [State.unifyWithConstraintsVerification] for details.
     */
/*
    infix fun unify(other: Term): Goal = { st: State ->
        st.unifyWithConstraintsVerification(this, other)?.let {
            single(it)
        } ?: nil()
    }
*/

    /**
     * Returns a goal that contains one of the following:
     * - Copy of the passed state with an [InequalityConstraint] of this term and [other], if this constraint can be
     * satisfied somehow;
     * - Passed state (the same reference), if the mentioned above constraint can never be violated (i.e., it is redundant);
     * - No state at all, if this constraint is violated.
     *
     * @see [Substitution.ineq] for details.
     */
/*
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
*/

    /*infix fun `===`(other: Term): Goal = this unify other
    infix fun `!==`(other: Term): Goal = this ineq other*/
}

/**
 * Represents a symbolic term that can be equal to any other [Term].
 */
class Var<T : Any> @PublishedApi internal constructor(val index: Int, val variableType: KClass<T>) : Term<T> {
    override fun occurs(variable: Var<out Any>): Boolean = this == variable

    override fun walk(substitution: Substitution): Term<T> = substitution[this]?.let {
        it.walk(substitution)
    } ?: this

    override fun unifyImpl(walkedOther: Term<T>, unificationState: UnificationState): UnificationState? =
        if (walkedOther is Var<*>) {
            if (variableType != walkedOther.variableType) {
                TODO("Error message")
            }

            if (this == walkedOther) {
                unificationState
            } else {
                val newAssociation = this to (walkedOther as Var<T>)
                unificationState.substitutionDifference += newAssociation

                unificationState.copy(substitution = unificationState.substitution + newAssociation)
            }
        } else {
            if (walkedOther.occurs(this)) {
                null
            } else {
                val newAssociation = this to walkedOther
                unificationState.substitutionDifference += newAssociation

                unificationState.copy(substitution = unificationState.substitution + newAssociation)
            }
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Var<*>

        if (index != other.index) return false
        if (variableType != other.variableType) return false

        return true
    }
    override fun hashCode(): Int {
        var result = index
        result = 31 * result + variableType.hashCode()
        return result
    }

    override fun toString(): String = "_.$index:$variableType"

    companion object {
        inline fun <reified T : Any> Int.createTypedVar(): Var<T> = Var(this, T::class)
    }
}

interface CustomTerm<T : CustomTerm<T>> : Term<T> {
    // TODO this method seems redundant
    fun unify(variable: Var<T>, unificationState: UnificationState): UnificationState? =
        variable.unify(this, unificationState)
}

/*interface ITerm<T : Any> {
    fun occurs(variable: MyVar<out Any>): Boolean

    fun walk(substitution: Substitution): ITerm<T>

    fun unify(other: ITerm<T>, unificationState: UnificationState): Goal {
        val walkedThis = walk(unificationState.substitution)
        val walkedOther = other.walk(unificationState.substitution)

        return if (walkedOther is MyVar<*>) {
            walkedOther.unifyImpl(walkedThis, unificationState)
        } else {
            walkedThis.unifyImpl(walkedOther, unificationState)
        }
    }

    fun unifyImpl(walkedOther: ITerm<T>, unificationState: UnificationState): Goal
}

class MyVar<T : Any> @PublishedApi internal constructor(val index: Int, val variableType: KClass<T>) : ITerm<T> {
    override fun occurs(variable: MyVar<out Any>): Boolean = this == variable

    override fun walk(substitution: Substitution): MyVar<T> = TODO()

    override fun unifyImpl(walkedOther: ITerm<T>, unificationState: UnificationState): Goal = TODO()
}

interface CustomTerm<T : CustomTerm<T>> : ITerm<T> {
    fun unify(variable: MyVar<T>, unificationState: UnificationState): Goal =
        variable.unify(this, unificationState)
}


class MySymbol(val name: String) : CustomTerm<MySymbol> {
    override fun occurs(variable: MyVar<out Any>): Boolean = false

    override fun walk(substitution: Substitution): ITerm<MySymbol> = this

    override fun unifyImpl(walkedOther: ITerm<MySymbol>, unificationState: UnificationState): Goal {
        if (this == walkedOther) TODO() else TODO()
    }
}

class Pair(val first: MyVar<Any>, val second: ITerm<Any>) : CustomTerm<Pair> {
    override fun occurs(variable: MyVar<out Any>): Boolean = first.occurs(variable) || second.occurs(variable)

    override fun walk(substitution: Substitution): Pair = Pair(first.walk(substitution), second.walk(substitution))

    override fun unifyImpl(walkedOther: ITerm<Pair>, unificationState: UnificationState): Goal {
        val otherPair = walkedOther as Pair

        first.unify(otherPair.first, unificationState)
        second.unify(otherPair.second, unificationState)

        TODO()
    }
}

fun main() {
    val unificationState = UnificationState.empty

    val variableString1 = MyVar(1, String::class)

    val variableInt1 = MyVar(3, Int::class)
    val variableInt2 = MyVar(4, Int::class)
    val variableSymbol = MyVar(5, MySymbol::class)

    val symbol = MySymbol("a")
    val pair1 = Pair(variableString1, symbol)

    // Occurs
    variableString1.occurs(variableString1)
    variableString1.occurs(variableInt1)
    symbol.occurs(variableInt1)
    symbol.occurs(variableString1)

    // Unify
    variableInt1.unify(variableInt2, unificationState)
    // variableInt1.unify(variableString1, unificationState) - does not compile
    // variableInt1.unify(symbol, unificationState) - does not compile

    // symbol.unify(variableInt1, unificationState) - does not compile
    // symbol.unify(variableString1, unificationState) - does not compile
    // symbol.unify(pair1, unificationState) - does not compile
    symbol.unify(symbol, unificationState)
    symbol.unify(variableSymbol, unificationState)
    variableSymbol.unify(symbol, unificationState)
}*/
