@file:Suppress("NonAsciiCharacters", "TestFunctionName")

package org.klogic.utils.terms

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.klogic.core.ReifiedTerm
import org.klogic.core.Term
import org.klogic.core.Var
import org.klogic.core.Var.Companion.createTypedVar
import org.klogic.core.and
import org.klogic.core.reified
import org.klogic.core.run
import org.klogic.utils.singleReifiedTerm
import org.klogic.utils.terms.Cons.Companion.logicListOf
import org.klogic.utils.terms.OlegLogicNumber.Companion.digitOne
import org.klogic.utils.terms.OlegLogicNumber.Companion.digitZero
import org.klogic.utils.terms.OlegLogicNumber.Companion.numberZero
import org.klogic.utils.terms.OlegLogicNumber.Companion.toOlegLogicNumber

class OlegLogicNumberTest {
    @Test
    fun testTruePosᴼ() {
        val x = (-1).createTypedVar<OlegLogicNumber>()

        val positiveNumber = 1u.toOlegLogicNumber()

        val goal = posᴼ(positiveNumber)

        val firstTenPositiveNumbers = run(10, x, goal)

        assertEquals(x, firstTenPositiveNumbers.singleReifiedTerm)
    }

    @Test
    fun testFalsePosᴼ() {
        val x = (-1).createTypedVar<OlegLogicNumber>()

        val zero = 0u.toOlegLogicNumber()

        val goal = posᴼ(zero)

        val firstTenPositiveNumbers = run(10, x, goal)

        assertTrue(firstTenPositiveNumbers.isEmpty())
    }

    @Test
    fun testTrueGreaterThan1ᴼ() {
        val x = (-1).createTypedVar<OlegLogicNumber>()

        val number2 = 2u.toOlegLogicNumber()

        val goal = greaterThen1ᴼ(number2)

        val firstTenPositiveNumbers = run(10, x, goal)

        assertEquals(x, firstTenPositiveNumbers.singleReifiedTerm)
    }

    @Test
    fun testFalseGreaterThan1ᴼ() {
        val x = (-1).createTypedVar<OlegLogicNumber>()

        val goal = greaterThen1ᴼ(numberOne)

        val firstTenPositiveNumbers = run(10, x, goal)

        assertTrue(firstTenPositiveNumbers.isEmpty())
    }

    @Test
    fun generateTriplesTest() {
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
        ReifiedTerm(term=((0, 1), (0, 1), (0, 0, 1)), constraints=[])
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
            assertEquals(numberTwo, it[0])
            assertEquals(numberTwo, it[1])
            assertEquals(numberFour, it[2])
        }
    }

    @Test
    fun minusTest() {
        val x = 8u.toOlegLogicNumber()
        val y = (-1).createTypedVar<OlegLogicNumber>()
        val q = 3u.toOlegLogicNumber()

        val goal = minusᴼ(x, y, q)

        val run = run(9, y, goal)

        val expectedTerms = listOf(5u.toOlegLogicNumber())

        assertEquals(expectedTerms.reified(), run)
    }
}