package org.klogic.core

import org.klogic.core.RecursiveStream.Companion.nil
import org.klogic.core.RecursiveStream.Companion.single
import org.klogic.unify.UnificationState
import kotlin.reflect.KClass

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
