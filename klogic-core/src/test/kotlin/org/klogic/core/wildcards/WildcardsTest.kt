package org.klogic.core.wildcards

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.klogic.core.Goal
import org.klogic.core.InequalityConstraint
import org.klogic.core.Term
import org.klogic.core.Var
import org.klogic.core.and
import org.klogic.utils.terms.LogicList.Companion.logicListOf
import org.klogic.utils.terms.LogicPair
import org.klogic.utils.terms.Symbol
import org.klogic.utils.terms.Symbol.Companion.toSymbol
import org.klogic.utils.terms.logicTo
import org.klogic.utils.withEmptyContext

class WildcardsTest {
    @Test
    fun `x !== __ -- failure`() {
        withEmptyContext {
            val goal = { x: Term<Symbol> -> x `!==` freshTypedWildcard() }

            val run = run(10, goal)
            assertTrue(run.isEmpty())
        }
    }

    @Test
    fun `x !== (__, 1) && x === (1, y)`() {
        withEmptyContext {
            val one = "1".toSymbol()

            fun goal(x: Term<LogicPair<Symbol, Symbol>>, y: Term<Symbol>): Goal =
                (x `!==` (freshTypedWildcard<Symbol>() logicTo one)) and (x `===` (one logicTo y))

            val y = freshTypedVar<Symbol>()
            val x = freshTypedVar<LogicPair<Symbol, Symbol>>()

            val run = run(10, x, goal(x, y))
            val answer = run.single()

            val answerTerm = answer.term.asReified()
            assertEquals(one, answerTerm.first)

            // x == (1, y), y != 1
            val inequalityConstraint = answer.constraints.single() as InequalityConstraint
            val answerConstraint = inequalityConstraint.simplifiedConstraints.single()
            assertEquals(answerTerm.second, answerConstraint.unboundedValue)
            assertEquals(one, answerConstraint.term)
        }
    }

    @Test
    fun `(1, __) != (__, 1) --- failure`() {
        withEmptyContext {
            val one = "1".toSymbol()

            val goal = { _: Term<*> -> (one logicTo freshTypedWildcard<Symbol>()) `!==` (freshTypedWildcard<Symbol>() logicTo one) }

            val run = run(10, goal)
            assertTrue(run.isEmpty())
        }
    }

    @Test
    fun `(x, 2, __) != (1, __, 2) --- x != 1`() {
        withEmptyContext {
            val one = "1".toSymbol()
            val two = "2".toSymbol()

            val goal = { x: Term<Symbol> ->
                logicListOf(x, two, freshTypedWildcard()) `!==` logicListOf(one, freshTypedWildcard(), two)
            }

            val run = run(10, goal)
            val answer = run.single()

            val variable = answer.term
            assertTrue(variable is Var)

            val inequalityConstraint = answer.constraints.single() as InequalityConstraint
            val answerConstraint = inequalityConstraint.simplifiedConstraints.single()

            assertEquals(variable, answerConstraint.unboundedValue)
            assertEquals(one, answerConstraint.term)
        }
    }

    @Test
    fun `(x, y) != (1, __) --- x != 1`() {
        withEmptyContext {
            val one = "1".toSymbol()

            val x = freshTypedVar<Symbol>()
            val y = freshTypedVar<Symbol>()
            val goal = { symbol: Term<Symbol> ->
                logicListOf(symbol, y) `!==` logicListOf(one, freshTypedWildcard())
            }

            val run = run(10, x, goal(x))
            val answer = run.single()

            val variable = answer.term
            assertTrue(variable is Var)

            val inequalityConstraint = answer.constraints.single() as InequalityConstraint
            val answerConstraint = inequalityConstraint.simplifiedConstraints.single()

            assertEquals(variable, answerConstraint.unboundedValue)
            assertEquals(one, answerConstraint.term)
        }
    }
}
