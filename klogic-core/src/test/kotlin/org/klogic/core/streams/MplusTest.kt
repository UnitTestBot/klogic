package org.klogic.core.streams

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.klogic.core.RelationalContext
import org.klogic.core.ThunkStream
import org.klogic.utils.withEmptyContext

class MplusTest {
    context(RelationalContext)
    private fun twos(): ThunkStream<Int> = repeat(2)

    context(RelationalContext)
    private fun threes(): ThunkStream<Int> = repeat(3)

    @Test
    fun testLeftAssociativity() {
        withEmptyContext {
            val mPlusResult = ones() mplus twos() mplus threes()
            val firstTen = mPlusResult.take(10)

            val expected = listOf(3, 2, 3, 1, 3, 2, 3, 1, 3, 2)

            assertEquals(expected, firstTen)
        }
    }

    @Test
    fun testRightAssociativity() {
        withEmptyContext {
            val mPlusResult = ones() mplus (twos() mplus threes())
            val firstTen = mPlusResult.take(10)

            val expected = listOf(3, 1, 2, 1, 3, 1, 2, 1, 3, 1)

            assertEquals(expected, firstTen)
        }
    }
}
