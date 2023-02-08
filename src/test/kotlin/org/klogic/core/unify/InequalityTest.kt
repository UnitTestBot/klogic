package org.klogic.core.unify

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.klogic.core.`&&&`
import org.klogic.core.InequalityConstraint
import org.klogic.core.Nil.nil
import org.klogic.core.Var.Companion.toVar
import org.klogic.core.reified
import org.klogic.core.run
import org.klogic.core.`|||`
import org.klogic.utils.*

// Some tests are taken from faster-minikanren
// (see https://github.com/michaelballantyne/faster-minikanren/blob/master/disequality-tests.scm)
@Suppress("RemoveRedundantBackticks", "LocalVariableName")
class InequalityTest {
    @Test
    fun testDropOneBranchAndTakeMore() {
        val onlyOneResult = ((x `===` `1`) `|||` (x `===` `2`)) `&&&` (x `!==` `1`)

        val run = run(2, x, onlyOneResult)

        val expected = listOf(`2`).reified()
        assertEquals(expected, run)
    }

    @Test
    fun testDropAllBranches() {
        val noResults = (x `===` `1`) `&&&` (x `!==` `1`)

        val run = run(2, x, noResults)

        assertTrue(run.isEmpty())
    }

    @Test
    fun testOnlyInequalityGoal() {
        val goal = x `!==` `1`

        val run = run(2, x, goal)

        val expectedConstraints = setOf(InequalityConstraint(x, `1`))
        assertEquals(x, run.singleReifiedTerm)
        assertEquals(expectedConstraints, run.singleReifiedTermConstraints)
    }

    @Test
    fun testTwoVariables() {
        val noResults = (x `===` y) `&&&` (x `!==` y)

        val run = run(2, x, noResults)

        assertTrue(run.isEmpty())
    }

    @Test
    fun testListExample1() {
        val goal = (q `!==` nil) `&&&` (q `!==` x + y)

        val run = run(2, q, goal)

        val expectedInequalityConstraints = setOf(
            InequalityConstraint(q, nil),
            InequalityConstraint(q, x + y)
        )
        assertEquals(expectedInequalityConstraints, run.singleReifiedTermConstraints)
    }

    // See corresponding "=/=-22"
    @Test
    fun testListExample2() {
        val goals = arrayOf(
            x + `1` `!==` `2` + y,
            x + y `===` q,
        )

        val run = run(2, q, goals)

        val expectedInequalityConstraints = setOf(InequalityConstraint(x to `2`, y to `1`))
        val expectedTerm = x + y
        assertEquals(expectedInequalityConstraints, run.singleReifiedTermConstraints)
        assertEquals(expectedTerm, run.singleReifiedTerm)
    }

    // See corresponding "=/=-24"
    @Test
    fun testListExample3() {
        val goals = arrayOf(
            x + `1` `!==` `2` + y,
            x `===` `2`,
            y `===` `9`,
            x + y `===` q,
        )

        val run = run(2, q, goals)

        val expectedInequalityConstraints = emptySet<InequalityConstraint>()
        val expectedTerm = `2` + `9`
        assertEquals(expectedInequalityConstraints, run.singleReifiedTermConstraints)
        assertEquals(expectedTerm, run.singleReifiedTerm)
    }

    // See corresponding "=/=-27"
    @Test
    fun testListExample4() {
        val goals = arrayOf(
            a `!==` x + `1`,
            a `===` z + `1`,
            x `===` `5`,
            x + z `===` q,
        )

        val run = run(2, q, goals)

        val expectedInequalityConstraints = setOf(InequalityConstraint(z, `5`))
        val expectedTerm = `5` + z
        assertEquals(expectedInequalityConstraints, run.singleReifiedTermConstraints)
        assertEquals(expectedTerm, run.singleReifiedTerm)
    }

    // See corresponding "=/=-28"
    @Test
    fun testTruth() {
        val goals = arrayOf(`3` `!==` `4`)

        val run = run(2, q, goals)

        assertTrue(run.singleReifiedTermConstraints.isEmpty())
        assertEquals(q, run.singleReifiedTerm)
    }

    // See corresponding "=/=-29"
    @Test
    fun testAbsurd() {
        val goals = arrayOf(`3` `!==` `3`)

        val run = run(2, q, goals)

        assertTrue(run.isEmpty())
    }

    // See "non watch-var pair implies satisfied"
    @Test
    fun testNoConstraintsRequired() {
        val goals = arrayOf(
            a + c `!==` b + d,
            c `===` (`1` + `2`),
            d `===` (`1` + `3`),
        )

        val run = run(2, a + b + c + d, goals)

        // The main point of this test is that constraint (a !== b) is not required here
        val expectedTerm = a + b + (`1` + `2`) + (`1` + `3`)
        assertTrue(run.singleReifiedTermConstraints.isEmpty())
        assertEquals(expectedTerm, run.singleReifiedTerm)
    }

    @Tag("implementation-dependent")
    @Test
    fun testOnlyOneConstraintIsEnoughExample1() {
        val goals = arrayOf(
            x `===` y + z,
            y `===` `5` + a,
            x `!==` (`5` + `7`) + `3`,
        )

        val run = run(2, x, goals)

        // Looks like the answer is implementation-dependent:
        // 1) There are can be 2 the same terms with different inequality constraints: (z !== 3) OR (a !== 7),
        // 2) or only one the same term with both constraints at the same time: (z !== 3) AND (a !== 7).

        // The current implementation returns the second answer, but in can change in the future.
        val expectedTerm = (`5` + a) + z
        val expectedConstraints = setOf(InequalityConstraint(a to `7`, z to `3`))

        assertEquals(expectedConstraints, run.singleReifiedTermConstraints)
        assertEquals(expectedTerm, run.singleReifiedTerm)
    }

    @Tag("implementation-dependent")
    @Test
    fun testOnlyOneConstraintIsEnoughExample2() {
        val q = 0.toVar()
        val x = 1.toVar()
        val y = 2.toVar()

        val goal = (x + `1` `!==` `2` + y) `&&&` (x + y `===` q)
        val run = run(3, q, goal)

        // Looks like the answer is implementation-dependent:
        // 1) There are can be 2 the same terms with different inequality constraints: (x !== 2) OR (y !== 1),
        // 2) or only one the same term with both constraints at the same time: (x !== 2) AND (y !== 1).

        // The current implementation returns the second answer, but in can change in the future.

        val expectedTerm = x + y
        val expectedConstraints = setOf(InequalityConstraint(x to `2`, y to `1`))

        assertEquals(expectedConstraints, run.singleReifiedTermConstraints)
        assertEquals(expectedTerm, run.singleReifiedTerm)
    }

    // Example from "Relational Programming in miniKanren: Techniques, Applications, and Implementations", Chapter 8.
    @Test
    fun testByrdExample() {
        val goals = arrayOf(
            `5` + `6` `!==` q,
            x + y `===` q,
            `5` `===` x,
            `7` `===` y
        )

        val run = run(2, q + x + y, goals)

        val expectedTerm = ((`5` + `7`) + `5` + `7`)
        assertTrue(run.singleReifiedTermConstraints.isEmpty())
        assertEquals(expectedTerm, run.singleReifiedTerm)
    }
}
