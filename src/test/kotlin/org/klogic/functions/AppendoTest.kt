package org.klogic.functions

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.klogic.core.Var.Companion.createTypedVar
import org.klogic.core.reified
import org.klogic.core.run
import org.klogic.terms.Cons.Companion.logicListOf
import org.klogic.terms.LogicList
import org.klogic.terms.Nil.nilLogicList
import org.klogic.terms.Symbol
import org.klogic.terms.Symbol.Companion.toSymbol
import org.klogic.terms.plus
import org.klogic.terms.toLogicList

class AppendoTest {
    @Test
    fun testForwardAppendo() {
        val a = "a".toSymbol()
        val b = "b".toSymbol()

        val xy = (-1).createTypedVar<LogicList<Symbol>>()

        val goal = appendo(a.toLogicList(), b.toLogicList(), xy)

        // Take more than expected to test correct number of results
        val results = run(3, xy, goal)

        val expected = listOf(logicListOf(a, b)).reified()
        assertEquals(expected, results)
    }

    @Test
    fun testBackwardAppendo() {
        val a = "a".toSymbol()
        val b = "b".toSymbol()

        val x = (-1).createTypedVar<LogicList<Symbol>>()
        val y = (-2).createTypedVar<LogicList<Symbol>>()
        val xy = a + (b + nilLogicList())

        val goal = appendo(x, y, xy)

        // Take more than expected to test correct number of results
        val results = run(4, logicListOf(x, y), goal)

        val expected = listOf(
            logicListOf(nilLogicList(), xy),
            logicListOf(a + nilLogicList(), b + nilLogicList()),
            logicListOf(xy, nilLogicList())
        ).reified()
        assertEquals(expected, results)
    }
}
