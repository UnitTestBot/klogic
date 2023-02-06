package org.klogic.core

import org.klogic.core.Stream.Companion.nil
import org.klogic.core.Stream.Companion.single
import org.klogic.unify.toUnificationResult

sealed class Term {
    operator fun plus(other: Term): Cons = Cons(this, other)

    infix fun unify(other: Term): Goal = { st: State ->
        st.toUnificationResult().unify(this, other)?.let {
            single(it.newState)
        } ?: nil()
    }

    infix fun ineq(other: Term): Goal = { st: State -> single(st.ineq(this, other)) }

    infix fun `===`(other: Term): Goal = this unify other
    infix fun `!==`(other: Term): Goal = this ineq other
}

data class Symbol(val name: String) : Term() {
    override fun toString(): String = name

    companion object {
        fun String.toSymbol(): Symbol = Symbol(this)
    }
}

sealed class RecursiveList : Term()

object Nil : RecursiveList() {
    val empty: RecursiveList = this
    val nil: RecursiveList = this

    override fun toString(): String = "Nil"
}
data class Cons(val head: Term, val tail: Term) : RecursiveList() {
    override fun toString(): String = "($head ++ $tail)"
}
data class Var(val index: Int) : Term() {
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
