package org.klogic.functions

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.klogic.core.Nil.nil
import org.klogic.core.Symbol.Companion.toSymbol
import org.klogic.core.Var.Companion.toVar
import org.klogic.core.run
import org.klogic.core.reified

class ReversoTest {
    private val symbolA = "a".toSymbol()
    private val symbolB = "b".toSymbol()

    @Test
    fun testForwardReverso() {
        val original = symbolA + (symbolB + nil)
        val goal = reverso(original, (-1).toVar())

        val results = run(2, (-1).toVar(), goal)

        val expected = listOf(symbolB + (symbolA + nil)).reified()
        assertEquals(expected, results)
    }

    @Test
    fun testBackwardReverso() {
        val reversed = symbolB + (symbolA + nil)
        val goal = reverso((-1).toVar(), reversed)

        // Hangs when count > 1
        val results = run(1, (-1).toVar(), goal)

        val expected = listOf(symbolA + (symbolB + nil)).reified()
        assertEquals(expected, results)
    }
}
