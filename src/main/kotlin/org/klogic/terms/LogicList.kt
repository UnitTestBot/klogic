package org.klogic.terms

import org.klogic.core.CustomTerm
import org.klogic.core.Substitution
import org.klogic.core.Term
import org.klogic.core.Var
import org.klogic.terms.Nil.nilLogicList
import org.klogic.unify.UnificationState

/**
 * Represents logic list with elements of the specified logic type that can contain in the same time
 * elements of this type, or variables of this type.
 */
sealed class LogicList<T : Term<T>> : CustomTerm<LogicList<T>>

/**
 * Represents an empty [LogicList].
 */
object Nil : LogicList<Nothing>() {
    @Suppress("UNCHECKED_CAST")
    fun <T : Term<T>> emptyLogicList(): LogicList<T> = this as LogicList<T>
    fun <T : Term<T>> nilLogicList(): LogicList<T> = emptyLogicList()

    override fun <R : Term<R>> occurs(variable: Var<R>): Boolean = false

    override fun walk(substitution: Substitution): CustomTerm<LogicList<Nothing>> = this

    override fun unifyCustomTermImpl(
        walkedOther: CustomTerm<LogicList<Nothing>>,
        unificationState: UnificationState
    ): UnificationState? = if (this == walkedOther) unificationState else null

    override fun toString(): String = "Nil"
}

/**
 * Represents a [LogicList] consisting of element [head] at the beginning of this list
 * and [tail] as the rest part of this list.
 */
data class Cons<T : Term<T>>(val head: Term<T>, val tail: Term<LogicList<T>>) : LogicList<T>() {
    override fun <R : Term<R>> occurs(variable: Var<R>): Boolean = head.occurs(variable) || tail.occurs(variable)

    override fun walk(substitution: Substitution): CustomTerm<LogicList<T>> =
        head.walk(substitution) + tail.walk(substitution)

    override fun unifyCustomTermImpl(
        walkedOther: CustomTerm<LogicList<T>>,
        unificationState: UnificationState
    ): UnificationState? {
        if (walkedOther is Nil) {
            return null
        }

        walkedOther as Cons<T>

        return head.unify(walkedOther.head, unificationState)?.let {
            tail.unify(walkedOther.tail, it)
        }
    }

    override fun toString(): String = "($head ++ $tail)"

    companion object {
        /**
         * Constructs [LogicList] of the specified type from passed [terms].
         */
        fun <T : Term<T>> logicListOf(vararg terms: Term<T>): LogicList<T> {
            if (terms.isEmpty()) {
                return nilLogicList()
            }

            return Cons(terms.first(), logicListOf(*terms.drop(1).toTypedArray()).cast())
        }
    }
}

operator fun <T : Term<T>> Term<T>.plus(list: Term<LogicList<T>>): LogicList<T> = Cons(this, list)
infix fun <T : Term<T>> Term<T>.cons(list: Term<LogicList<T>>): LogicList<T> = this + list

fun <T : Term<T>> Term<T>.toLogicList(): LogicList<T> = Cons(this, nilLogicList())
