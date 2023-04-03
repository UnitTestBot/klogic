package org.klogic.core

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.klogic.core.Var.Companion.createTypedVar
import org.klogic.utils.terms.PeanoLogicNumber
import org.klogic.utils.terms.PeanoLogicNumber.Companion.succ
import org.klogic.utils.terms.ZeroNaturalNumber.Z
import org.klogic.utils.terms.toPeanoLogicNumber

class ReifyTest {
    private val debugValues: MutableMap<String, MutableList<ReifiedTerm<*>>> = mutableMapOf()

    @AfterEach
    fun clearDebug() {
        debugValues.clear()
    }

    @Test
    fun testDebugVar() {
        val left = (-1).createTypedVar<PeanoLogicNumber>()
        val right = 10.toPeanoLogicNumber()
        val result = 13.toPeanoLogicNumber()

        val run = run(10, left, plus(left, right, result))

        assertEquals(3.toPeanoLogicNumber(), run.single().term)

        debugValues["x"]!!.zip(debugValues["z"]!!).forEach {
            println("x: ${it.first.term}, z: ${it.second.term}")
        }

        val expectedDebugValues = (13 downTo 1).map { it.toPeanoLogicNumber() }
        assertEquals(expectedDebugValues, debugValues["z"]!!.map { it.term })
    }

    private fun plus(x: Term<PeanoLogicNumber>, y: Term<PeanoLogicNumber>, z: Term<PeanoLogicNumber>): Goal = conde(
        (x `===` Z) and (z `===` y),
        freshTypedVars<PeanoLogicNumber, PeanoLogicNumber> { a, b ->
            (x `===` succ(a)) and (z `===` succ(b)) and debugVar(
                x,
                callBack = { reifiedX ->
                    debugValues.getOrPut("x") { mutableListOf() } += reifiedX

                    debugVar(
                        z,
                        callBack = { reifiedZ ->
                            debugValues.getOrPut("z") { mutableListOf() } += reifiedZ

                            plus(a, y, b)
                        }
                    )
                }
            )
        }
    )
}
