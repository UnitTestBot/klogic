package org.klogic.functions

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.klogic.core.Var.Companion.createTypedVar
import org.klogic.core.reified
import org.klogic.core.run
import org.klogic.terms.Cons.Companion.recursiveListOf
import org.klogic.terms.Nil.nilLogicList
import org.klogic.terms.LogicList
import org.klogic.terms.Symbol
import org.klogic.terms.Symbol.Companion.toSymbol
import org.klogic.terms.plus

class AppendoTest {
    // TODO test forward
    @Test
    fun testAppendo() {
        val a = "a".toSymbol()
        val b = "b".toSymbol()

        val x = (-1).createTypedVar<LogicList<Symbol>>()
        val y = (-2).createTypedVar<LogicList<Symbol>>()
        val xy = a + (b + nilLogicList())

        val goal = appendo(x, y, xy)

        // Take more than expected to test correct number of results
        val results = run(4, recursiveListOf(x, y), goal)

        val expected = listOf(
            recursiveListOf(nilLogicList(), xy),
            recursiveListOf(a + nilLogicList(), b + nilLogicList()),
            recursiveListOf(xy, nilLogicList())
        ).reified()
        assertEquals(expected, results)
    }
}
