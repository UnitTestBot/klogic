package org.klogic.core

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.klogic.core.Var.Companion.createTypedVar
import org.klogic.terms.Symbol
import org.klogic.terms.Symbol.Companion.toSymbol

class RunTest {
    @Test
    fun testFailedRun() {
        val variable = 1.createTypedVar<Symbol>()
        val unreachableGoal = (variable `===` "a".toSymbol()) `&&&` (variable `===` "b".toSymbol())

        val run = run(2, variable, unreachableGoal)

        val expected = emptyList<ReifiedTerm<*>>()
        assertEquals(expected, run)
    }
}
