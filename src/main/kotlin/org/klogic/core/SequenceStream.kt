package org.klogic.core

class SequenceStream<T>(val sequence: Sequence<T>) : Stream<T> {
    override fun take(n: Int): List<T> = sequence.take(n).toList()

    override fun mplus(other: Stream<T>): SequenceStream<T> {
        require(other is SequenceStream)

//        val head = sequence.firstOrNull() ?: return other
//        val tail = sequence.drop(1).toSequenceStream()

        return sequence.interleave(other.sequence).toSequenceStream()
//        return sequence.zip(other.sequence).map { sequenceOf(it.first, it.second) }.flatten().toSequenceStream()
//        return (sequenceOf(head) + (other mplus tail).sequence).toSequenceStream()
    }

    @Suppress("UNCHECKED_CAST")
    override fun <R> bind(f: (T) -> Stream<R>): SequenceStream<R> {
        f as (T) -> SequenceStream<R>

        return sequence.flatMap { f(it).sequence }.toSequenceStream()
//        val head = sequence.firstOrNull() ?: return empty()
//        val tail = sequence.drop(1).toSequenceStream()
//
//        return f(head) mplus (tail bind f)
    }

    override fun force(): SequenceStream<T> = this

    companion object {
        fun <T> empty(): SequenceStream<T> = emptySequence<T>().toSequenceStream()
        fun <T> nil(): SequenceStream<T> = empty()

        fun <T> of(vararg elements: T): SequenceStream<T> = sequenceOf(*elements).toSequenceStream()

        fun <T> single(element: T): SequenceStream<T> = of(element)

        fun <T> lazyStream(elements: () -> SequenceStream<T>): SequenceStream<T> = elements()
    }
}

fun <T> Sequence<T>.toSequenceStream() = SequenceStream(this)

fun <T> Sequence<T>.interleave(other: Sequence<T>): Sequence<T> = sequence {
    val lsIterator = this@interleave.iterator()
    val rsIterator = other.iterator()

    while (lsIterator.hasNext() && rsIterator.hasNext()) {
        yield(lsIterator.next())
        yield(rsIterator.next())
    }
    yieldAll(lsIterator)
    yieldAll(rsIterator)
}
