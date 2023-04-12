package org.klogic.core

/**
 * Stream type to represent an infinite sequence of results for a relational query.
 */
sealed interface RecursiveStream<out T> {
    /**
     * Returns the list of first [n] elements of this stream.
     */
    @Suppress("NAME_SHADOWING")
    infix fun take(n: Int): List<T> {
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

    /**
     * Concatenates two streams, the resulting stream contains elements of both input streams in an interleaved order.
     */
    infix fun mplus(other: RecursiveStream<@UnsafeVariance T>): RecursiveStream<T>

    /**
     * Maps function [f] over values of this stream, obtaining a stream of streams, and then flattens this stream.
     */
    infix fun <R> bind(f: (T) -> RecursiveStream<R>): RecursiveStream<R>

    /**
     * Forces calculating elements of this Stream.
     */
    fun force(): RecursiveStream<T>

    /**
     * Splits this stream to head and tail, if possible, and returns null otherwise.
     */
    fun msplit(): Pair<T, RecursiveStream<T>>?

    operator fun invoke(): RecursiveStream<T> = force()

    operator fun plus(head: @UnsafeVariance T): RecursiveStream<T> = ConsStream(head, this)

    companion object {
        fun <T> nilStream(): RecursiveStream<T> = NilStream
        fun <T> emptyStream(): RecursiveStream<T> = nilStream()

        fun <T> streamOf(vararg elements: T): RecursiveStream<T> {
            if (elements.isEmpty()) {
                return NilStream
            }

            return elements.fold(nilStream()) { acc, e ->
                ConsStream(e, acc)
            }
        }

        fun <T> single(element: T): RecursiveStream<T> = ConsStream(element, NilStream)
    }
}

/**
 * Represents an empty [RecursiveStream].
 */
private object NilStream : RecursiveStream<Nothing> {
    override infix fun mplus(other: RecursiveStream<Nothing>): RecursiveStream<Nothing> = other.force()

    override infix fun <R> bind(f: (Nothing) -> RecursiveStream<R>): RecursiveStream<R> = NilStream

    override fun force(): RecursiveStream<Nothing> = this

    override fun msplit(): Pair<Nothing, RecursiveStream<Nothing>>? = null
}

/**
 * Represents a [RecursiveStream], consisting of first element [head] and other elements in stream [tail].
 */
data class ConsStream<T>(val head: T, val tail: RecursiveStream<T>) : RecursiveStream<T> {
    override infix fun mplus(other: RecursiveStream<@UnsafeVariance T>): RecursiveStream<T> =
        ConsStream(head, ThunkStream { other() mplus tail })

    override infix fun <R> bind(f: (T) -> RecursiveStream<R>): RecursiveStream<R> =
        f(head) mplus ThunkStream { tail() bind f }

    override fun force(): RecursiveStream<T> = this

    override fun msplit(): Pair<T, RecursiveStream<T>> = head to tail
}

/**
 * Represents not already evaluated [RecursiveStream].
 */
@JvmInline
value class ThunkStream<T>(val elements: () -> RecursiveStream<T>) : RecursiveStream<T> {
    override infix fun mplus(other: RecursiveStream<@UnsafeVariance T>): RecursiveStream<T> =
        ThunkStream { other() mplus this }

    override infix fun <R> bind(f: (T) -> RecursiveStream<R>): RecursiveStream<R> = ThunkStream { elements() bind f }

    override fun force(): RecursiveStream<T> = elements()

    override fun msplit(): Pair<T, RecursiveStream<T>>? = force().msplit()
}
