package org.klogic.terms

import org.klogic.core.CustomTerm
import org.klogic.core.Substitution
import org.klogic.core.Term
import org.klogic.core.Var
import org.klogic.unify.UnificationState

/**
 * Represents classic recursive lists.
 */
sealed class RecursiveList

/**
 * Represents an empty [RecursiveList].
 */
object Nil : RecursiveList(), CustomTerm<Nil> {
    val emptyList: RecursiveList = this
    val nilList: RecursiveList = this

    override fun occurs(variable: Var<out Any>): Boolean = false

    override fun walk(substitution: Substitution): CustomTerm<Nil> = this

    override fun unifyImpl(walkedOther: Term<Nil>, unificationState: UnificationState): UnificationState? =
        if (this == walkedOther) unificationState else null

    override fun toString(): String = "Nil"
}

/**
 * Represents a [RecursiveList] consisting of element [head] at the beginning and [tail] as the rest.
 */
data class Cons(val head: Term<Any>, val tail: Term<Any>) : RecursiveList(), CustomTerm<Cons> {
    override fun occurs(variable: Var<out Any>): Boolean = head.occurs(variable) || tail.occurs(variable)

    override fun walk(substitution: Substitution): CustomTerm<Cons> =
        Cons(head.walk(substitution), tail.walk(substitution))

    override fun unifyImpl(walkedOther: Term<Cons>, unificationState: UnificationState): UnificationState? {
        walkedOther as Cons

        return head.unify(walkedOther.head, unificationState)?.let {
            tail.unify(walkedOther.tail, it)
        }
    }

    override fun toString(): String = "($head ++ $tail)"
}
