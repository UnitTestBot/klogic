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
    val HC = { it: Any -> System.identityHashCode(it) }
    /**
     * Concatenates two streams, the resulting stream contains elements of both input streams in an interleaved order.
     */
    infix fun mplus(other: RecursiveStream<@UnsafeVariance T>): RecursiveStream<T> {
        if (shouldStopCalculations()) {
            return this
        }
        mplusListeners.forEach { it.onMplus(this, other) }

//        return this mplusImpl other

        return when (this) {
            is NilStream -> {
                if (System.getenv("SILENT_MPLUS_BIND") == null)
//                    println("  mplus 1: Nil from ${this.msg}, ys = ${HC(other)}")
                    println("  mplus 1")
                other.force()
            }
            is ThunkStream -> {
                val rez = ThunkStream { other() mplus this }
                if (System.getenv("SILENT_MPLUS_BIND") == null)
//                    println("  mplus 2: xs = ${HC(this)} ys = ${HC(other)} ~~> Thunk _ = ${HC(rez)}")
                    println("  mplus 2")
                return rez
            }
            is ConsStream -> {
                when (this.tail) {
                    is NilStream -> {
                        val rez = ConsStream(this.head, other)
                        if (System.getenv("SILENT_MPLUS_BIND") == null)
//                            println("  mplus 3: xs = ${HC(this)} ys = ${HC(other)} ~~> ${HC(rez)}")
                            println("  mplus 3")
                        rez
                    }
                    else -> {
                        if (System.getenv("SILENT_MPLUS_BIND") == null)
//                            println("  mplus 4")
                            println("  mplus 4")
                        ConsStream(this.head, ThunkStream { other() mplus this.tail })
                    }
                }
            }
        }
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

//        println("FUCK")
//        return this bindImpl f
        return when (this) {
            is NilStream -> {
                if (System.getenv("SILENT_MPLUS_BIND") == null)
//                    println("  bind  1: Nil from ${this.msg}")
                    println("  bind  1")
                this
            }
            is ThunkStream -> {
                if (System.getenv("SILENT_MPLUS_BIND") == null)
//                    println("  bind  2: xs = ${HC(this)}")
                    println("  bind  2")
                ThunkStream { elements() bind f }
            }
            is ConsStream -> {
                when (this.tail) {
                    is NilStream -> {
                        if (System.getenv("SILENT_MPLUS_BIND") == null)
//                            println("  bind  3: xs = ${HC(this)}")
                            println("  bind  3")
                        f(head)
                    }
                    else -> {
                        if (System.getenv("SILENT_MPLUS_BIND") == null)
//                            println("  bind  4: xs = ${HC(this)}")
                            println("  bind  4")
                        val mappedHead = f(head)
                        mappedHead mplus ThunkStream { tail() bind f }
                    }
                }
            }
        }
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
internal class NilStream(val msg: String = "") : RecursiveStream<Nothing>() {
    override infix fun mplusImpl(other: RecursiveStream<Nothing>): RecursiveStream<Nothing> {
        TODO("IT should fail")
//        if (System.getenv("SILENT_MPLUS_BIND") == null)
//            println("mplus 1")
//        return other.force()
    }

    override infix fun <R> bindImpl(f: (Nothing) -> RecursiveStream<R>): RecursiveStream<R> {
        TODO("IT should fail")
//        if (System.getenv("SILENT_MPLUS_BIND") == null)
//            println("bind 1")
//        return this
    }

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
        TODO("IT should fail")
        // The special case for streams containing only one element from the Scheme implementation
//        if (tail is NilStream) {
//            if (System.getenv("SILENT_MPLUS_BIND") == null)
//                println("mplus 3")
//            return ConsStream(head, other)
//        }
//        if (System.getenv("SILENT_MPLUS_BIND") == null)
//            println("mplus 4")
//        return ConsStream(head, ThunkStream { other() mplus tail })
    }

    override infix fun <R> bindImpl(f: (T) -> RecursiveStream<R>): RecursiveStream<R> {
        TODO("IT should fail")
        // The special case for streams containing only one element from the Scheme implementation
//        if (tail is NilStream) {
//            if (System.getenv("SILENT_MPLUS_BIND") == null)
//                println("bind 3")
//            return f(head)
//        }
//        if (System.getenv("SILENT_MPLUS_BIND") == null)
//            println("bind 4")
//        val mappedHead = f(head)
//        return mappedHead mplus ThunkStream { tail() bind f }
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
    override infix fun mplusImpl(other: RecursiveStream<@UnsafeVariance T>): RecursiveStream<T> {
        TODO("IT should fail")
//        if (System.getenv("SILENT_MPLUS_BIND") == null)
//            println("mplus 2")
//        return ThunkStream { other() mplus this }
    }


    override infix fun <R> bindImpl(f: (T) -> RecursiveStream<R>): RecursiveStream<R> {
        TODO("IT should fail")
//        if (System.getenv("SILENT_MPLUS_BIND") == null)
//            println("bind 2")
//        return ThunkStream { elements() bind f }
    }

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
