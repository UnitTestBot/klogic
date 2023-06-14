package org.klogic.utils.terms

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.klogic.core.RelationalContext
import org.klogic.core.Var.Companion.createTypedVar
import org.klogic.core.run
import org.klogic.core.useWith
import org.klogic.utils.singleReifiedTerm
import org.klogic.utils.terms.Symbol.Companion.toSymbol
import org.klogic.utils.withEmptyContext

class LogicPairTest {
    @Test
    fun testConstructingPair() {
        withEmptyContext {
            val first = "a".toSymbol()
            val second = 42.toPeanoLogicNumber()
            val pair = (-1).createTypedVar<LogicPair<Symbol, PeanoLogicNumber>>()

            val goal = pair `===` (first logicTo second)

            val run = run(2, pair, goal)

            val expectedTerm = first logicTo second

            assertEquals(expectedTerm, run.singleReifiedTerm)
        }
    }

    @Test
    fun testFirstComponent() {
        withEmptyContext {
            val first = (-1).createTypedVar<Symbol>()
            val second = 42.toPeanoLogicNumber()
            val pair = "a".toSymbol() logicTo second

            val goal = pair `===` (first logicTo second)

            val run = run(2, first, goal)

            val expectedTerm = "a".toSymbol()

            assertEquals(expectedTerm, run.singleReifiedTerm)
        }
    }

    @Test
    fun testSecondComponent() {
        withEmptyContext {
            val first = "a".toSymbol()
            val second = (-1).createTypedVar<PeanoLogicNumber>()
            val pair = first logicTo 42.toPeanoLogicNumber()

            val goal = pair `===` (first logicTo second)

            val run = run(2, second, goal)

            val expectedTerm = 42.toPeanoLogicNumber()

            assertEquals(expectedTerm, run.singleReifiedTerm)
        }
    }
}
