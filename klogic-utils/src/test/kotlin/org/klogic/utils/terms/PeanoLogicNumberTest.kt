@file:Suppress("TestFunctionName", "NonAsciiCharacters")

package org.klogic.utils.terms

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.klogic.core.*
import org.klogic.core.Var.Companion.createTypedVar
import org.klogic.utils.singleReifiedTerm
import org.klogic.utils.terms.LogicList.Companion.logicListOf
import org.klogic.utils.terms.LogicTruᴼ.truᴼ
import org.klogic.utils.terms.PeanoLogicNumber.Companion.succ
import org.klogic.utils.terms.ZeroNaturalNumber.Z
import kotlin.system.measureTimeMillis
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

class PeanoLogicNumberTest {
    private val three: NextNaturalNumber = succ(two)
    private val four: NextNaturalNumber = succ(three)

    @Test
    @DisplayName("0 + 1 == q -> q == 1")
    fun testAddᴼ1() {
        val q = (-1).createTypedVar<PeanoLogicNumber>()

        val goal = addᴼ(Z, one, q)

        val run = run(2, q, goal)

        val expectedTerm = one

        assertEquals(expectedTerm, run.singleReifiedTerm)
    }

    @Test
    @DisplayName("1 + 1 == q -> q == 2")
    fun testAddᴼ2() {
        val q = (-1).createTypedVar<PeanoLogicNumber>()

        val goal = addᴼ(one, one, q)

        val run = run(2, q, goal)

        val expectedTerm = two

        assertEquals(expectedTerm, run.singleReifiedTerm)
    }

    @Test
    @DisplayName("q + 1 == 1 -> q == 0")
    fun testAddᴼ3() {
        val q = (-1).createTypedVar<PeanoLogicNumber>()

        val goal = addᴼ(q, one, one)

        val run = run(2, q, goal)

        val expectedTerm = Z

        assertEquals(expectedTerm, run.singleReifiedTerm)
    }

    @Test
    @DisplayName("1 + q == 1 -> q == 0")
    fun testAddᴼ4() {
        val q = (-1).createTypedVar<PeanoLogicNumber>()

        val goal = addᴼ(one, q, one)

        val run = run(2, q, goal)

        val expectedTerm = Z

        assertEquals(expectedTerm, run.singleReifiedTerm)
    }

    @Test
    @DisplayName("q + r == 4")
    fun testAddᴼ5() {
        val q = (-1).createTypedVar<PeanoLogicNumber>()
        val r = (-2).createTypedVar<PeanoLogicNumber>()

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
        val q = (-1).createTypedVar<PeanoLogicNumber>()

        val goal = mulᴼ(Z, one, q)

        val run = run(2, q, goal)

        val expectedTerm = Z

        assertEquals(expectedTerm, run.singleReifiedTerm)
    }

    @Test
    @DisplayName("2 * 2 == q -> q == 4")
    fun testMulᴼ2() {
        val q = (-1).createTypedVar<PeanoLogicNumber>()

        val goal = mulᴼ(two, two, q)

        val run = run(2, q, goal)

        val expectedTerm = four

        assertEquals(expectedTerm, run.singleReifiedTerm)
    }

    @Test
    @DisplayName("q * 2 == 2 -> q == 1")
    fun testMulᴼ3() {
        val q = (-1).createTypedVar<PeanoLogicNumber>()

        val goal = mulᴼ(q, two, two)

        val run = run(2, q, goal)

        val expectedTerm = one

        assertEquals(expectedTerm, run.singleReifiedTerm)
    }

    @Test
    @DisplayName("q * 2 == 3 -> no answer")
    fun testMulᴼ4() {
        val q = (-1).createTypedVar<PeanoLogicNumber>()

        val goal = mulᴼ(q, two, three)

        val run = run(2, q, goal)

        assertTrue(run.isEmpty())
    }

    @Test
    @DisplayName("2 * q == 2 -> q == 1")
    fun testMulᴼ5() {
        val q = (-1).createTypedVar<PeanoLogicNumber>()

        val goal = mulᴼ(two, q, two)

        val run = run(2, q, goal)

        val expectedTerm = one

        assertEquals(expectedTerm, run.singleReifiedTerm)
    }

    @Test
    @DisplayName("q * 1 == r")
    fun testMulᴼ6() {
        val q = (-1).createTypedVar<PeanoLogicNumber>()
        val r = (-2).createTypedVar<PeanoLogicNumber>()

        val goal = mulᴼ(q, one, r)

        val unreifiedRun = unreifiedRun(10, goal)

        val reifiedTerms = unreifiedRun.reify(q).zip(unreifiedRun.reify(r))

        val expectedTerms = (0..9).map { int ->
            int.toPeanoLogicNumber().reified().let { it to it }
        }

        assertEquals(expectedTerms, reifiedTerms)
    }

    @Test
    @DisplayName("minmax(5, 6) == minmax(6, 5) = (5, 6)")
    fun testMinMaxᴼ() {
        val q = (-1).createTypedVar<PeanoLogicNumber>()
        val r = (-2).createTypedVar<PeanoLogicNumber>()

        val five = 5.toPeanoLogicNumber()
        val six = 6.toPeanoLogicNumber()
        val goal = minMaxᴼ(q, r, five, six)

        val unreifiedRun = unreifiedRun(10, goal)

        val reifiedTerms = unreifiedRun.reify(q).zip(unreifiedRun.reify(r))

        val expectedTerms = listOf(
            five to six,
            six to five
        ).map { it.first.reified() to it.second.reified() }

        assertEquals(expectedTerms, reifiedTerms)
    }

    @Test
    @DisplayName("all permutations of [1, 2, 3]")
    fun testAllPermutations() {
        val unsortedList = (-1).createTypedVar<LogicList<PeanoLogicNumber>>()

        val sortedList = logicListOf(one, two, three)

        val goal = sortᴼ(unsortedList, sortedList)

        val run = run(7, unsortedList, goal)

        val expectedTerms = listOf(
            logicListOf(one, two, three),
            logicListOf(two, one, three),
            logicListOf(one, three, two),
            logicListOf(two, three, one),
            logicListOf(three, one, two),
            logicListOf(three, two, one),
        ).map { it.reified() }

        assertEquals(expectedTerms, run)
    }

    @OptIn(ExperimentalTime::class)
    @Test
    fun testWithoutIncrementality() {
        val number = (-1).createTypedVar<PeanoLogicNumber>()

        var greaterThanOrEqualGoal = success
        val timeouts = mutableListOf<Long>()

        measureTimeMillis {
            for (i in 0..150) {
                val lowerBound = i.toPeanoLogicNumber()

                greaterThanOrEqualGoal = greaterThanOrEqualGoal and greaterThanOrEqualᴼ(number, lowerBound, truᴼ)

                val run = measureTimedValue { run(2, number, greaterThanOrEqualGoal) }
                timeouts += run.duration.inWholeMilliseconds
            }
        }.let {
            println("Without: $it ms")
        }

        timeouts.forEachIndexed { i, time ->
            println("$i: $time ms")
        }
    }

    @OptIn(ExperimentalTime::class)
    @Test
    fun testWithIncrementality() {
        val number = (-1).createTypedVar<PeanoLogicNumber>()

//        val greaterThanOrEqualGoal = success
        var states = listOf(State.empty)
        val timeouts = mutableListOf<Long>()
        val values = mutableListOf<Term<PeanoLogicNumber>>()

        measureTimeMillis {
            for (i in 0..150) {
                val lowerBound = i.toPeanoLogicNumber()

                val run = measureTimedValue {
                    val stream = and(states, greaterThanOrEqualᴼ(number, lowerBound, truᴼ))
                    states = stream.take(2)
                    values += states.map { it.reify(number) }.single().term
                }
                timeouts += run.duration.inWholeMilliseconds
            }
        }.let {
            println("With: $it ms")
        }

        timeouts.forEachIndexed { i, time ->
            println("$i: $time ms")
        }

        values.forEachIndexed { i, value ->
            println("$i: $value")
        }
    }
}
