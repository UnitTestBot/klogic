package org.klogic.core.unify

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.klogic.core.`&&&`
import org.klogic.core.InequalityConstraint
import org.klogic.core.InequalityConstraint.SingleInequalityConstraint
import org.klogic.core.Var.Companion.createTypedVar
import org.klogic.core.reified
import org.klogic.core.reify
import org.klogic.core.run
import org.klogic.core.unreifiedRun
import org.klogic.core.`|||`
import org.klogic.terms.Cons.Companion.logicListOf
import org.klogic.terms.LogicList
import org.klogic.terms.Nil.nilLogicList
import org.klogic.terms.Symbol
import org.klogic.terms.plus
import org.klogic.terms.toLogicList
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

        val expectedConstraints = setOf(InequalityConstraint.of(x, `1`))
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
        val tail = y.toLogicList()
        val goal = (listQ `!==` nilLogicList()) `&&&` (listQ `!==` x + tail)

        val run = run(2, listQ, goal)

        val expectedInequalityConstraints = setOf(
            InequalityConstraint.of(listQ, nilLogicList()),
            InequalityConstraint.of(listQ, x + tail)
        )
        assertEquals(expectedInequalityConstraints, run.singleReifiedTermConstraints)
    }

    // See corresponding "=/=-22"
    @Test
    fun testListExample2() {
        val tail = y.toLogicList()
        val goals = arrayOf(
            x + `1`.toLogicList() `!==` `2` + tail,
            x + tail `===` listQ,
        )

        val run = run(2, listQ, goals)

        val singleInequalityConstraints = listOf(
            SingleInequalityConstraint(x, `2`),
            SingleInequalityConstraint(y, `1`),
        )
        val expectedInequalityConstraints = setOf(InequalityConstraint(singleInequalityConstraints))
        val expectedTerm = x + tail
        assertEquals(expectedInequalityConstraints, run.singleReifiedTermConstraints)
        assertEquals(expectedTerm, run.singleReifiedTerm)
    }

    // See corresponding "=/=-24"
    @Test
    fun testListExample3() {
        val tail = y.toLogicList()
        val goals = arrayOf(
            x + `1`.toLogicList() `!==` `2` + tail,
            x `===` `2`,
            y `===` `9`,
            x + tail `===` listQ,
        )

        val run = run(2, listQ, goals)

        val expectedInequalityConstraints = emptySet<InequalityConstraint>()
        val expectedTerm = logicListOf(`2`, `9`)
        assertEquals(expectedInequalityConstraints, run.singleReifiedTermConstraints)
        assertEquals(expectedTerm, run.singleReifiedTerm)
    }

    // See corresponding "=/=-27"
    @Test
    fun testListExample4() {
        val goals = arrayOf(
            listA `!==` x + `1`.toLogicList(),
            listA `===` z + `1`.toLogicList(),
            x `===` `5`,
            x + z.toLogicList() `===` listQ,
        )

        val run = run(2, listQ, goals)

        val expectedInequalityConstraints = setOf(InequalityConstraint.of(z, `5`))
        val expectedTerm = logicListOf(`5`, z)
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
            a + listC `!==` b + listD,
            listC `===` logicListOf(`1`, `2`),
            listD `===` logicListOf(`1`, `3`),
        )

        val unreifiedRun = unreifiedRun(2, goals)
        val reifiedTerms = listOf(a, b, listC, listD).map { unreifiedRun.reify(it).single() }

        // The main point of this test is that constraint (a !== b) is not required here
        val expectedTerms = listOf(a, b, logicListOf(`1`, `2`), logicListOf(`1`, `3`))
        assertTrue(reifiedTerms.all { it.constraints.isEmpty() })
        assertEquals(expectedTerms, reifiedTerms.map { it.term })
    }

    @Tag("implementation-dependent")
    @Test
    fun testOnlyOneConstraintIsEnoughExample1() {
        val listOfListsX = (-1).createTypedVar<LogicList<LogicList<Symbol>>>()

        val tail = z.toLogicList().toLogicList()
        val goals = arrayOf(
            listOfListsX `===` listY + tail,
            listY `===` logicListOf(`5`, a),
            listOfListsX `!==` logicListOf(`5`, `7`) + `3`.toLogicList().toLogicList(),
        )

        val run = run(2, listOfListsX, goals)

        // Looks like the answer is implementation-dependent:
        // 1) There are can be 2 the same terms with different inequality constraints: (z !== 3) OR (a !== 7),
        // 2) or only one the same term with both constraints at the same time: (z !== 3) AND (a !== 7).

        // The current implementation returns the second answer, but in can change in the future.
        val expectedTerm = logicListOf(`5`, a) + tail

        val singleInequalityConstraints = listOf(
            SingleInequalityConstraint(a, `7`),
            SingleInequalityConstraint(z, `3`),
        )
        val expectedConstraints = setOf(InequalityConstraint(singleInequalityConstraints))

        assertEquals(expectedConstraints, run.singleReifiedTermConstraints)
        assertEquals(expectedTerm, run.singleReifiedTerm)
    }

    @Tag("implementation-dependent")
    @Test
    fun testOnlyOneConstraintIsEnoughExample2() {
        val goal = (logicListOf(x, `1`) `!==` `2` + y.toLogicList()) `&&&` (logicListOf(x, y) `===` listQ)
        val run = run(3, listQ, goal)

        // Looks like the answer is implementation-dependent:
        // 1) There are can be 2 the same terms with different inequality constraints: (x !== 2) OR (y !== 1),
        // 2) or only one the same term with both constraints at the same time: (x !== 2) AND (y !== 1).

        // The current implementation returns the second answer, but in can change in the future.

        val expectedTerm = logicListOf(x, y)

        val singleInequalityConstraints = listOf(
            SingleInequalityConstraint(x, `2`),
            SingleInequalityConstraint(y, `1`),
        )
        val expectedConstraints = setOf(InequalityConstraint(singleInequalityConstraints))

        assertEquals(expectedConstraints, run.singleReifiedTermConstraints)
        assertEquals(expectedTerm, run.singleReifiedTerm)
    }

    // Example from "Relational Programming in miniKanren: Techniques, Applications, and Implementations", Chapter 8.
    @Test
    fun testByrdExample() {
        val goals = arrayOf(
            logicListOf(`5`, `6`) `!==` listQ,
            logicListOf(x, y) `===` listQ,
            `5` `===` x,
            `7` `===` y
        )

        val run = run(2, logicListOf(listQ, logicListOf(x), logicListOf(y)), goals)

        val expectedTerm = logicListOf((logicListOf(`5`, `7`)), logicListOf(`5`), logicListOf(`7`))
        assertTrue(run.singleReifiedTermConstraints.isEmpty())
        assertEquals(expectedTerm, run.singleReifiedTerm)
    }
}
