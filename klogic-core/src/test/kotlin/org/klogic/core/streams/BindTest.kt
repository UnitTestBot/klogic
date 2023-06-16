package org.klogic.core.streams

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.klogic.core.ConsStream
import org.klogic.utils.withEmptyContext

class BindTest {
    @Test
    fun testBind() {
        withEmptyContext {
            val simpleBind = ones().bind { ConsStream(2, ConsStream(3, nilStream())) }
            val firstTen = simpleBind.take(10).toList()

            val expected = listOf(2, 2, 3, 2, 3, 2, 3, 2, 3, 2)

            assertEquals(expected, firstTen)
        }
    }
}
