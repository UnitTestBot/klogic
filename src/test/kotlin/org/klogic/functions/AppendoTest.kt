package org.klogic.functions

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.klogic.core.Nil.nil
import org.klogic.core.Symbol.Companion.toSymbol
import org.klogic.core.Var.Companion.toVar
import org.klogic.core.run

class AppendoTest {
    // TODO test forward
    @Test
    fun testAppendo() {
        val a = "a".toSymbol()
        val b = "b".toSymbol()
        val xy = a + (b + nil)
        val goal = appendo((-1).toVar(), (-2).toVar(), xy)

        // Take more than expected to test correct number of results
        val results = run(4, (-1).toVar() + (-2).toVar(), goal)

        val expected = listOf(
            nil + xy,
            (a + nil) + (b + nil),
            xy + nil
        )
        assertEquals(expected, results)
    }
}
