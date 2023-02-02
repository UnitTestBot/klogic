package org.klogic.core

import kotlinx.collections.immutable.toPersistentList
import org.klogic.core.Stream.Companion.nil
import org.klogic.unify.walk

sealed interface Stream<out T> {
    infix fun take(n: Int): List<T>

    @Suppress("SpellCheckingInspection")
    infix fun mplus(other: Stream<@UnsafeVariance T>): Stream<T>

    infix fun <R> bind(f: (T) -> Stream<R>): Stream<R>

    fun force(): Stream<T>
    operator fun invoke(): Stream<T> = force()

    companion object {
        fun <T, R : KanrenStream<T>> empty(): KanrenStream<T> = NilStream
        fun <T, R : KanrenStream<T>> nil(): KanrenStream<T> = empty<T, R>()

        fun <T, R : KanrenStream<T>> of(vararg elements: T): KanrenStream<T> {
            if (elements.isEmpty()) {
                return nil()
            }

            return elements.fold(nil()) { acc, e ->
                ConsStream(e, acc)
            }
        }

        fun <T, R : KanrenStream<T>> single(element: T): KanrenStream<T> = of(element)
    }
}

@Suppress("SpellCheckingInspection")
sealed class KanrenStream<out T> : Stream<T> {
    @Suppress("NAME_SHADOWING")
    override infix fun take(n: Int): List<T> {
        val result = mutableListOf<T>()
        var n = n
        var curStream = this

        while (n > 0) {
            when (curStream) {
                NilStream -> return result
                is ThunksStream -> curStream = curStream.elements()
                is ConsStream -> {
                    result += curStream.head
                    curStream = curStream.tail
                    --n
                }
            }
        }

        return result
    }

    override fun mplus(other: Stream<@UnsafeVariance T>): KanrenStream<T> {
        require(other is KanrenStream)

        return when (this) {
            NilStream -> other()
            is ConsStream<T> -> {
                ConsStream(head, ThunksStream { other() mplus tail })
            }
            is ThunksStream -> {
                ThunksStream { other() mplus this }
            }
        }
    }

    override infix fun <R> bind(f: (T) -> Stream<R>): KanrenStream<R> =
        when (this) {
            NilStream -> nil()
            is ConsStream<T> -> {
                (f(head) as KanrenStream<R>) mplus ThunksStream { tail() bind f }
            }
            is ThunksStream<T> -> ThunksStream { elements() bind f }
        }

    override fun force(): KanrenStream<T> =
        when (this) {
            is ThunksStream -> elements()
            is ConsStream -> this
            NilStream -> this
        }

    override operator fun invoke(): KanrenStream<T> = force()

    operator fun plus(head: @UnsafeVariance T): KanrenStream<T> = ConsStream(head, this)
}

private object NilStream : KanrenStream<Nothing>()

data class ConsStream<T>(val head: T, val tail: KanrenStream<T>) : KanrenStream<T>()

data class ThunksStream<T>(val elements: () -> KanrenStream<T>) : KanrenStream<T>()

fun KanrenStream<State>.check(): KanrenStream<State> =
    when (this) {
        NilStream -> nil()
        is ConsStream -> {
            val checkedHead = head.check()
            val checkedTail = tail.check()

            checkedHead?.let {
                ConsStream(it, checkedTail)
            } ?: checkedTail
        }
        is ThunksStream -> ThunksStream { elements().check() }
    }

fun State.check(): State? =
    inequalityConstraints.flatMap { inequalityConstraint ->
        val left = inequalityConstraint.left
        val right = inequalityConstraint.right

        unify(left, right)?.let { inequalityState ->
            val newSubstitution = inequalityState.substitution

            val delta = newSubstitution - substitution
            if (delta.isEmpty()) {
                return@check null
            }

            delta.entries.map {
                InequalityConstraint(it.key, it.value)
            }
        } ?: emptyList()
    }.toPersistentList().let {
        copy(inequalityConstraints = it)
    }
