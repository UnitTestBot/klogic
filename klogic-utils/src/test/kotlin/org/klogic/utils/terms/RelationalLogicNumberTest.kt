@file:Suppress("TestFunctionName", "NonAsciiCharacters")

package org.klogic.utils.terms

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.klogic.core.Var.Companion.createTypedVar
import org.klogic.core.reified
import org.klogic.core.reify
import org.klogic.core.run
import org.klogic.core.unreifiedRun
import org.klogic.utils.singleReifiedTerm
import org.klogic.utils.terms.RelationalLogicNumber.Companion.succ
import org.klogic.utils.terms.ZeroNaturalNumber.Z

class RelationalLogicNumberTest {
    private val three: SuccNaturalNumber = succ(two)
    private val four: SuccNaturalNumber = succ(three)

    @Test
    @DisplayName("0 + 1 == q -> q == 1")
    fun testAddᴼ1() {
        val q = (-1).createTypedVar<RelationalLogicNumber>()

        val goal = addᴼ(Z, one, q)

        val run = run(2, q, goal)

        val expectedTerm = one

        assertEquals(expectedTerm, run.singleReifiedTerm)
    }

    @Test
    @DisplayName("1 + 1 == q -> q == 2")
    fun testAddᴼ2() {
        val q = (-1).createTypedVar<RelationalLogicNumber>()

        val goal = addᴼ(one, one, q)

        val run = run(2, q, goal)

        val expectedTerm = two

        assertEquals(expectedTerm, run.singleReifiedTerm)
    }

    @Test
    @DisplayName("q + 1 == 1 -> q == 0")
    fun testAddᴼ3() {
        val q = (-1).createTypedVar<RelationalLogicNumber>()

        val goal = addᴼ(q, one, one)

        val run = run(2, q, goal)

        val expectedTerm = Z

        assertEquals(expectedTerm, run.singleReifiedTerm)
    }

    @Test
    @DisplayName("1 + q == 1 -> q == 0")
    fun testAddᴼ4() {
        val q = (-1).createTypedVar<RelationalLogicNumber>()

        val goal = addᴼ(one, q, one)

        val run = run(2, q, goal)

        val expectedTerm = Z

        assertEquals(expectedTerm, run.singleReifiedTerm)
    }

    @Test
    @DisplayName("q + r == 4")
    fun testAddᴼ5() {
        val q = (-1).createTypedVar<RelationalLogicNumber>()
        val r = (-2).createTypedVar<RelationalLogicNumber>()

        val goal = addᴼ(q, r, four)

        val unreifiedRun = unreifiedRun(7, goal)

        val reifiedTerms = unreifiedRun.reify(q).zip(unreifiedRun.reify(r))

        val expectedTerms = listOf(
            zero to four,
            one to three,
            two to two,
            three to one,
            four to zero
        ).map { it.first.reified() to it.second.reified() }

        assertEquals(expectedTerms, reifiedTerms)
    }

    @Test
    @DisplayName("0 * 1 == q -> q == 0")
    fun testMulᴼ1() {
        val q = (-1).createTypedVar<RelationalLogicNumber>()

        val goal = mulᴼ(Z, one, q)

        val run = run(2, q, goal)

        val expectedTerm = Z

        assertEquals(expectedTerm, run.singleReifiedTerm)
    }

    @Test
    @DisplayName("2 * 2 == q -> q == 4")
    fun testMulᴼ2() {
        val q = (-1).createTypedVar<RelationalLogicNumber>()

        val goal = mulᴼ(two, two, q)

        val run = run(2, q, goal)

        val expectedTerm = four

        assertEquals(expectedTerm, run.singleReifiedTerm)
    }

    @Test
    @DisplayName("q * 2 == 2 -> q == 1")
    fun testMulᴼ3() {
        val q = (-1).createTypedVar<RelationalLogicNumber>()

        val goal = mulᴼ(q, two, two)

        val run = run(2, q, goal)

        val expectedTerm = one

        assertEquals(expectedTerm, run.singleReifiedTerm)
    }

    @Test
    @DisplayName("q * 2 == 3 -> no answer")
    fun testMulᴼ4() {
        val q = (-1).createTypedVar<RelationalLogicNumber>()

        val goal = mulᴼ(q, two, three)

        val run = run(2, q, goal)

        assertTrue(run.isEmpty())
    }

    @Test
    @DisplayName("2 * q == 2 -> q == 1")
    fun testMulᴼ5() {
        val q = (-1).createTypedVar<RelationalLogicNumber>()

        val goal = mulᴼ(two, q, two)

        val run = run(2, q, goal)

        val expectedTerm = one

        assertEquals(expectedTerm, run.singleReifiedTerm)
    }

    @Test
    @DisplayName("q * 1 == r")
    fun testMulᴼ6() {
        val q = (-1).createTypedVar<RelationalLogicNumber>()
        val r = (-2).createTypedVar<RelationalLogicNumber>()

        val goal = mulᴼ(q, one, r)

        val unreifiedRun = unreifiedRun(10, goal)

        val reifiedTerms = unreifiedRun.reify(q).zip(unreifiedRun.reify(r))

        val expectedTerms = (0..9).map { int ->
            int.toRelationalLogicNumber().reified().let { it to it }
        }

        assertEquals(expectedTerms, reifiedTerms)
    }
}
