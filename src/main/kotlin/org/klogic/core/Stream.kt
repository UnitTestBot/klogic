package org.klogic.core

import org.klogic.core.Stream.Companion.empty
import org.klogic.core.Stream.Companion.nil

interface StreamGenerator {
    fun <T> empty(): Stream<T>
    fun <T> nil(): Stream<T>
    fun <T> of(vararg elements: T): Stream<T>

    fun <T> single(element: T): Stream<T> = of(element)
}

sealed interface Stream<T> {
    infix fun take(n: Int): List<T>

    @Suppress("SpellCheckingInspection")
    infix fun mplus(other: Stream<T>): Stream<T>

    infix fun <R> bind(f: (T) -> Stream<R>): Stream<R>

    fun force(): Stream<T>
    operator fun invoke(): Stream<T> = force()

    companion object {
        @Suppress("UNCHECKED_CAST")
        inline fun <T, reified R : Stream<T>> empty(): Stream<T> =
            when (R::class) {
                KanrenStream::class -> KanrenStream.empty()
                SequenceStream::class -> TODO()
                else -> error("Unknown stream ${R::class}")
            }

        inline fun <T, reified R : Stream<T>> nil(): Stream<T> = empty<T, R>()

        inline fun <T, reified R : Stream<T>> of(vararg elements: T): Stream<T> {
            if (elements.isEmpty()) {
                return nil()
            }

            return when (R::class) {
                KanrenStream::class -> KanrenStream.of(*elements)
                SequenceStream::class -> TODO()
                else -> error("Unknown stream ${R::class}")
            }
        }

        fun <T, R : Stream<T>> single(element: T): Stream<T> = of(element)

        @Suppress("UNCHECKED_CAST")
        inline fun <T, reified R : Stream<T>> lazyStream(noinline f: () -> R): Stream<T> =
            when (R::class) {
                KanrenStream::class -> ThunksStream(f as () -> KanrenStream<T>)
                SequenceStream::class -> TODO()
                else -> error("Unknown stream ${R::class}")
            }
    }
}

class SequenceStream<T>(val sequence: Sequence<T>) : Stream<T> {
    override fun take(n: Int): List<T> {
        TODO("Not yet implemented")
    }

    override fun mplus(other: Stream<T>): Stream<T> {
        TODO("Not yet implemented")
    }

    override fun <R> bind(f: (T) -> Stream<R>): Stream<R> {
        /*sequence.firstOrNull {
            return empty()
        }*/
        TODO()
    }

    override fun force(): Stream<T> = this

    companion object : StreamGenerator {
        override fun <T> empty(): SequenceStream<T> {
            TODO("Not yet implemented")
        }

        override fun <T> nil(): SequenceStream<T> = empty()

        override fun <T> of(vararg elements: T): SequenceStream<T> {
            TODO("Not yet implemented")
        }
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
            NilStream -> empty()
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

    companion object : StreamGenerator {
        @Suppress("CAST_NEVER_SUCCEEDS")
        override fun <T> empty(): KanrenStream<T> = Nil as KanrenStream<T>

        override fun <T> nil(): KanrenStream<T> = empty()

        override fun <T> of(vararg elements: T): KanrenStream<T> = elements.fold(empty()) { acc, e ->
            ConsStream(e, acc)
        }
    }
}

// This object cannot be private because of usages in inline API but should not be used manually.
private object NilStream : KanrenStream<Nothing>()

data class ConsStream<T>(val head: T, val tail: KanrenStream<T>) : KanrenStream<T>()

data class ThunksStream<T>(val elements: () -> KanrenStream<T>) : KanrenStream<T>()
