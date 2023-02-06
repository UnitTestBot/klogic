package org.klogic.unify

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.klogic.core.Nil.nil
import org.klogic.core.State
import org.klogic.core.Substitution
import org.klogic.core.Symbol.Companion.toSymbol
import org.klogic.core.run
import org.klogic.core.Var.Companion.toVar
import org.klogic.core.toRunAnswer
import org.klogic.core.`|||`
import org.klogic.utils.two

class UnifyTest {
    @Test
    fun testUnifyReflexivity() {
        val variable = 1.toVar()
        val symbol = two
        val unification = unify(variable + symbol, symbol + variable)!!.newState.substitution

        val expectedUnification = Substitution(mapOf(variable to symbol))
        assertEquals(expectedUnification, unification)
    }

    @Test
    fun testUnifyVarToVar() {
        val first = 1.toVar()
        val second = 2.toVar()
        val unification = unify(first, second)!!.newState.substitution

        val expectedUnification = Substitution(mapOf(first to second))
        assertEquals(expectedUnification, unification)
    }

    @Test
    fun testUnifyVarToList() {
        val firstVar = 1.toVar()
        val secondVar = 2.toVar()

        val left = firstVar + secondVar
        val right = secondVar + nil
        val unification = unify(left, right)!!.newState.substitution

        val expectedUnification = Substitution(mapOf(firstVar to secondVar, secondVar to nil))
        assertEquals(expectedUnification, unification)
    }

    @Test
    fun testUnunifiable() {
        val firstVar = 1.toVar()
        val secondVar = 2.toVar()

        val left = firstVar + secondVar
        val right = nil

        val unification = unify(left, right)

        val failedUnification: State? = null
        assertEquals(failedUnification, unification?.newState)
    }
    
    @Test
    fun testUnion() {
        val q = 0.toVar()

        val five = "5".toSymbol()
        val six = "6".toSymbol()
        val goal = (five `===` q) `|||` (six `===` q)

        val run = run(3, q, goal)

        val expectedAnswer = listOf(five, six).toRunAnswer()
        assertEquals(expectedAnswer, run)
    }
}
