package org.klogic.core.unify

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.klogic.core.`&&&`
import org.klogic.core.InequalityConstraint
import org.klogic.core.Var.Companion.createTypedVar
import org.klogic.core.reified
import org.klogic.core.run
import org.klogic.core.`|||`
import org.klogic.terms.Cons.Companion.recursiveListOf
import org.klogic.terms.Nil.nilRecursiveList
import org.klogic.terms.Symbol
import org.klogic.terms.plus
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
    fun testDropAllBranchesReverted() {
        val noResults = (x `!==` `1`) `&&&` (x `===` `1`)

        val run = run(2, x, noResults)

        assertTrue(run.isEmpty())
    }

    @Test
    fun testOnlyInequalityGoal() {
        val goal = x `!==` `1`

        val run = run(2, x, goal)

        val expectedConstraints = setOf(InequalityConstraint.of(x to `1`))
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
        val goal = (listQ `!==` nilRecursiveList()) `&&&` (listQ `!==` x + y)

        val run = run(2, listQ, goal)

        val expectedInequalityConstraints = setOf(
            InequalityConstraint.of(listQ to nilRecursiveList<Symbol>()),
            InequalityConstraint.of(listQ to x + y)
        )
        assertEquals(expectedInequalityConstraints, run.singleReifiedTermConstraints)
    }

    // See corresponding "=/=-22"
    @Test
    fun testListExample2() {
        val goals = arrayOf(
            x + `1` `!==` `2` + y,
            x + y `===` listQ,
        )

        val run = run(2, listQ, goals)

        val expectedInequalityConstraints = setOf(InequalityConstraint.of(x to `2`, y to `1`))
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
            x + y `===` listQ,
        )

        val run = run(2, listQ, goals)

        val expectedInequalityConstraints = emptySet<InequalityConstraint>()
        val expectedTerm = `2` + `9`
        assertEquals(expectedInequalityConstraints, run.singleReifiedTermConstraints)
        assertEquals(expectedTerm, run.singleReifiedTerm)
    }

    // See corresponding "=/=-27"
    @Test
    fun testListExample4() {
        val goals = arrayOf(
            listA `!==` x + `1`,
            listA `===` z + `1`,
            x `===` `5`,
            x + z `===` listQ,
        )

        val run = run(2, listQ, goals)

        val expectedInequalityConstraints = setOf(InequalityConstraint.of(z to `5`))
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
            listA + listC `!==` listB + listD,
            listC `===` (`1` + `2`),
            listD `===` (`1` + `3`),
        )

        val run = run(2, recursiveListOf(listA, listB, listC, listD), goals)

        // The main point of this test is that constraint (a !== b) is not required here
        val expectedTerm = recursiveListOf(listA, listB, `1` + `2`, (`1` + `3`))
        assertTrue(run.singleReifiedTermConstraints.isEmpty())
        assertEquals(expectedTerm, run.singleReifiedTerm)
    }

    /*@Tag("implementation-dependent")
    @Test
    fun testOnlyOneConstraintIsEnoughExample1() {
        val goals = arrayOf(
            listX `===` listY + listZ,
            listY `===` `5` + a,
            listX `!==` (`5` + `7`) + `3`,
        )

        val run = run(2, listX, goals)

        // Looks like the answer is implementation-dependent:
        // 1) There are can be 2 the same terms with different inequality constraints: (z !== 3) OR (a !== 7),
        // 2) or only one the same term with both constraints at the same time: (z !== 3) AND (a !== 7).

        // The current implementation returns the second answer, but in can change in the future.
        val expectedTerm = (`5` + a) + z
        val expectedConstraints = setOf(InequalityConstraint.of(a to `7`, z to `3`))

        assertEquals(expectedConstraints, run.singleReifiedTermConstraints)
        assertEquals(expectedTerm, run.singleReifiedTerm)
    }*/

    @Tag("implementation-dependent")
    @Test
    fun testOnlyOneConstraintIsEnoughExample2() {
        val goal = (x + `1` `!==` `2` + y) `&&&` (x + y `===` listQ)
        val run = run(3, listQ, goal)

        // Looks like the answer is implementation-dependent:
        // 1) There are can be 2 the same terms with different inequality constraints: (x !== 2) OR (y !== 1),
        // 2) or only one the same term with both constraints at the same time: (x !== 2) AND (y !== 1).

        // The current implementation returns the second answer, but in can change in the future.

        val expectedTerm = x + y
        val expectedConstraints = setOf(InequalityConstraint.of(x to `2`, y to `1`))

        assertEquals(expectedConstraints, run.singleReifiedTermConstraints)
        assertEquals(expectedTerm, run.singleReifiedTerm)
    }

    // Example from "Relational Programming in miniKanren: Techniques, Applications, and Implementations", Chapter 8.
    @Test
    fun testByrdExample() {
        val goals = arrayOf(
            `5` + `6` `!==` listQ,
            x + y `===` listQ,
            `5` `===` x,
            `7` `===` y
        )

        val run = run(2, recursiveListOf(listQ, recursiveListOf(x), recursiveListOf(y)), goals)

        val expectedTerm = recursiveListOf((`5` + `7`), recursiveListOf(`5`), recursiveListOf(`7`))
        assertTrue(run.singleReifiedTermConstraints.isEmpty())
        assertEquals(expectedTerm, run.singleReifiedTerm)
    }
}
