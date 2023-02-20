@file:Suppress("FunctionName", "NonAsciiCharacters")

package org.klogic.utils.terms

import org.klogic.core.`&&&`
import org.klogic.core.CustomTerm
import org.klogic.core.Goal
import org.klogic.core.Term
import org.klogic.core.Var
import org.klogic.core.freshTypedVars
import org.klogic.core.`|||`
import org.klogic.utils.terms.Nil.nilLogicList

/**
 * Represents logic list with elements of the specified logic type that can contain in the same time
 * elements of this type, or variables of this type.
 */
sealed class LogicList<T : Term<T>> : CustomTerm<LogicList<T>> {
    fun isEmpty(): Boolean = this is Nil
}

/**
 * Represents an empty [LogicList].
 */
object Nil : LogicList<Nothing>() {
    @Suppress("UNCHECKED_CAST")
    fun <T : Term<T>> emptyLogicList(): LogicList<T> = this as LogicList<T>
    fun <T : Term<T>> nilLogicList(): LogicList<T> = emptyLogicList()

    override val subtreesToUnify: Sequence<Term<*>>
        get() = emptySequence()

    override fun constructFromSubtrees(subtrees: List<*>): CustomTerm<LogicList<Nothing>> = this

    // Nil cannot be unified with a not empty list
    override fun isUnifiableWith(other: CustomTerm<LogicList<Nothing>>): Boolean = other is Nil

    override fun toString(): String = "()"
}

/**
 * Represents a [LogicList] consisting of element [head] at the beginning of this list
 * and [tail] as the rest part of this list.
 */
data class Cons<T : Term<T>>(val head: Term<T>, val tail: Term<LogicList<T>>) : LogicList<T>() {
    override val subtreesToUnify: Sequence<Term<*>>
        get() = sequenceOf(head, tail)

    override fun constructFromSubtrees(subtrees: List<*>): CustomTerm<LogicList<T>> {
        require(subtrees.size == 2) {
            "Expected 2 arguments for constructing Cons but ${subtrees.size} are presented - $subtrees"
        }

        @Suppress("UNCHECKED_CAST")
        return subtrees.first() as Term<T> + (subtrees.last() as Term<LogicList<T>>)
    }

    // A not empty list cannot be unified with an empty list
    override fun isUnifiableWith(other: CustomTerm<LogicList<T>>): Boolean = other is Cons

    override fun toString(): String {
        fun Term<LogicList<T>>.mapToString(): List<String> =
            when (this) {
                is Nil -> emptyList()
                is Var<*> -> listOf(this.toString())
                else -> listOf((this as Cons<T>).head.toString()) + tail.mapToString()
            }

        return mapToString().joinToString(", ", prefix = "(", postfix = ")")
    }

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

/**
 * Constructs [LogicList] using [this] term as a [Cons.head] and [list] as a [Cons.tail].
 */
operator fun <T : Term<T>> Term<T>.plus(list: Term<LogicList<T>>): LogicList<T> = Cons(this, list)
infix fun <T : Term<T>> Term<T>.cons(list: Term<LogicList<T>>): LogicList<T> = this + list

/**
 * Constructs [LogicList] consisting of only [this] element.
 */
fun <T : Term<T>> Term<T>.toLogicList(): LogicList<T> = Cons(this, nilLogicList())

fun <T : Term<T>> appendᴼ(x: Term<LogicList<T>>, y: Term<LogicList<T>>, xy: Term<LogicList<T>>): Goal =
    ((x `===` nilLogicList())) `&&&` (y `===` xy) `|||`
            freshTypedVars<T, LogicList<T>, LogicList<T>> { head, tail, rest ->
                (x `===` head + tail) `&&&` (xy `===` head + rest) `&&&` appendᴼ(tail, y, rest)
            }

fun <T : Term<T>> reversᴼ(x: Term<LogicList<T>>, reversed: Term<LogicList<T>>): Goal =
    ((x `===` nilLogicList()) `&&&` (reversed `===` nilLogicList())) `|||`
            freshTypedVars<T, LogicList<T>, LogicList<T>> { head, tail, rest ->
                (x `===` head + tail) `&&&` reversᴼ(tail, rest) `&&&` appendᴼ(
                    rest,
                    head + nilLogicList(),
                    reversed
                )
            }
