package org.klogic.terms

import org.klogic.core.CustomTerm
import org.klogic.core.Substitution
import org.klogic.core.Term
import org.klogic.core.Var
//import org.klogic.core.occurs
//import org.klogic.core.walk
import org.klogic.terms.Cons.Companion.recursiveListOf
//import org.klogic.terms.FANil.emptyFAList
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

    override fun occurs(variable: Var<out Any>): Boolean = false

    override fun walk(substitution: Substitution): CustomTerm<RecursiveList<Nothing>> = this

    override fun unifyCustomTermImpl(walkedOther: CustomTerm<RecursiveList<Nothing>>, unificationState: UnificationState): UnificationState? =
        if (this == walkedOther) unificationState else null

    override fun toString(): String = "Nil"
}

/**
 * Represents a [RecursiveList] consisting of element [head] at the beginning and [tail] as the rest.
 */
data class Cons<T : Any>(val head: Term<T>, val tail: Term<T>) : RecursiveList<T>() {
    override fun occurs(variable: Var<out Any>): Boolean = head.occurs(variable) || tail.occurs(variable)

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
        fun <T : Any> recursiveListOf(vararg terms: Term<T>): RecursiveList<T> {
            if (terms.isEmpty()) {
                return nilRecursiveList()
            }

            return Cons(terms.first(), recursiveListOf(*terms.drop(1).toTypedArray()).cast())
        }
    }
}

operator fun <T : Any> Term<T>.plus(list: RecursiveList<T>): RecursiveList<T> = Cons(this, list.cast())
operator fun <T : Any> RecursiveList<T>.plus(list: RecursiveList<T>): RecursiveList<T> {
    if (this is Nil) {
        return list
    }

    if (list is Nil) {
        return this
    }

    return (this as Cons).head + (tail + list)
}

//@JvmName("recursiveListPlusTerm")
//operator fun <T : Any> Term<RecursiveList<T>>.plus(term: Term<T>): RecursiveList<T> = this.projection() + (term + nilRecursiveList())
//@JvmName("termPlusRecursiveList")
//operator fun <T : Any> Term<T>.plus(term: Var<RecursiveList<T>>): RecursiveList<T> = recursiveListOf(this, term)

@JvmName("termPlusTerm")
operator fun <T : Any> Term<T>.plus(other: Term<T>): RecursiveList<T> = Cons(this, other)
operator fun <T : Any> Term<T>.plus(other: Term<RecursiveList<T>>): RecursiveList<T> = Cons(this, other.cast())

//fun <T : Any> Term<T>.toList(): Cons<T> = Cons(this, nilRecursiveList())
/*
sealed class MyList<T : Any>

object MyNil : MyList<Nothing>() {
    @Suppress("UNCHECKED_CAST")
    fun <T : Any> emptyMyList(): MyList<T> = this as MyList<T>
}
class MyCons<T : Any> : MyList<T>()

sealed class FAList<T : Any, R : Any> {
    abstract fun occurs(variable: Var<out Any>): Boolean
    abstract fun walk(substitution: Substitution): FAList<T, R>
    abstract fun unify(walkedOther: FAList<T, ListLogic<T>>, unificationState: UnificationState): UnificationState?
}

object FANil : FAList<Nothing, Nothing>() {
    @Suppress("UNCHECKED_CAST")
    fun <T : Any, R : Any> emptyFAList(): FAList<T, R> = this as FAList<T, R>

    override fun occurs(variable: Var<out Any>): Boolean = false

    override fun walk(substitution: Substitution): FAList<Nothing, Nothing> = this

    override fun unify(
        walkedOther: FAList<Nothing, ListLogic<Nothing>>,
        unificationState: UnificationState
    ): UnificationState? = if (this == (walkedOther as? FAList<*, *>)) unificationState else null
}

class FAListCons<T : Any, R : Any>(val head: T, val tail: R) : FAList<T, R>() {
    override fun occurs(variable: Var<out Any>): Boolean = head.occurs(variable) || tail.occurs(variable)

    override fun walk(substitution: Substitution): FAList<T, R> = FAListCons(head.walk(substitution), tail.walk(substitution))

    override fun unify(walkedOther: FAList<T, ListLogic<T>>, unificationState: UnificationState): UnificationState? {
        if ((walkedOther as FAList<*, *>) is FANil) {
            return null
        }

        walkedOther as FAListCons<T, ListLogic<T>>

        return head.unify(walkedOther.head, unificationState)?.let {
            tail.unify(walkedOther.tail, it)
        }
    }
}

class List2<T : Any>(val list: FAList<T, List2<T>>)

class ListLogic<T : Any>(private val list: FAList<T, ListLogic<T>>) : CustomTerm<ListLogic<T>> {
    override fun occurs(variable: Var<out Any>): Boolean = list.occurs(variable)

    override fun walk(substitution: Substitution): CustomTerm<ListLogic<T>> = ListLogic(list.walk(substitution))

    override fun unifyCustomTermImpl(
        walkedOther: CustomTerm<ListLogic<T>>,
        unificationState: UnificationState
    ): UnificationState? = list.unify(walkedOther.projection().list, unificationState)
}*/
