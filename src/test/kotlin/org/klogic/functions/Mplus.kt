package org.klogic.functions

import org.klogic.streams.ConsStream
import org.klogic.streams.Stream
import org.klogic.streams.ThunksStream
import org.klogic.streams.bind
import org.klogic.streams.empty
import org.klogic.streams.mplus
import org.klogic.streams.single
import org.klogic.streams.take

private fun <X> repeat(e: X): ThunksStream<X> {
 //   repeat()
    return ThunksStream { ConsStream(e, repeat(e)) }
}

private fun ones() = repeat(1)

fun testMplus(): Stream<Int> {
    val twos = repeat(2)
    val threes = repeat(3)

    return (ones() mplus twos) mplus threes
}

fun testBind(): Stream<Int> {
    return ones().bind { ConsStream(2, ConsStream(3, empty())) }
}

fun main() {
//    val mPlusResult = testMplus().take(10)
//    println(mPlusResult)

    val bindResult = testBind().take(10)
    println(bindResult)
}
