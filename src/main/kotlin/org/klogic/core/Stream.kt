package org.klogic.core

/**
 * Stream type to represent an infinite sequence of results for a relational query.
 */
sealed interface Stream<out T> {
    /**
     * Returns the list of first [n] elements of this stream.
     */
    infix fun take(n: Int): List<T>

    /**
     * Concatenates two streams, the resulting stream contains elements of both input streams in an interleaved order.
     */
    infix fun mplus(other: Stream<@UnsafeVariance T>): Stream<T>

    /**
     * Maps function [f] over values of this stream, obtaining a stream of streams, and then flattens this stream.
     */
    infix fun <R> bind(f: (T) -> Stream<R>): Stream<R>
}

sealed class RecursiveStream<out T> : Stream<T> {
    @Suppress("NAME_SHADOWING")
    override infix fun take(n: Int): List<T> {
        val result = mutableListOf<T>()
        var n = n
        var curStream = this

        while (n > 0) {
            when (curStream) {
                NilStream -> return result
                is ThunkStream -> curStream = curStream.elements()
                is ConsStream -> {
                    result += curStream.head
                    curStream = curStream.tail
                    --n
                }
            }
        }

        return result
    }

    override fun mplus(other: Stream<@UnsafeVariance T>): RecursiveStream<T> {
        require(other is RecursiveStream)

        return when (this) {
            NilStream -> other()
            is ConsStream<T> -> {
                ConsStream(head, ThunkStream { other() mplus tail })
            }
            is ThunkStream -> {
                ThunkStream { other() mplus this }
            }
        }
    }

    override infix fun <R> bind(f: (T) -> Stream<R>): RecursiveStream<R> =
        when (this) {
            NilStream -> nil()
            is ConsStream<T> -> {
                (f(head) as RecursiveStream<R>) mplus ThunkStream { tail() bind f }
            }
            is ThunkStream<T> -> ThunkStream { elements() bind f }
        }

    private fun force(): RecursiveStream<T> =
        when (this) {
            is ThunkStream -> elements()
            is ConsStream -> this
            NilStream -> this
        }

    private operator fun invoke(): RecursiveStream<T> = force()

    operator fun plus(head: @UnsafeVariance T): RecursiveStream<T> = ConsStream(head, this)

    companion object {
        fun <T> empty(): RecursiveStream<T> = NilStream
        fun <T> nil(): RecursiveStream<T> = empty()

        fun <T> of(vararg elements: T): RecursiveStream<T> {
            if (elements.isEmpty()) {
                return nil()
            }

            return elements.fold(nil()) { acc, e ->
                ConsStream(e, acc)
            }
        }

        fun <T> single(element: T): RecursiveStream<T> = of(element)
    }
}

/**
 * Represents an empty [RecursiveStream].
 */
private object NilStream : RecursiveStream<Nothing>()

/**
 * Represents a [RecursiveStream], consisting of first element [head] and other elements in stream [tail].
 */
data class ConsStream<T>(val head: T, val tail: RecursiveStream<T>) : RecursiveStream<T>()

/**
 * Represents not already evaluated [RecursiveStream].
 */
data class ThunkStream<T>(val elements: () -> RecursiveStream<T>) : RecursiveStream<T>()

/**
 * Transforms [this] stream by performing [State.verify] to each element.
 */
fun RecursiveStream<State>.verify(): RecursiveStream<State> =
    when (this) {
        NilStream -> RecursiveStream.nil()
        is ConsStream -> {
            val verifiedHead = head.verify()
            val verifiedTail = tail.verify()

            verifiedHead?.let {
                ConsStream(it, verifiedTail)
            } ?: verifiedTail
        }
        is ThunkStream -> ThunkStream { elements().verify() }
    }

