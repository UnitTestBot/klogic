package org.klogic.terms

import org.klogic.core.CustomTerm
import org.klogic.core.Substitution
import org.klogic.core.Term
import org.klogic.core.Var
import org.klogic.terms.Nil.nilLogicList
import org.klogic.unify.UnificationState

/**
 * Represents logic list with elements of the specified logic type that can contain in the same time
 * elements of this type, or variables of this type, or another lists of this type.
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
data class Cons<T : Term<T>>(val head: Term<T>, val tail: Term<T>) : LogicList<T>() {
    override fun <R : Term<R>> occurs(variable: Var<R>): Boolean = head.occurs(variable) || tail.occurs(variable)

    override fun walk(substitution: Substitution): CustomTerm<LogicList<T>> = head.walk(substitution) + tail.walk(substitution)

    override fun unifyCustomTermImpl(
        walkedOther: CustomTerm<LogicList<T>>,
        unificationState: UnificationState
    ): UnificationState? {
        // This branch means we need to unify the whole current list with only one element - it can only happen
        // if the head equals to this element and the tail is Nil.
        if (walkedOther !is LogicList) {
            return head.unify(walkedOther.cast(), unificationState)?.let {
                tail.unify(nilLogicList().cast(), it)
            }
        }

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
        fun <T : Term<T>> recursiveListOf(vararg terms: Term<T>): LogicList<T> {
            if (terms.isEmpty()) {
                return nilLogicList()
            }

            // Hack(?) for testOnlyOneConstraintIsEnoughExample1
            if (terms.size == 2) {
                return terms.first() + terms.last()
            }

            return Cons(terms.first(), recursiveListOf(*terms.drop(1).toTypedArray()).cast())
        }
    }
}

@JvmName("termPlusTerm")
operator fun <T : Term<T>> Term<T>.plus(other: Term<T>): LogicList<T> = Cons(this, other)
@JvmName("termPlusList")
operator fun <T : Term<T>> Term<T>.plus(list: LogicList<T>): LogicList<T> = Cons(this, list.cast())
@JvmName("termPlusTermList")
operator fun <T : Term<T>> Term<T>.plus(list: Term<LogicList<T>>): LogicList<T> = Cons(this, list.cast())
@JvmName("termListPlusTermList")
operator fun <T : Term<T>> Term<LogicList<T>>.plus(list: Term<LogicList<T>>): LogicList<T> =
    when (this) {
        is Var<*> -> Cons(this.cast(), list.cast())
        else -> (this as LogicList<T>) + list
    }
@JvmName("listPlusList")
operator fun <T : Term<T>> LogicList<T>.plus(list: LogicList<T>): LogicList<T> {
    if (this is Nil) {
        return list
    }

    if (list is Nil) {
        return this
    }

    return (this as Cons).head + (tail + list)
}
