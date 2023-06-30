package org.klogic.core.wildcards

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.klogic.core.*
import org.klogic.core.InequalityConstraint.SingleInequalityConstraint
import org.klogic.core.Var.Companion.createTypedVar
import org.klogic.utils.singleReifiedTerm
import org.klogic.utils.terms.LogicPair
import org.klogic.utils.terms.Symbol
import org.klogic.utils.terms.Symbol.Companion.toSymbol
import org.klogic.utils.withEmptyContext
import org.klogic.utils.terms.logicTo

class WildcardsTest {
    @Test
    @DisplayName("x !== *")
    fun testAlwaysFail() {
        withEmptyContext {
            val goal = { x: Term<Symbol> -> x `!==` freshTypedWildcard() }

            val run = run(10, goal)
            assertTrue(run.isEmpty())
        }
    }

    @Test
    @DisplayName("x === *")
    fun testAlwaysSuccess() {
        withEmptyContext {
            val goal = { x: Term<Symbol> -> x `===` freshTypedWildcard() }

            val run = run(10, goal)
            assertTrue(run.single().term is Var)
        }
    }

    @Test
    @DisplayName("x === * && y === *")
    fun testFewWildcards() {
        withEmptyContext {
            val a = (-1).createTypedVar<Symbol>()
            val b = (-2).createTypedVar<Symbol>()
            val goal = { x: Term<Symbol>, y: Term<Symbol> -> (x `===` freshTypedWildcard()) and (y `===` freshTypedWildcard()) }

            val run = unreifiedRun(10, goal(a, b))
            val answers = listOf(a, b).map { run.reify(it) }

            assertEquals(a, answers[0].single().term)
            assertEquals(b, answers[1].single().term)
        }
    }

    @Test
    @DisplayName("x !== (1, *) && x === (1, *)")
    fun testWildcardInTermWithDisequality() {
        withEmptyContext {
            val one = "1".toSymbol()
            val goal = { x: Term<LogicPair<Symbol, Symbol>> ->
                (x `!==` (freshTypedWildcard<Symbol>() logicTo one)) and (x `===` (one logicTo freshTypedWildcard()))
            }

            val run = run(10, goal)
            val answer = run.single()

            assertEquals(one, answer.term.asReified().first)

            val wildcardInPair = answer.term.asReified().second as Wildcard<Symbol>
            val answerConstraint = answer.constraints.single() as InequalityConstraint

            val expectedConstraint = SingleInequalityConstraint(wildcardInPair, one)
            assertTrue(answerConstraint.simplifiedConstraints.any { it == expectedConstraint })
        }
    }

    @Test
    @DisplayName("(*, 1) === (1, *)")
    fun testWildcardsInTerms() {
        withEmptyContext {
            val goal = { _: Term<*> -> (freshTypedWildcard<Symbol>() logicTo "1".toSymbol()) `===` ("1".toSymbol() logicTo freshTypedWildcard()) }

            val run = run(10, goal)
            val answer = run.single()

            assertTrue(answer.term is Var)
            assertTrue(answer.constraints.isEmpty())
        }
    }

    @Test
    @DisplayName("x === * && x === 5")
    fun testRedundantWildcard() {
        withEmptyContext {
            val goal = { x: Term<Symbol> -> x `===` freshTypedWildcard() and (x `===` "5".toSymbol()) }

            val run = run(10, goal)
            assertEquals("5".toSymbol(), run.singleReifiedTerm)
        }
    }
}
