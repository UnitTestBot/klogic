package org.klogic.core

import org.klogic.core.Stream.Companion.nil

sealed interface Stream<T> {
    infix fun take(n: Int): List<T>

    @Suppress("SpellCheckingInspection")
    infix fun mplus(other: Stream<T>): Stream<T>

    infix fun <R> bind(f: (T) -> Stream<R>): Stream<R>

    fun force(): Stream<T>
    operator fun invoke(): Stream<T> = force()

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun <T, R : KanrenStream<T>> empty(): KanrenStream<T> = NilStream as KanrenStream<T>
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
sealed class KanrenStream<T> : Stream<T> {
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

    override fun mplus(other: Stream<T>): KanrenStream<T> {
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

    operator fun plus(head: T): KanrenStream<T> = ConsStream(head, this)
}

private object NilStream : KanrenStream<Nothing>()

data class ConsStream<T>(val head: T, val tail: KanrenStream<T>) : KanrenStream<T>()

data class ThunksStream<T>(val elements: () -> KanrenStream<T>) : KanrenStream<T>()
