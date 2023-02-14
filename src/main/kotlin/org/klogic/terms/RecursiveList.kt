package org.klogic.terms

import org.klogic.core.CustomTerm
import org.klogic.core.Substitution
import org.klogic.core.Term
import org.klogic.core.Var
import org.klogic.terms.Cons.Companion.recursiveListOf
import org.klogic.terms.Nil.nilRecursiveList
import org.klogic.unify.UnificationState

/**
 * Represents classic recursive lists.
 */
sealed class RecursiveList<T : Term> : CustomTerm

/**
 * Represents an empty [RecursiveList].
 */
object Nil : RecursiveList<Nothing>() {
    @Suppress("UNCHECKED_CAST")
    fun <T : Term> emptyRecursiveList(): RecursiveList<T> = this as RecursiveList<T>
    fun <T : Term> nilRecursiveList(): RecursiveList<T> = emptyRecursiveList()

    override fun occurs(variable: Var<out Term>): Boolean = false

    override fun walk(substitution: Substitution): RecursiveList<out Term> = this

    override fun unifyCustomTermImpl(walkedOther: CustomTerm, unificationState: UnificationState): UnificationState? =
        if (this == walkedOther) unificationState else null

    override fun toString(): String = "Nil"
}

/**
 * Represents a [RecursiveList] consisting of element [head] at the beginning and [tail] as the rest.
 */
data class Cons<T : Term>(val head: T, val tail: RecursiveList<T>) : RecursiveList<T>() {
    override fun occurs(variable: Var<out Term>): Boolean = head.occurs(variable) || tail.occurs(variable)

    @Suppress("UNCHECKED_CAST")
    override fun walk(substitution: Substitution): RecursiveList<T> =
        Cons(head.walk(substitution) as T, tail.walk(substitution) as RecursiveList<T>)

    override fun unifyCustomTermImpl(walkedOther: CustomTerm, unificationState: UnificationState): UnificationState? {
        if (walkedOther is Nil) {
            return null
        }

        @Suppress("UNCHECKED_CAST")
        walkedOther as Cons<T>

        return head.unify(walkedOther.head, unificationState)?.let {
            tail.unify(walkedOther.tail, it)
        }
    }

    override fun toString(): String = "($head ++ $tail)"

    companion object {
        inline fun <reified T : Term> recursiveListOf(vararg terms: T): RecursiveList<T> {
            if (terms.isEmpty()) {
                return nilRecursiveList()
            }

            // Cannot use recursion here because this method is inlined
            val last = terms.last()
            var result = Cons(last, nilRecursiveList())
            var remainingTerms = terms.toList().subList(0, terms.lastIndex)
            (0..remainingTerms.lastIndex).forEach { _ ->
                result = Cons(remainingTerms.last(), result)
                remainingTerms = remainingTerms.subList(0, remainingTerms.lastIndex)
            }

            return result
        }
    }
}

operator fun <T : Term> T.plus(list: RecursiveList<T>): RecursiveList<T> = Cons(this, list)
operator fun <T : Term> RecursiveList<T>.plus(list: RecursiveList<T>): RecursiveList<T> {
    if (this is Nil) {
        return list
    }

    if (list is Nil) {
        return this
    }

    return (this as Cons).head + (tail + list)
}

@Suppress("USELESS_CAST")
operator fun <T : Term> RecursiveList<T>.plus(term: T): RecursiveList<T> = this + ((term + nilRecursiveList()) as RecursiveList<T>)

inline operator fun <reified T : Term> T.plus(other: T): RecursiveList<T> = recursiveListOf(this, other)

fun <T : Term> T.toList(): Cons<T> = Cons(this, nilRecursiveList())
