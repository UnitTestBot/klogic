package org.klogic.core.unify

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.klogic.core.Substitution
import org.klogic.core.reified
import org.klogic.core.run
import org.klogic.core.`|||`
import org.klogic.terms.Nil.nilLogicList
import org.klogic.terms.Symbol
import org.klogic.terms.plus
import org.klogic.terms.toLogicList
import org.klogic.unify.unifyWithConstraintsVerification
import org.klogic.utils.*

class UnifyTest {
    @Test
    fun testUnifyReflexivity() {
        val unification = unifyWithConstraintsVerification(x + `2`.toLogicList(), `2` + x.toLogicList())!!.substitution

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
        val left = x + listY
        val tail = `2`.toLogicList()
        val right = `1` + tail
        val unification = unifyWithConstraintsVerification(left, right)!!.substitution

        val expectedUnification = Substitution(mapOf(x to `1`, y to tail))
        assertEquals(expectedUnification, unification)
    }

    @Test
    fun testUnunifiable1() {
        val left = x + listY
        val right = nilLogicList<Symbol>()

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
