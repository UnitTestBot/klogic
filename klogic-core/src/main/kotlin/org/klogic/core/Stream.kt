package org.klogic.core

/**
 * Stream type to represent an infinite sequence of results for a relational query.
 */
sealed class RecursiveStream<out T> {
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

    infix fun mplus(other: RecursiveStream<@UnsafeVariance T>): RecursiveStream<T> {
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

    infix fun <R> bind(f: (T) -> RecursiveStream<R>): RecursiveStream<R> =
        when (this) {
            NilStream -> NilStream
            is ConsStream<T> -> {
                f(head) mplus ThunkStream { tail() bind f }
            }
            is ThunkStream<T> -> ThunkStream { elements() bind f }
        }

    private fun force(): RecursiveStream<T> =
        when (this) {
            is ThunkStream -> elements()
            is ConsStream, NilStream -> this
        }

    private operator fun invoke(): RecursiveStream<T> = force()

    operator fun plus(head: @UnsafeVariance T): RecursiveStream<T> = ConsStream(head, this)

    companion object {
        fun <T> nil(): RecursiveStream<T> = NilStream

        fun <T> of(vararg elements: T): RecursiveStream<T> {
            if (elements.isEmpty()) {
                return NilStream
            }

            return elements.fold(nil()) { acc, e ->
                ConsStream(e, acc)
            }
        }

        fun <T> single(element: T): RecursiveStream<T> = ConsStream(element, NilStream)
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
