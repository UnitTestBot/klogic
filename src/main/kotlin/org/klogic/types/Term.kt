package org.klogic.types

interface LogicObject

sealed class Term : LogicObject

data class Symbol(val name: String) : Term() {}

sealed class List : Term() {}
object Nil : List() {}
data class Cons(val head: Term, val tail: List) : List() {}
data class Var(val index: Int) : Term() {}
