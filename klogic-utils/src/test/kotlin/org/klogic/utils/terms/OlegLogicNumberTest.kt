@file:Suppress("NonAsciiCharacters", "TestFunctionName")

package org.klogic.utils.terms

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.klogic.core.*
import org.klogic.core.Var.Companion.createTypedVar
import org.klogic.utils.singleReifiedTerm
import org.klogic.utils.terms.LogicList.Companion.logicListOf
import org.klogic.utils.terms.OlegLogicNumber.Companion.digitOne
import org.klogic.utils.terms.OlegLogicNumber.Companion.digitZero
import org.klogic.utils.terms.OlegLogicNumber.Companion.numberZero
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

            assertEquals(x, firstTenPositiveNumbers.singleReifiedTerm)
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

            assertEquals(x, firstTenPositiveNumbers.singleReifiedTerm)
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

            val run = run(6, q, goal)

            fun ReifiedTerm<LogicList<OlegLogicNumber>>.listTerm(): LogicList<OlegLogicNumber> =
                term as LogicList<OlegLogicNumber>

            fun Term<OlegLogicNumber>.reifiedDigits(): List<Term<Digit>> = asReified().digits.asReified().toList()

            val numberTwo = 2u.toOlegLogicNumber()
            val numberThree = 3u.toOlegLogicNumber()
            val numberFour = 4u.toOlegLogicNumber()

            /*
            ReifiedTerm(term=(_.-3, (), _.-3), constraints=[])
            ReifiedTerm(term=((), (_.0, _.1), (_.0, _.1)), constraints=[])
            ReifiedTerm(term=((1), (1), (0, 1)), constraints=[])
            ReifiedTerm(term=((1), (0, _.11, _.12), (1, _.11, _.12)), constraints=[])
            ReifiedTerm(term=((1), (1, 1), (0, 0, 1)), constraints=[])
            ReifiedTerm(term=((0, _.17, _.18), (1), (1, _.17, _.18)), constraints=[])
            */
            (run[0].listTerm()).let {
                assertEquals(r, it[0])
                assertEquals(numberZero, it[1])
                assertEquals(r, it[2])
            }
            (run[1].listTerm()).let {
                assertEquals(numberZero, it[0])

                val firstDigit = it[1].asReified()[0]
                val secondDigit = it[1].asReified()[1]

                // Check they are new variables
                assertTrue(firstDigit.isVar() && (firstDigit as Var<Digit>).index >= 0)
                assertTrue(secondDigit.isVar() && (secondDigit as Var<Digit>).index >= 0)
                assertTrue(firstDigit != secondDigit)

                assertEquals(firstDigit, (it[2] as OlegLogicNumber)[0])
                assertEquals(secondDigit, (it[2] as OlegLogicNumber)[1])
            }
            (run[2].listTerm()).let {
                assertEquals(numberOne, it[0])
                assertEquals(numberOne, it[1])
                assertEquals(numberTwo, it[2])
            }
            (run[3].listTerm()).let {
                assertEquals(numberOne, it[0])

                it[1].reifiedDigits().zip(it[2].reifiedDigits()).toList().let { pairedDigits ->
                    assertEquals(digitZero, pairedDigits[0].first)
                    assertEquals(digitOne, pairedDigits[0].second)

                    assertEquals(pairedDigits[1].first, pairedDigits[1].second)
                    assertTrue(pairedDigits[1].first.isVar())

                    assertEquals(pairedDigits[2].first, pairedDigits[2].second)
                    assertTrue(pairedDigits[2].first.isVar())
                }
            }
            (run[4].listTerm()).let {
                assertEquals(numberOne, it[0])
                assertEquals(numberThree, it[1])
                assertEquals(numberFour, it[2])
            }
            (run[5].listTerm()).let {
                assertEquals(digitZero, it[0].reifiedDigits().first().asReified())
                assertEquals(numberOne, it[1])
                assertEquals(digitOne, it[2].reifiedDigits().first().asReified())

                assertEquals(it[0].reifiedDigits().drop(1), it[2].reifiedDigits().drop(1))
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
                    assertEquals(expectedResult, run.singleReifiedTerm.asReified().toUInt())
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

                    assertEquals(i, run.singleReifiedTerm.asReified().toUInt())
                }
            }
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

                    assertEquals(j, run.singleReifiedTerm.asReified().toUInt())
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

            val r = run(10, { r: Term<OlegLogicNumber> -> logᴼ(n, b, q, r) }).singleReifiedTerm.asReified().toUInt()
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
                    assertEquals(expectedResult, run.singleReifiedTerm.asReified().toUInt())
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

                    assertEquals(i, run.singleReifiedTerm.asReified().toUInt())
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

                    assertEquals(j, run.singleReifiedTerm.asReified().toUInt())
                }
            }
        }
    }
}