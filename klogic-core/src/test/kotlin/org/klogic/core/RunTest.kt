package org.klogic.core

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.klogic.core.Var.Companion.createTypedVar
import org.klogic.utils.singleReifiedTerm
import org.klogic.utils.terms.Symbol
import org.klogic.utils.terms.Symbol.Companion.toSymbol
import org.klogic.utils.withEmptyContext

class RunTest {
    @Test
    fun testFailedRun() {
        withEmptyContext {
            val variable = 1.createTypedVar<Symbol>()
            val unreachableGoal = (variable `===` "a".toSymbol()) `&&&` (variable `===` "b".toSymbol())

            val run = run(2, variable, unreachableGoal)

            val expected = emptyList<ReifiedTerm<*>>()
            assertEquals(expected, run)
        }
    }

    @Test
    fun testRunOverloadingWithoutExplicitResultingVariable() {
        withEmptyContext {
            val goal = { symbol: Term<Symbol> -> symbol `===` "a".toSymbol() }

            val run = run(2, goal)

            assertEquals("a".toSymbol(), run.singleReifiedTerm)
        }
    }
}
