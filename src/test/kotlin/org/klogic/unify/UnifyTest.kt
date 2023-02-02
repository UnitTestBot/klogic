package org.klogic.unify

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.klogic.core.Nil.nil
import org.klogic.core.State
import org.klogic.core.Substitution
import org.klogic.core.Var
import org.klogic.core.toTerm

class UnifyTest {
    @Test
    fun testUnifyReflexivity() {
        val variable = 1.toTerm() as Var
        val symbol = "2".toTerm()
        val unification = unify(variable + symbol, symbol + variable)!!.substitution

        val expectedUnification = Substitution(mapOf(variable to symbol))
        assertEquals(expectedUnification, unification)
    }

    @Test
    fun testUnifyVarToVar() {
        val first = 1.toTerm() as Var
        val second = 2.toTerm() as Var
        val unification = unify(first, second)!!.substitution

        val expectedUnification = Substitution(mapOf(first to second))
        assertEquals(expectedUnification, unification)
    }

    @Test
    fun testUnifyVarToList() {
        val firstVar = 1.toTerm() as Var
        val secondVar = 2.toTerm() as Var

        val left = firstVar + secondVar
        val right = secondVar + nil
        val unification = unify(left, right)!!.substitution

        val expectedUnification = Substitution(mapOf(firstVar to secondVar, secondVar to nil))
        assertEquals(expectedUnification, unification)
    }

    @Test
    fun testUnunifiable() {
        val firstVar = 1.toTerm() as Var
        val secondVar = 2.toTerm() as Var

        val left = firstVar + secondVar
        val right = nil

        val unification = unify(left, right)

        val failedUnification: State? = null
        assertEquals(failedUnification, unification)
    }
}
