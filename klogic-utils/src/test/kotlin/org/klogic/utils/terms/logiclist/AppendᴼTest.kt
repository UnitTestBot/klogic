@file:Suppress("NonAsciiCharacters", "ClassName", "TestFunctionName")

package org.klogic.utils.terms.logiclist

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.klogic.core.RelationalContext
import org.klogic.core.Var.Companion.createTypedVar
import org.klogic.core.reified
import org.klogic.core.run
import org.klogic.core.useWith
import org.klogic.utils.terms.LogicList.Companion.logicListOf
import org.klogic.utils.terms.LogicList
import org.klogic.utils.terms.Nil.nilLogicList
import org.klogic.utils.terms.Symbol
import org.klogic.utils.terms.Symbol.Companion.toSymbol
import org.klogic.utils.terms.appendᴼ
import org.klogic.utils.terms.plus
import org.klogic.utils.terms.toLogicList
import org.klogic.utils.withEmptyContext

class AppendᴼTest {
    private val symbolA = "a".toSymbol()
    private val symbolB = "b".toSymbol()

    @Test
    fun testForwardAppendᴼ() {
        withEmptyContext {
            val xy = (-1).createTypedVar<LogicList<Symbol>>()

            val goal = appendᴼ(symbolA.toLogicList(), symbolB.toLogicList(), xy)

            // Take more than expected to test correct number of results
            val results = run(3, xy, goal)

            val expected = listOf(logicListOf(symbolA, symbolB)).reified()
            assertEquals(expected, results)
        }
    }

    @Test
    fun testBackwardAppendᴼ() {
        withEmptyContext {
            val x = (-1).createTypedVar<LogicList<Symbol>>()
            val y = (-2).createTypedVar<LogicList<Symbol>>()
            val xy = symbolA + (symbolB + nilLogicList())

            val goal = appendᴼ(x, y, xy)

            // Take more than expected to test correct number of results
            val results = run(4, logicListOf(x, y), goal)

            val expected = listOf(
                logicListOf(nilLogicList(), xy),
                logicListOf(symbolA + nilLogicList(), symbolB + nilLogicList()),
                logicListOf(xy, nilLogicList())
            ).reified()
            assertEquals(expected, results)
        }
    }
}
