@file:Suppress("NonAsciiCharacters", "TestFunctionName")

package org.klogic.utils.terms

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.klogic.core.*
import org.klogic.core.Var.Companion.createTypedVar
import org.klogic.utils.listeners.UnificationCounter
import org.klogic.utils.singleReifiedTerm
import org.klogic.utils.terms.LogicList.Companion.logicListOf
import org.klogic.utils.terms.OlegLogicNumber.Companion.toOlegLogicNumber
import org.klogic.utils.withEmptyContext
import kotlin.math.pow

class OlegLogicNumberTest {
    @Test
    fun testTruePosᴼ() {
        withEmptyContext {
            val x = (-1).createTypedVar<OlegLogicNumber>()

            val positiveNumber = 1u.toOlegLogicNumber()

            val goal = posᴼ(positiveNumber)

            val firstTenPositiveNumbers = run(10, x, goal)

            assertEquals(x, firstTenPositiveNumbers.single().term)
        }
    }

    @Test
    fun testFalsePosᴼ() {
        withEmptyContext {
            val x = (-1).createTypedVar<OlegLogicNumber>()

            val zero = 0u.toOlegLogicNumber()

            val goal = posᴼ(zero)

            val firstTenPositiveNumbers = run(10, x, goal)

            assertTrue(firstTenPositiveNumbers.isEmpty())
        }
    }

    @Test
    fun testTrueGreaterThan1ᴼ() {
        withEmptyContext {
            val x = (-1).createTypedVar<OlegLogicNumber>()

            val number2 = 2u.toOlegLogicNumber()

            val goal = greaterThan1ᴼ(number2)

            val firstTenPositiveNumbers = run(10, x, goal)

            assertEquals(x, firstTenPositiveNumbers.single().term)
        }
    }

    @Test
    fun testFalseGreaterThan1ᴼ() {
        withEmptyContext {
            val x = (-1).createTypedVar<OlegLogicNumber>()

            val goal = greaterThan1ᴼ(numberOne)

            val firstTenPositiveNumbers = run(10, x, goal)

            assertTrue(firstTenPositiveNumbers.isEmpty())
        }
    }

    @Test
    fun generateTriplesTest() {
        withEmptyContext {
            val x = (-1).createTypedVar<OlegLogicNumber>()
            val y = (-2).createTypedVar<OlegLogicNumber>()
            val r = (-3).createTypedVar<OlegLogicNumber>()
            val q = (-4).createTypedVar<LogicList<OlegLogicNumber>>()

            val goal = plusᴼ(x, y, r) and (logicListOf(x, y, r) `===` q)

            val count = 6
            val answers = run(count, q, goal)

            fun ReifiedTerm<LogicList<OlegLogicNumber>>.listTerm(): LogicList<OlegLogicNumber> =
                term as LogicList<OlegLogicNumber>

            fun Term<OlegLogicNumber>.reifiedDigits(): List<Term<Digit>> = asReified().digits.asReified().toList()

            assertEquals(count, answers.size)
            assertEquals(count, answers.distinct().size) // Check that we have different answers
            answers.forEach { (_, constraints) ->
                assert(constraints.isEmpty())
            }
        }
    }

    @Test
    fun minusTest() {
        withEmptyContext {
            val x = 8u.toOlegLogicNumber()
            val y = (-1).createTypedVar<OlegLogicNumber>()
            val q = 3u.toOlegLogicNumber()

            val goal = minusᴼ(x, y, q)

            val run = run(9, y, goal)

            val expectedTerms = listOf(5u.toOlegLogicNumber())

            assertEquals(expectedTerms.reified(), run)
        }
    }

    @Test
    fun forwardMultiplicationTest() {
        withEmptyContext {
            for (i in 0u until 5u) {
                val first = i.toOlegLogicNumber()

                for (j in 0u until 5u) {
                    val second = j.toOlegLogicNumber()
                    val result = (-1).createTypedVar<OlegLogicNumber>()

                    val run = run(10, result, mulᴼ(first, second, result))

                    val expectedResult = i * j
                    assertEquals(expectedResult, run.singleReifiedTerm.toUInt())
                }
            }
        }
    }

    @Test
    fun backwardFirstArgumentMultiplicationTest() {
        withEmptyContext {
            for (i in 1u..5u) {
                val first = (-1).createTypedVar<OlegLogicNumber>()

                for (j in 1u..5u) {
                    val second = j.toOlegLogicNumber()
                    val result = (i * j).toOlegLogicNumber()

                    val run = run(10, first, mulᴼ(first, second, result))

                    assertEquals(i, run.singleReifiedTerm.toUInt())
                }
            }
        }
    }

    private val unificationsTracer = UnificationListener { firstTerm, secondTerm, stateBefore, _ ->
        println("${firstTerm.walk(stateBefore.substitution)} ${secondTerm.walk(stateBefore.substitution)}, ")
    }

    @Test
    fun testMul3x3WithTracing() {
        withEmptyContext {
            val unificationCounter = UnificationCounter()

            addUnificationListener(unificationCounter)
            addUnificationListener(unificationsTracer)

            val first = (3u).toOlegLogicNumber()
            val second = (3u).toOlegLogicNumber()

            val answer = run(1, { q: Term<OlegLogicNumber> -> mulᴼ(first, second, q) })

            println(answer.singleReifiedTerm)
            println("Unifications: ${unificationCounter.counter}")
        }
    }

    @Test
    fun testMul5x5WithTracing() {
        withEmptyContext {
            val unificationCounter = UnificationCounter()

            addUnificationListener(unificationCounter)
            addUnificationListener(unificationsTracer)

            val first = (5u).toOlegLogicNumber()
            val second = (5u).toOlegLogicNumber()

            val answer = run(1, { q: Term<OlegLogicNumber> -> mulᴼ(first, second, q) })

            println(answer.singleReifiedTerm)
            println("Unifications: ${unificationCounter.counter}")
        }
    }

    @Test
    fun backwardSecondArgumentMultiplicationTest() {
        withEmptyContext {
            for (i in 1u..5u) {
                val first = i.toOlegLogicNumber()

                for (j in 1u..5u) {
                    val result = (i * j).toOlegLogicNumber()
                    val second = (-1).createTypedVar<OlegLogicNumber>()

                    val run = run(10, second, mulᴼ(first, second, result))

                    assertEquals(j, run.singleReifiedTerm.toUInt())
                }
            }
        }
    }

    @Test
    fun testFactors() {
        withEmptyContext {
            val result = 12u.toOlegLogicNumber()
            val firstFactor = (-1).createTypedVar<OlegLogicNumber>()
            val secondFactor = (-2).createTypedVar<OlegLogicNumber>()

            val run = run(10, firstFactor, mulᴼ(firstFactor, secondFactor, result))

            val expectedTerms = listOf(1, 12, 2, 4, 3, 6).map { it.toUInt().toOlegLogicNumber().reified() }
            assertEquals(expectedTerms, run)
        }
    }

    @Test
    fun testBackwardDivision() {
        withEmptyContext {
            for (i in 1u..5u) {
                val m = i.toOlegLogicNumber()
                for (j in 1u..5u) {
                    val q = j.toOlegLogicNumber()
                    for (k in 0u until i) {
                        val r = k.toOlegLogicNumber()

                        val n = (-1).createTypedVar<OlegLogicNumber>()
                        val run = run(10, n, divᴼ(n, m, q, r))

                        val expectedN = i * j + k
                        assertEquals(expectedN.toOlegLogicNumber(), run.singleReifiedTerm)
                    }
                }
            }
        }
    }

    @Test
    fun logarithmTest() {
        withEmptyContext {
            val n = 14u.toOlegLogicNumber()
            val b = 2u.toOlegLogicNumber()
            val q = 3u.toOlegLogicNumber()

            val r = run(10, { r: Term<OlegLogicNumber> -> logᴼ(n, b, q, r) }).singleReifiedTerm.toUInt()
            assertEquals(6u, r)
        }
    }

    @Test
    fun forwardExponentTest() {
        withEmptyContext {
            for (i in 1u..3u) {
                val base = i.toOlegLogicNumber()

                for (j in 1u..5u) {
                    val power = j.toOlegLogicNumber()
                    val result = (-1).createTypedVar<OlegLogicNumber>()

                    val run = run(10, result, expᴼ(base, power, result))

                    val expectedResult = i.toDouble().pow(j.toInt()).toUInt()
                    assertEquals(expectedResult, run.singleReifiedTerm.toUInt())
                }
            }
        }
    }

    @Test
    fun backwardFirstArgumentExponentTest() {
        withEmptyContext {
            for (i in 1u..3u) {
                val base = (-1).createTypedVar<OlegLogicNumber>()

                for (j in 1u..5u) {
                    val power = j.toOlegLogicNumber()
                    val result = i.toDouble().pow(j.toInt()).toUInt().toOlegLogicNumber()

                    val run = run(10, base, expᴼ(base, power, result))

                    assertEquals(i, run.singleReifiedTerm.toUInt())
                }
            }
        }
    }

    @Test
    fun backwardSecondArgumentExponentTest() {
        withEmptyContext {
            for (i in 2u..3u) {
                val base = i.toOlegLogicNumber()

                for (j in 1u..4u) {
                    val power = (-1).createTypedVar<OlegLogicNumber>()
                    val result = i.toDouble().pow(j.toInt()).toUInt().toOlegLogicNumber()

                    val run = run(10, power, expᴼ(base, power, result))

                    assertEquals(j, run.singleReifiedTerm.toUInt())
                }
            }
        }
    }
}