package org.klogic.core

/**
 * Stream type to represent an infinite sequence of results for a relational query.
 */
context(RelationalContext)
sealed class RecursiveStream<out T> {
    /**
     * Returns the list of first [n] elements of this stream.
     */
    infix fun take(n: Int): List<T> {
        @Suppress("NAME_SHADOWING")
        var n = n

        return buildList(n) {
            var curStream = this@RecursiveStream

            while (n > 0) {
                when (curStream) {
                    is NilStream -> return@buildList
                    is ThunkStream -> curStream = curStream.elements()
                    is ConsStream -> {
                        add(curStream.head)
                        curStream = curStream.tail
                        --n
                    }
                }
            }
        }
    }

    /**
     * Concatenates two streams, the resulting stream contains elements of both input streams in an interleaved order.
     */
    infix fun mplus(other: RecursiveStream<@UnsafeVariance T>): RecursiveStream<T> {
        if (shouldStopCalculations()) {
            return this
        }
        mplusListeners.forEach { it.onMplus(this, other) }

        return this mplusImpl other
    }

    /**
     * Maps function [f] over values of this stream, obtaining a stream of streams, and then flattens this stream.
     *
     * NOTE: do not use this method with mapping to a different type together with non-default [RelationalContext.shouldStopCalculations].
     */
    infix fun <R> bind(f: (T) -> RecursiveStream<R>): RecursiveStream<R> {
        if (shouldStopCalculations()) {
            // Here we assume that we work on streams consisting of the same type - see the note in the docs of this method.
            @Suppress("UNCHECKED_CAST")
            return this as RecursiveStream<R>
        }
        bindListeners.forEach { it.onBind(this, f) }

        return this bindImpl f
    }

    protected abstract infix fun mplusImpl(other: RecursiveStream<@UnsafeVariance T>): RecursiveStream<T>

    protected abstract infix fun <R> bindImpl(f: (T) -> RecursiveStream<R>): RecursiveStream<R>

    /**
     * Forces calculating elements of this Stream.
     */
    abstract fun force(): RecursiveStream<T>

    /**
     * Splits this stream to head and tail, if possible, and returns null otherwise.
     */
    abstract fun msplit(): Pair<T, RecursiveStream<T>>?

    operator fun invoke(): RecursiveStream<T> = force()

    operator fun plus(head: @UnsafeVariance T): RecursiveStream<T> = ConsStream(head, this)

    companion object {
        context(RelationalContext)
        fun <T> nilStream(): RecursiveStream<T> = nilStream

        context(RelationalContext)
        fun <T> emptyStream(): RecursiveStream<T> = nilStream()

        context(RelationalContext)
        fun <T> streamOf(vararg elements: T): RecursiveStream<T> {
            if (elements.isEmpty()) {
                return NilStream()
            }

            return elements.fold(nilStream()) { acc, e ->
                ConsStream(e, acc)
            }
        }

        context(RelationalContext)
        fun <T> single(element: T): RecursiveStream<T> = ConsStream(element, nilStream)
    }
}

/**
 * Represents an empty [RecursiveStream].
 *
 * NOTE: it can not be instantiated directly and should be used only via [RelationalContext].
 */
context(RelationalContext)
internal class NilStream internal constructor(): RecursiveStream<Nothing>() {
    override infix fun mplusImpl(other: RecursiveStream<Nothing>): RecursiveStream<Nothing> = other.force()

    override infix fun <R> bindImpl(f: (Nothing) -> RecursiveStream<R>): RecursiveStream<R> = this

    override fun force(): RecursiveStream<Nothing> = this

    override fun msplit(): Pair<Nothing, RecursiveStream<Nothing>>? = null

    override fun equals(other: Any?): Boolean = this === nilStream

    // Note that this hashCode does not depend on the current context
    // so nil streams from different contexts loaded using the same classloader have the same hash code.
    override fun hashCode(): Int = javaClass.hashCode()
}

/**
 * Represents a [RecursiveStream], consisting of first element [head] and other elements in stream [tail].
 */
context(RelationalContext)
class ConsStream<T>(val head: T, val tail: RecursiveStream<T>) : RecursiveStream<T>() {
    override infix fun mplusImpl(other: RecursiveStream<@UnsafeVariance T>): RecursiveStream<T> {
        // The special case for streams containing only one element from the Scheme implementation
        if (tail is NilStream) {
            return ConsStream(head, other)
        }

        return ConsStream(head, ThunkStream { other() mplus tail })
    }

    override infix fun <R> bindImpl(f: (T) -> RecursiveStream<R>): RecursiveStream<R> {
        val mappedHead = f(head)

        // The special case for streams containing only one element from the Scheme implementation
        if (tail is NilStream) {
            return mappedHead
        }

        return mappedHead mplus ThunkStream { tail() bind f }
    }

    override fun force(): RecursiveStream<T> = this

    override fun msplit(): Pair<T, RecursiveStream<T>> = head to tail

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ConsStream<*>

        if (head != other.head) return false
        return tail == other.tail
    }

    override fun hashCode(): Int {
        var result = head?.hashCode() ?: 0
        result = 31 * result + tail.hashCode()
        return result
    }
}

/**
 * Represents not already evaluated [RecursiveStream].
 */
context(RelationalContext)
class ThunkStream<T>(val elements: () -> RecursiveStream<T>) : RecursiveStream<T>() {
    override infix fun mplusImpl(other: RecursiveStream<@UnsafeVariance T>): RecursiveStream<T> =
        ThunkStream { other() mplus this }

    override infix fun <R> bindImpl(f: (T) -> RecursiveStream<R>): RecursiveStream<R> = ThunkStream { elements() bind f }

    override fun force(): RecursiveStream<T> = elements()

    override fun msplit(): Pair<T, RecursiveStream<T>>? = force().msplit()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ThunkStream<*>

        return elements == other.elements
    }

    override fun hashCode(): Int {
        return elements.hashCode()
    }
}
