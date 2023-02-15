package org.klogic.functions

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.klogic.terms.Nil.nilRecursiveList
import org.klogic.core.Var.Companion.createTypedVar
import org.klogic.core.run
import org.klogic.core.reified
import org.klogic.terms.Symbol
import org.klogic.terms.Symbol.Companion.toSymbol
import org.klogic.terms.plus

class ReversoTest {
    private val symbolA = "a".toSymbol()
    private val symbolB = "b".toSymbol()

    @Test
    fun testForwardReverso() {
        val original = symbolA + (symbolB + nilRecursiveList())
        val goal = reverso(original, (-1).createTypedVar())

        val results = run(2, (-1).createTypedVar(), goal)

        val expected = listOf(symbolB + (symbolA + nilRecursiveList())).reified()
        assertEquals(expected, results)
    }

    @Test
    fun testBackwardReverso() {
        val reversed = symbolB + (symbolA + nilRecursiveList())
        val goal = reverso((-1).createTypedVar(), reversed)

        // Hangs when count > 1
        val results = run(1, (-1).createTypedVar<Symbol>(), goal)

        val expected = listOf(symbolA + (symbolB + nilRecursiveList())).reified()
        assertEquals(expected, results)
    }
}
