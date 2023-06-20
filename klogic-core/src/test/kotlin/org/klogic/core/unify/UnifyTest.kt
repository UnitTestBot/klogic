package org.klogic.core.unify

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.klogic.core.*
import org.klogic.utils.terms.Nil.nilLogicList
import org.klogic.utils.terms.Symbol
import org.klogic.utils.terms.plus
import org.klogic.utils.terms.toLogicList
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
        withEmptyContext {
            val goal = (`5` `===` q) `|||` (`6` `===` q)

            val run = run(3, q, goal)

            val expectedAnswer = listOf(`5`, `6`).reified()
            assertEquals(expectedAnswer, run)
        }
    }
}
