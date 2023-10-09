package org.klogic.core.wildcards

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.klogic.core.Goal
import org.klogic.core.InequalityConstraint
import org.klogic.core.Term
import org.klogic.core.Var
import org.klogic.core.and
import org.klogic.core.conde
import org.klogic.core.delay
import org.klogic.core.freshTypedVars
import org.klogic.utils.singleReifiedTerm
import org.klogic.utils.terms.Cons
import org.klogic.utils.terms.LogicList
import org.klogic.utils.terms.LogicList.Companion.logicListOf
import org.klogic.utils.terms.LogicPair
import org.klogic.utils.terms.Nil.emptyLogicList
import org.klogic.utils.terms.PeanoLogicNumber
import org.klogic.utils.terms.Symbol
import org.klogic.utils.terms.Symbol.Companion.toSymbol
import org.klogic.utils.terms.ZeroNaturalNumber.Z
import org.klogic.utils.terms.logicTo
import org.klogic.utils.terms.toPeanoLogicNumber
import org.klogic.utils.terms.unaryPlus
import org.klogic.utils.withEmptyContext

class WildcardsTest {
    @Test
    fun `x !== __ -- failure`() {
        withEmptyContext {
            val goal = { x: Term<Symbol> -> x `!==` wildcard() }

            val run = run(10, goal)
            assertTrue(run.isEmpty())
        }
    }

    @Test
    fun `x !== (__, 1) && x === (1, y)`() {
        withEmptyContext {
            val one = "1".toSymbol()

            fun goal(x: Term<LogicPair<Symbol, Symbol>>, y: Term<Symbol>): Goal =
                (x `!==` (wildcard<Symbol>() logicTo one)) and (x `===` (one logicTo y))

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

            val goal = { _: Term<*> -> (one logicTo wildcard<Symbol>()) `!==` (wildcard<Symbol>() logicTo one) }

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
                logicListOf(x, two, wildcard()) `!==` logicListOf(one, wildcard(), two)
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
                logicListOf(symbol, y) `!==` logicListOf(one, wildcard())
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

    @Test
    fun `x lt 4`() {
        withEmptyContext {
            fun allPeanoNumbers(x: Term<PeanoLogicNumber>): Goal = conde(
                x `===` Z,
                freshTypedVars<PeanoLogicNumber> { prev ->
                    (x `===` +prev) and delay { allPeanoNumbers(prev) }
                }
            )

            fun lessThan4(x: Term<PeanoLogicNumber>) = x `!==` +(+(+(+(wildcard<PeanoLogicNumber>()))))

            val goal = { x: Term<PeanoLogicNumber> -> lessThan4(x) and allPeanoNumbers(x) }

            val run = run(10, goal)
            assertEquals(4, run.size)
            assertEquals(Z, run[0].term)
            assertEquals(1.toPeanoLogicNumber(), run[1].term)
            assertEquals(2.toPeanoLogicNumber(), run[2].term)
            assertEquals(3.toPeanoLogicNumber(), run[3].term)
        }
    }

    @Test
    fun `x != Cons --- x == ()`() {
        withEmptyContext {
            val value = "42".toSymbol()

            fun allLists(x: Term<LogicList<Symbol>>): Goal = conde(
                x `===` emptyLogicList(),
                freshTypedVars<LogicList<Symbol>> { prev ->
                    (x `===` Cons(value, prev)) and delay { allLists(prev) }
                }
            )

            fun isNotCons(x: Term<LogicList<Symbol>>) = x `!==` Cons(wildcard(), wildcard())

            val goal = { x: Term<LogicList<Symbol>> -> isNotCons(x) and allLists(x) }

            val run = run(10, goal)
            val answer = run.single()
            assertEquals(emptyLogicList(), answer.term)
        }
    }

    @Test
    fun `test saving disequality constraints for wildcards`() {
        withEmptyContext {
            val innerGoal = { x: Term<LogicList<Symbol>> ->
                conde(
                    x `===` logicListOf("1".toSymbol()),
                    freshTypedVars<Symbol, Symbol> { first, second ->
                        x `===` logicListOf(first, second)
                    }
                )
            }

            val goal = { x: Term<LogicList<Symbol>> ->
                and(
                    x `!==` logicListOf(wildcard(), wildcard()),
                    innerGoal(x),
                )
            }

            val answers = run(10, goal)
            val expectedTerm = logicListOf("1".toSymbol())
            assertEquals(expectedTerm, answers.singleReifiedTerm)
        }
    }
}
