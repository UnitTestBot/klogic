package org.klogic.unify

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.klogic.core.`&&&`
import org.klogic.core.InequalityConstraint
import org.klogic.core.Nil.nil
import org.klogic.core.Symbol.Companion.toSymbol
import org.klogic.core.Term
import org.klogic.core.Var.Companion.toVar
import org.klogic.core.run
import org.klogic.core.toRunAnswer
import org.klogic.core.`|||`
import org.klogic.utils.one
import org.klogic.utils.three
import org.klogic.utils.two

// Some tests are taken from faster-minikanren
// (see https://github.com/michaelballantyne/faster-minikanren/blob/master/disequality-tests.scm)
class InequalityTest {
    @Test
    fun testDropOneBranchAndTakeMore() {
        val first = 1.toVar()
        val onlyOneResult = ((first `===` "a".toSymbol()) `|||` (first `===` "b".toSymbol())) `&&&` (first `!==` "a".toSymbol())

        val run = run(2, first, onlyOneResult)

        val expected = listOf("b".toSymbol()).toRunAnswer()
        assertEquals(expected, run)
    }

    @Test
    fun testDropAllBranches() {
        val first = 1.toVar()
        val noResults = (first `===` "a".toSymbol()) `&&&` (first `!==` "a".toSymbol())

        val run = run(2, first, noResults)

        val expected = emptyList<Term>().toRunAnswer()
        assertEquals(expected, run)
    }

    @Test
    fun testOnlyInequalityGoal() {
        val variable = 1.toVar()
        val goal = variable `!==` one

        val run = run(2, variable, goal)

        println(run)
    }

    @Test
    fun testTwoVariables() {
        val first = 1.toVar()
        val second = 2.toVar()
        val noResults = (first `===` second) `&&&` (first `!==` second)

        val run = run(2, first, noResults)

        val expected = emptyList<Term>().toRunAnswer()
        assertEquals(expected, run)
    }

    @Test
    fun testListExample1() {
        val first = 1.toVar()
        val second = 2.toVar()
        val list = 3.toVar()

        val goal = (list `!==` nil) `&&&` (list `!==` first + second)

        val run = run(2, list, goal)

        val expectedInequalityConstraints =
            listOf(InequalityConstraint(list, nil)) + InequalityConstraint(list, first + second)
        assertEquals(expectedInequalityConstraints, run.inequalityConstraints)
    }

    // See corresponding "=/=-22"
    @Test
    fun testListExample2() {
        val x = 1.toVar()
        val y = 2.toVar()
        val q = 3.toVar()

        val goals = arrayOf(
            x + one `!==` two + y,
            x + y `===` q,
        )

        val run = run(2, q, goals)

        val expectedInequalityConstraints = listOf(
            InequalityConstraint(x, two),
            InequalityConstraint(y, one),
        )
        val expectedTerm = x + y
        assertEquals(expectedInequalityConstraints, run.inequalityConstraints)
        assertEquals(expectedTerm, run.terms.single())
    }

    // See corresponding "=/=-24"
    @Test
    fun testListExample3() {
        val x = 1.toVar()
        val y = 2.toVar()
        val q = 3.toVar()

        val goals = arrayOf(
            x + one `!==` two + y,
            x `===` two,
            y `===` "9".toSymbol(),
            x + y `===` q,
        )

        val run = run(2, q, goals)

        val expectedInequalityConstraints = emptyList<InequalityConstraint>()
        val expectedTerm = two + "9".toSymbol()
        assertEquals(expectedInequalityConstraints, run.inequalityConstraints)
        assertEquals(expectedTerm, run.terms.single())
    }

    // See corresponding "=/=-27"
    @Test
    fun testListExample4() {
        val a = 1.toVar()
        val x = 2.toVar()
        val z = 3.toVar()
        val q = 4.toVar()

        val five = "5".toSymbol()

        val goals = arrayOf(
            a `!==` x + one,
            a `===` z + one,
            x `===` five,
            x + z `===` q,
        )

        val run = run(2, q, goals)

        val expectedInequalityConstraints = listOf(InequalityConstraint(z, five))
        val expectedTerm = five + z
        assertEquals(expectedInequalityConstraints, run.inequalityConstraints)
        assertEquals(expectedTerm, run.terms.single())
    }

    // See corresponding "=/=-28"
    @Test
    fun testTruth() {
        val q = 0.toVar()

        val goals = arrayOf(three `!==` "4".toSymbol())

        val run = run(2, q, goals)

        val expectedInequalityConstraints = emptyList<InequalityConstraint>()
        assertEquals(expectedInequalityConstraints, run.inequalityConstraints)
        assertEquals(q, run.terms.single())
    }

    // See corresponding "=/=-29"
    @Test
    fun testAbsurd() {
        val q = 0.toVar()

        val goals = arrayOf(three `!==` three)

        val run = run(2, q, goals)

        val expectedInequalityConstraints = emptyList<InequalityConstraint>()
        assertEquals(expectedInequalityConstraints, run.inequalityConstraints)
        assertTrue(run.terms.isEmpty())
    }

    // See "non watch-var pair implies satisfied"
    @Test
    fun testComplicatedExample() {
        val a = 1.toVar()
        val b = 2.toVar()
        val c = 3.toVar()
        val d = 4.toVar()

        val goals = arrayOf(
            a + c `!==` b + d,
            c `===` (one + two),
            d `===` (one + three),
        )

        val run = run(2, a + b + c + d, goals)

        val expectedInequalityConstraints = emptyList<InequalityConstraint>()
        val expectedTerm = a + b + (one + two) + (one + three)
        assertEquals(expectedInequalityConstraints, run.inequalityConstraints)
        assertEquals(expectedTerm, run.terms.single())
    }
}
