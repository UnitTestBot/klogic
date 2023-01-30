package org.klogic.streams

sealed class KanrenStream<T> {}

class NilStream<T> : KanrenStream<T>()

data class ConsStream<T>(val head: T, val tail: KanrenStream<T>) : KanrenStream<T>()

data class ThunksStream<T>(val elements: () -> KanrenStream<T>) : KanrenStream<T>()

typealias Stream<T> = KanrenStream<T>


//val EMPTY_STREAM: Stream = sequenceOf()

fun <T> empty(): Stream<T> = NilStream()

//fun single(logicObject: LogicObject): Stream = sequenceOf(logicObject)
fun <T> single(element: T): Stream<T> = ConsStream(element, empty())

fun <T> Stream<T>.take(n: Int): List<T> {
    return take(n, mutableListOf())
}

private fun <T> Stream<T>.take(n: Int, result: MutableList<T>): List<T> {
    if (n == 0) {
        return result
    }

    return when (this) {
        is NilStream<T> -> result
        is ConsStream<T> -> {
            val newResult = result + head
            tail.take(n - 1, newResult.toMutableList())
        }

        is ThunksStream<T> -> {
            elements().take(n, result)
        }
    }
}

infix fun <T> Stream<T>.mplus(other: Stream<T>): Stream<T> =
//    when (this) {
//        EMPTY_STREAM -> other
//        else -> {
//            val head = take(1).first()
//            println(head)
//            val tail = drop(1)
//
//            sequenceOf(head).plus(other.mplus(tail))
//        }
    when (this) {
        is NilStream<T> -> other
        is ConsStream<T> -> {
            ConsStream(head, ThunksStream { other.force() mplus tail })
        }

        is ThunksStream -> {
            ThunksStream { other.force() mplus this }
        }
    }

fun <T> Stream<T>.force(): Stream<T> =
    when (this) {
        is ThunksStream -> this.elements()
        else -> this
    }

fun <T, R> Stream<T>.bind(f: (T) -> Stream<R>): Stream<R> {
    return when (this) {
        is NilStream<T> -> empty()
        is ConsStream<T> -> {
            f(head) mplus ThunksStream { tail.force().bind(f) }
        }
        is ThunksStream<T> -> ThunksStream { elements().bind(f) }
    }
}
