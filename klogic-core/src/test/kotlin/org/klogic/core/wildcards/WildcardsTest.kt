package org.klogic.core.wildcards

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.klogic.core.*
import org.klogic.core.Var.Companion.createTypedVar
import org.klogic.utils.singleReifiedTerm
import org.klogic.utils.terms.LogicPair
import org.klogic.utils.terms.Symbol
import org.klogic.utils.terms.Symbol.Companion.toSymbol
import org.klogic.utils.withEmptyContext
import org.klogic.utils.terms.logicTo

class WildcardsTest {
    @Test
    fun testAlwaysFail() {
        withEmptyContext {
            val goal = { x: Term<Symbol> -> x `!==` Wildcard() }

            val run = run(10, goal)
            assertTrue(run.isEmpty())
        }
    }

    @Test
    fun testAlwaysSuccess() {
        withEmptyContext {
            val goal = { x: Term<Symbol> -> x `===` Wildcard() }

            val run = run(10, goal)
            assertTrue(run.single().term is Var)
        }
    }

    @Test
    fun testFewWildcards() {
        withEmptyContext {
            val a = (-1).createTypedVar<Symbol>()
            val b = (-2).createTypedVar<Symbol>()
            val goal = { x: Term<Symbol>, y: Term<Symbol> -> (x `===` Wildcard()) and (y `===` Wildcard()) }

            val run = unreifiedRun(10, goal(a, b))
            val answers = listOf(a, b).map { run.reify(it) }

            assertEquals(a, answers[0].single().term)
            assertEquals(b, answers[1].single().term)
        }
    }

    @Test
    fun testWildcardInTermWithDisequality() {
        withEmptyContext {
            val goal = { x: Term<LogicPair<Symbol, Symbol>> ->
                (x `!==` (Wildcard<Symbol>() logicTo "1".toSymbol())) and (x `===` ("1".toSymbol() logicTo Wildcard()))
            }

            val run = run(10, goal)
            println(run)
            assertTrue(run.single().term is Var)
        }
    }

    @Test
    fun testWildcardsInTerms() {
        withEmptyContext {
            val goal = { _: Term<*> -> (Wildcard<Symbol>() logicTo "1".toSymbol()) `===` ("1".toSymbol() logicTo Wildcard()) }

            val run = run(10, goal)
            val answer = run.single()

            assertTrue(answer.term is Var)
            assertTrue(answer.constraints.isEmpty())
        }
    }

    @Test
    fun testRedundantWildcard() {
        withEmptyContext {
            val goal = { x: Term<Symbol> -> x `===` Wildcard() and (x `===` "5".toSymbol()) }

            val run = run(10, goal)
            assertEquals("5".toSymbol(), run.singleReifiedTerm)
        }
    }
}
