package org.klogic.types

interface LogicObject

sealed class Term : LogicObject {
    operator fun plus(other: Term): Cons = Cons(this, other)
}

data class Symbol(val name: String) : Term() {
    override fun toString(): String = name
}

sealed class List : Term() {}
object Nil : List() {
    override fun toString(): String = "Nil"
}
data class Cons(val head: Term, val tail: Term) : List() {
    override fun toString(): String = "($head ++ $tail)"
}
data class Var(val index: Int) : Term() {
    override fun toString(): String = "_.$index"
}

fun String.toSymbol(): Symbol = Symbol(this)
