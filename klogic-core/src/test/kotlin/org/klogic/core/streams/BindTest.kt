package org.klogic.core.streams

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.klogic.core.ConsStream
import org.klogic.core.RecursiveStream.Companion.nilStream

class BindTest {
    @Test
    fun testBind() {
        val simpleBind = ones().bind { ConsStream(2, ConsStream(3, nilStream())) }
        val firstTen = simpleBind.take(10)

        val expected = listOf(2, 2, 3, 2, 3, 2, 3, 2, 3, 2)

        assertEquals(expected, firstTen)
    }
}
