package org.klogic.terms

import org.klogic.core.CustomTerm
import org.klogic.core.Substitution
import org.klogic.core.Term
import org.klogic.core.Var
import org.klogic.terms.Nil.nilRecursiveList
import org.klogic.unify.UnificationState

/**
 * Represents classic recursive lists.
 */
sealed class RecursiveList<T : Any> : CustomTerm<RecursiveList<T>>

/**
 * Represents an empty [RecursiveList].
 */
object Nil : RecursiveList<Nothing>() {
    @Suppress("UNCHECKED_CAST")
    fun <T : Any> emptyRecursiveList(): RecursiveList<T> = this as RecursiveList<T>
    fun <T : Any> nilRecursiveList(): RecursiveList<T> = emptyRecursiveList()

    override fun <R : Term<R>> occurs(variable: Var<R>): Boolean = false

    override fun walk(substitution: Substitution): CustomTerm<RecursiveList<Nothing>> = this

    override fun unifyCustomTermImpl(walkedOther: CustomTerm<RecursiveList<Nothing>>, unificationState: UnificationState): UnificationState? =
        if (this == walkedOther) unificationState else null

    override fun toString(): String = "Nil"
}

/**
 * Represents a [RecursiveList] consisting of element [head] at the beginning and [tail] as the rest.
 */
data class Cons<T : Term<T>>(val head: Term<T>, val tail: Term<T>) : RecursiveList<T>() {
    override fun <R : Term<R>> occurs(variable: Var<R>): Boolean = head.occurs(variable) || tail.occurs(variable)

    override fun walk(substitution: Substitution): CustomTerm<RecursiveList<T>> =
        Cons(head.walk(substitution), tail.walk(substitution))

    override fun unifyCustomTermImpl(walkedOther: CustomTerm<RecursiveList<T>>, unificationState: UnificationState): UnificationState? {
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
        fun <T : Term<T>> recursiveListOf(vararg terms: Term<T>): RecursiveList<T> {
            if (terms.isEmpty()) {
                return nilRecursiveList()
            }

            return Cons(terms.first(), recursiveListOf(*terms.drop(1).toTypedArray()).cast())
        }
    }
}

operator fun <T : Term<T>> Term<T>.plus(list: RecursiveList<T>): RecursiveList<T> = Cons(this, list.cast())
operator fun <T : Term<T>> Term<T>.plus(list: Term<RecursiveList<T>>): RecursiveList<T> = Cons(this, list.cast())
operator fun <T : Term<T>> RecursiveList<T>.plus(list: RecursiveList<T>): RecursiveList<T> {
    if (this is Nil) {
        return list
    }

    if (list is Nil) {
        return this
    }

    return (this as Cons).head + (tail + list)
}

@JvmName("termPlusTerm")
operator fun <T : Term<T>> Term<T>.plus(other: Term<T>): RecursiveList<T> = Cons(this, other)
