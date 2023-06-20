@file:Suppress("TestFunctionName", "NonAsciiCharacters")

package org.klogic.utils.terms

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.klogic.core.*
import org.klogic.core.Var.Companion.createTypedVar
import org.klogic.utils.singleReifiedTerm
import org.klogic.utils.terms.LogicFalsᴼ.falsᴼ
import org.klogic.utils.terms.LogicTruᴼ.truᴼ
import org.klogic.utils.withEmptyContext

class LogicBoolTest {
    @Test
    @DisplayName("Forward !false = true")
    fun testForwardNotᴼ1() {
        withEmptyContext {
            val q = (-1).createTypedVar<LogicBool>()

            val goal = notᴼ(truᴼ, q)

            val run = run(2, q, goal)

            val expectedTerm = falsᴼ

            assertEquals(expectedTerm, run.singleReifiedTerm)
        }
    }

    @Test
    @DisplayName("Forward !true = false")
    fun testForwardNotᴼ2() {
        withEmptyContext {
            val q = (-1).createTypedVar<LogicBool>()

            val goal = notᴼ(falsᴼ, q)

            val run = run(2, q, goal)

            val expectedTerm = truᴼ

            assertEquals(expectedTerm, run.singleReifiedTerm)
        }
    }

    @Test
    @DisplayName("Backward !false = true")
    fun testBackwardNotᴼ1() {
        withEmptyContext {
            val q = (-1).createTypedVar<LogicBool>()

            val goal = notᴼ(q, truᴼ)

            val run = run(2, q, goal)

            val expectedTerm = falsᴼ

            assertEquals(expectedTerm, run.singleReifiedTerm)
        }
    }

    @Test
    @DisplayName("Backward !true = false")
    fun testBackwardNotᴼ2() {
        withEmptyContext {
            val q = (-1).createTypedVar<LogicBool>()

            val goal = notᴼ(q, falsᴼ)

            val run = run(2, q, goal)

            val expectedTerm = truᴼ

            assertEquals(expectedTerm, run.singleReifiedTerm)
        }
    }
    
    @Test
    fun testOrᴼ() {
        withEmptyContext {
            val x = (-1).createTypedVar<LogicBool>()
            val y = (-2).createTypedVar<LogicBool>()
            val z = (-3).createTypedVar<LogicBool>()

            val goal = orᴼ(x, y, z)

            val unreifiedRun = unreifiedRun(9, goal)

            val reifiedTerms = unreifiedRun.reify(x).zip(unreifiedRun.reify(y)).zip(unreifiedRun.reify(z)).map {
                Triple(it.first.first, it.first.second, it.second)
            }

            val expectedTerms = listOf(
                Triple(falsᴼ, falsᴼ, falsᴼ),
                Triple(falsᴼ, truᴼ, truᴼ),
                Triple(truᴼ, falsᴼ, truᴼ),
                Triple(truᴼ, truᴼ, truᴼ),
            ).map { Triple(it.first.reified(), it.second.reified(), it.third.reified()) }

            assertEquals(expectedTerms, reifiedTerms)
        }
    }

    @Test
    fun testAndᴼ() {
        withEmptyContext {
            val x = (-1).createTypedVar<LogicBool>()
            val y = (-2).createTypedVar<LogicBool>()
            val z = (-3).createTypedVar<LogicBool>()

            val goal = andᴼ(x, y, z)

            val unreifiedRun = unreifiedRun(9, goal)

            val reifiedTerms = unreifiedRun.reify(x).zip(unreifiedRun.reify(y)).zip(unreifiedRun.reify(z)).map {
                Triple(it.first.first, it.first.second, it.second)
            }

            val expectedTerms = listOf(
                Triple(falsᴼ, falsᴼ, falsᴼ),
                Triple(falsᴼ, truᴼ, falsᴼ),
                Triple(truᴼ, falsᴼ, falsᴼ),
                Triple(truᴼ, truᴼ, truᴼ),
            ).map { Triple(it.first.reified(), it.second.reified(), it.third.reified()) }

            assertEquals(expectedTerms, reifiedTerms)
        }
    }
}
