@file:Suppress("NonAsciiCharacters", "ClassName", "TestFunctionName")

package org.klogic.utils.terms.logiclist

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.klogic.core.RelationalContext
import org.klogic.utils.terms.Nil.nilLogicList
import org.klogic.core.Var.Companion.createTypedVar
import org.klogic.core.run
import org.klogic.core.reified
import org.klogic.core.useWith
import org.klogic.utils.terms.Symbol
import org.klogic.utils.terms.Symbol.Companion.toSymbol
import org.klogic.utils.terms.plus
import org.klogic.utils.terms.reversᴼ
import org.klogic.utils.withEmptyContext

class ReversᴼTest {
    private val symbolA = "a".toSymbol()
    private val symbolB = "b".toSymbol()

    @Test
    fun testForwardReversᴼ() {
        withEmptyContext {
            val original = symbolA + (symbolB + nilLogicList())
            val goal = reversᴼ(original, (-1).createTypedVar())

            val results = run(2, (-1).createTypedVar(), goal)

            val expected = listOf(symbolB + (symbolA + nilLogicList())).reified()
            assertEquals(expected, results)
        }
    }

    @Test
    fun testBackwardReversᴼ() {
        withEmptyContext {
            val reversed = symbolB + (symbolA + nilLogicList())
            val goal = reversᴼ((-1).createTypedVar(), reversed)

            // Hangs when count > 1
            val results = run(1, (-1).createTypedVar<Symbol>(), goal)

            val expected = listOf(symbolA + (symbolB + nilLogicList())).reified()
            assertEquals(expected, results)
        }
    }
}
