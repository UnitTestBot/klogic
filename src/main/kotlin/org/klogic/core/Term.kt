package org.klogic.core

import org.klogic.core.Stream.Companion.nil
import org.klogic.core.Stream.Companion.single

sealed class Term {
    operator fun plus(other: Term): Cons = Cons(this, other)

    infix fun unify(other: Term): Goal = { st: State ->
        st.unify(this, other)?.let {
            /*single(it)*/CurStream.single(it)
        } ?: /*nil()*/CurStream.nil()
    }

    infix fun `===`(other: Term): Goal = this unify other
}

data class Symbol(val name: String) : Term() {
    override fun toString(): String = name

    companion object {
        fun String.toSymbol(): Symbol = Symbol(this)
    }
}

sealed class RecursiveList : Term() {
    companion object {
        val empty: RecursiveList = Nil
        val nil: RecursiveList = empty
    }
}
object Nil : RecursiveList() {
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
