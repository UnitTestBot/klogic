package org.klogic.unify

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
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
            setOf(InequalityConstraint(list, nil)) + InequalityConstraint(list, first + second)
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

        val expectedInequalityConstraints = setOf(
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

        val expectedInequalityConstraints = emptySet<InequalityConstraint>()
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

        val expectedInequalityConstraints = setOf(InequalityConstraint(z, five))
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

        val expectedInequalityConstraints = emptySet<InequalityConstraint>()
        assertEquals(expectedInequalityConstraints, run.inequalityConstraints)
        assertEquals(q, run.terms.single())
    }

    // See corresponding "=/=-29"
    @Test
    fun testAbsurd() {
        val q = 0.toVar()

        val goals = arrayOf(three `!==` three)

        val run = run(2, q, goals)

        val expectedInequalityConstraints = emptySet<InequalityConstraint>()
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

        val expectedInequalityConstraints = emptySet<InequalityConstraint>()
        val expectedTerm = a + b + (one + two) + (one + three)
        assertEquals(expectedInequalityConstraints, run.inequalityConstraints)
        assertEquals(expectedTerm, run.terms.single())
    }

    @Tag("implementation-dependent")
    @Test
    fun testOnlyOneConstraintIsEnoughExample1() {
        val a = 1.toVar()
        val x = 2.toVar()
        val y = 3.toVar()
        val z = 4.toVar()

        val five = "5".toSymbol()
        val seven = "7".toSymbol()
        val goals = arrayOf(
            x `===` y + z,
            y `===` five + a,
            x `!==` (five + seven) + three,
        )

        val run = run(2, x, goals)

        // Looks like the answer is implementation-dependent:
        // 1) There are can be 2 the same terms with different inequality constraints: (z !== 3) OR (a !== 7),
        // 2) or only one the same term with both constraints at the same time: (z !== 3) AND (a !== 7).

        // The current implementation returns the second answer, but in can change in the future.
        val expectedTerm = (five + a) + z
        val expectedConstraints = setOf(
            InequalityConstraint(a, seven),
            InequalityConstraint(z, three)
        )

        assertEquals(expectedTerm, run.terms.single())
        assertEquals(expectedConstraints, run.inequalityConstraints)
    }

    @Tag("implementation-dependent")
    @Test
    fun testOnlyOneConstraintIsEnoughExample2() {
        val q = 0.toVar()
        val x = 1.toVar()
        val y = 2.toVar()

        val goal = (x + one `!==` two + y) `&&&` (x + y `===` q)
        val run = run(3, q, goal)

        // Looks like the answer is implementation-dependent:
        // 1) There are can be 2 the same terms with different inequality constraints: (x !== 2) OR (y !== 1),
        // 2) or only one the same term with both constraints at the same time: (x !== 2) AND (y !== 1).

        // The current implementation returns the second answer, but in can change in the future.

        val expectedTerm = x + y
        val expectedConstraints = setOf(
            InequalityConstraint(x, two),
            InequalityConstraint(y, one)
        )

        assertEquals(expectedTerm, run.terms.single())
        assertEquals(expectedConstraints, run.inequalityConstraints)
    }
}
