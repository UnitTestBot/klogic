@file:Suppress("FunctionName", "NonAsciiCharacters")

package org.klogic.utils.terms

import org.klogic.core.*
import org.klogic.utils.terms.Nil.nilLogicList

/**
 * Represents logic list with elements of the specified logic type that can contain in the same time
 * elements of this type, or variables of this type.
 */
sealed class LogicList<T : Term<T>> : CustomTerm<LogicList<T>> {
    abstract val size: Int

    abstract fun isEmpty(): Boolean
    abstract operator fun get(index: Int): Term<T>
    abstract fun toList(): List<Term<T>>

    companion object {
        /**
         * Constructs [LogicList] of the specified type from passed [terms].
         */
        fun <T : Term<T>> logicListOf(vararg terms: Term<T>): LogicList<T> {
            if (terms.isEmpty()) {
                return nilLogicList()
            }

            return Cons(terms.first(), logicListOf(*terms.drop(1).toTypedArray()))
        }
    }
}

private typealias ListTerm<T> = Term<LogicList<T>>

/**
 * Represents an empty [LogicList].
 */
object Nil : LogicList<Nothing>() {
    @Suppress("UNCHECKED_CAST")
    fun <T : Term<T>> emptyLogicList(): LogicList<T> = this as LogicList<T>
    fun <T : Term<T>> nilLogicList(): LogicList<T> = emptyLogicList()

    override val size: Int = 0

    override fun isEmpty(): Boolean = true

    override val subtreesToUnify: Array<Term<*>>
        get() = emptyArray()

    override fun constructFromSubtrees(subtrees: Iterable<*>): CustomTerm<LogicList<Nothing>> = this

    override fun get(index: Int): Nothing = throw IndexOutOfBoundsException("This list is empty")

    override fun toList(): List<Term<Nothing>> = emptyList()

    override fun toString(): String = "()"
}

/**
 * Represents a [LogicList] consisting of element [head] at the beginning of this list
 * and [tail] as the rest part of this list.
 */
data class Cons<T : Term<T>>(val head: Term<T>, val tail: Term<LogicList<T>>) : LogicList<T>() {
    override val subtreesToUnify: Array<Term<*>>
        get() = arrayOf(head, tail)

    override val size: Int
        get() = 1 + tail.asReified().size

    override fun isEmpty(): Boolean = false

    override fun constructFromSubtrees(subtrees: Iterable<*>): CustomTerm<LogicList<T>> {
        // We use by-hand iteration here to avoid losing performance.
        val iterator = subtrees.iterator()
        val head = iterator.next()
        val tail = iterator.next()

        require(!iterator.hasNext()) {
            "Expected only head and tail for constructing Cons but got more elements"
        }

        @Suppress("UNCHECKED_CAST")
        return Cons(head as Term<T>, tail as Term<LogicList<T>>)
    }

    override fun get(index: Int): Term<T> {
        require(index >= 0) {
            "Index $index is negative"
        }

        if (index == 0) {
            return head
        }

        if (index == 1 && tail is Var<*>) {
            return tail.cast()
        }

        return tail.asReified()[index - 1]
    }

    override fun toList(): List<Term<T>> {
        val begin = listOf(head)

        return begin + if (tail is LogicList<T>) {
            tail.toList()
        } else {
            // Tail is Var
            tail as Var<LogicList<T>>
            listOf(tail.cast())
        }
    }

    override fun toString(): String {
        fun ListTerm<T>.mapToString(): List<String> =
            when (this) {
                is Nil -> emptyList()
                is Var<*> -> listOf(this.toString())
                else -> listOf((this as Cons<T>).head.toString()) + tail.mapToString()
            }

        return mapToString().joinToString(", ", prefix = "(", postfix = ")")
    }
}

/**
 * Constructs [LogicList] using [this] term as a [Cons.head] and [list] as a [Cons.tail].
 */
operator fun <T : Term<T>> Term<T>.plus(list: ListTerm<T>): LogicList<T> = Cons(this, list)
infix fun <T : Term<T>> Term<T>.cons(list: ListTerm<T>): LogicList<T> = this + list

/**
 * Constructs [LogicList] consisting of only [this] element.
 */
fun <T : Term<T>> Term<T>.toLogicList(): LogicList<T> = Cons(this, nilLogicList())

/**
 * Constructs a [LogicList] from the elements of [this] [Collection].
 */
fun <T : Term<T>> Collection<Term<T>>.toLogicList(): LogicList<T> = LogicList.logicListOf(*this.toTypedArray())

context(RelationalContext)
fun <T : Term<T>> appendᴼ(x: ListTerm<T>, y: ListTerm<T>, xy: ListTerm<T>): Goal =
    ((x `===` nilLogicList()) `&&&` (y `===` xy)) `|||`
            freshTypedVars<T, LogicList<T>, LogicList<T>> { head, tail, rest ->
                (x `===` head + tail) `&&&` (xy `===` head + rest) `&&&` appendᴼ(tail, y, rest)
            }

context(RelationalContext)
fun <T : Term<T>> reversᴼ(x: ListTerm<T>, reversed: ListTerm<T>): Goal =
    ((x `===` nilLogicList()) `&&&` (reversed `===` nilLogicList())) `|||`
            freshTypedVars<T, LogicList<T>, LogicList<T>> { head, tail, rest ->
                (x `===` head + tail) `&&&` reversᴼ(tail, rest) `&&&` appendᴼ(
                    rest,
                    head + nilLogicList(),
                    reversed
                )
            }
