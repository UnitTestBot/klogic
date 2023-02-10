package org.klogic.core.unify

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.klogic.core.Nil.nil
import org.klogic.core.Substitution
import org.klogic.core.run
import org.klogic.core.reified
import org.klogic.core.`|||`
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
    fun testUnifyVarToList() {
        val left = x + y
        val right = y + nil
        val unification = unifyWithConstraintsVerification(left, right)!!.substitution

        val expectedUnification = Substitution(mapOf(x to y, y to nil))
        assertEquals(expectedUnification, unification)
    }

    @Test
    fun testUnunifiable() {
        val left = x + y
        val right = nil

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
