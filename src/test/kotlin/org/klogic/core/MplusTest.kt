package org.klogic.core

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.klogic.utils.ones
import org.klogic.utils.repeat

class MplusTest {
    private fun twos(): CurStream<Int> = repeat(2)
    private fun threes(): CurStream<Int> = repeat(3)

    @Test
    fun testLeftAssociativity() {
        val mPlusResult = ones() mplus twos() mplus threes()
        val firstTen = mPlusResult.take(10)

        val expected = listOf(3, 2, 3, 1, 3, 2, 3, 1, 3, 2)

        assertEquals(expected, firstTen)
    }

    @Test
    fun testRightAssociativity() {
        val mPlusResult = ones() mplus (twos() mplus threes())
        val firstTen = mPlusResult.take(10)

        val expected = listOf(3, 1, 2, 1, 3, 1, 2, 1, 3, 1)

        assertEquals(expected, firstTen)
    }
}
