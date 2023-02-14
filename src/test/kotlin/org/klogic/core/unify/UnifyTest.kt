package org.klogic.core.unify

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.klogic.core.Substitution
import org.klogic.core.reified
import org.klogic.core.run
import org.klogic.core.`|||`
import org.klogic.terms.Cons.Companion.recursiveListOf
import org.klogic.terms.Nil.nilRecursiveList
import org.klogic.terms.Symbol
import org.klogic.terms.plus
import org.klogic.unify.unifyWithConstraintsVerification
import org.klogic.utils.*

class UnifyTest {
    @Test
    fun testUnifyReflexivity() {
        val unification = unifyWithConstraintsVerification(x + `2`, `2` + x)!!.substitution

        val expectedUnification = Substitution(mapOf(x to `2`))
        assertEquals(expectedUnification, unification)
    }

    @Test
    fun testUnifyVarToVar() {
        val unification = unifyWithConstraintsVerification(x, y)!!.substitution

        val expectedUnification = Substitution(mapOf(x to y))
        assertEquals(expectedUnification, unification)
    }

    @Test
    fun testUnifyLists() {
        val left = x + y
        val right = `1` + `2`
        val unification = unifyWithConstraintsVerification(left, right)!!.substitution

        val expectedUnification = Substitution(mapOf(x to `1`, y to `2`))
        assertEquals(expectedUnification, unification)
    }

    @Test
    fun testUnunifiable1() {
        val left = x + y
        val right = nilRecursiveList<Symbol>()

        val unification = unifyWithConstraintsVerification(left, right)

        assertNull(unification)
    }

    @Test
    fun testUnunifiable2() {
        val left = x + y
        val right = recursiveListOf(`1`)

        val unification = unifyWithConstraintsVerification(left, right)

        assertNull(unification)
    }
    
    @Test
    fun testUnion() {
        val goal = (`5` `===` q) `|||` (`6` `===` q)

        val run = run(3, q, goal)

        val expectedAnswer = listOf(`5`, `6`).reified()
        assertEquals(expectedAnswer, run)
    }
}
