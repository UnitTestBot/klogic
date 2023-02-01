package org.klogic.core

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.klogic.core.Stream.Companion.nil
import org.klogic.utils.ones

class BindTest {
    @Test
    fun testBind() {
//        val simpleBind = ones().bind { ConsStream(2, ConsStream(3, nil())) }
        val simpleBind = ones().bind { sequenceOf(2, 3).toSequenceStream() }
        val firstTen = simpleBind.take(10)

        val expected = listOf(2, 2, 3, 2, 3, 2, 3, 2, 3, 2)

        assertEquals(expected, firstTen)
    }
}
