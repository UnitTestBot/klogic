package org.klogic.functions

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.klogic.core.Nil.nil
import org.klogic.core.Symbol.Companion.toSymbol
import org.klogic.core.Var.Companion.toVar
import org.klogic.core.run

@Suppress("SpellCheckingInspection")
class ReversoTest {
    private val a = "a".toSymbol()
    private val b = "b".toSymbol()

    @Test
    fun testForwardReverso() {
        val original = a + (b + nil)
        val goal = reverso(original, (-1).toVar())

        val results = run(2, (-1).toVar(), goal)

        val expected = listOf(b + (a + nil))
        assertEquals(expected, results)
    }

    @Test
    fun testBackwardReverso() {
        val reversed = b + (a + nil)
        val goal = reverso((-1).toVar(), reversed)

        // Hangs when count > 1
        val results = run(1, (-1).toVar(), goal)

        val expected = listOf(a + (b + nil))
        assertEquals(expected, results)
    }
}
