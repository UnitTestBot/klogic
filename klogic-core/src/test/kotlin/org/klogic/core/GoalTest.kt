package org.klogic.core

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class GoalTest {
    private fun infiniteGoal(): Goal = success `|||` delay { infiniteGoal() }

    @Test
    fun testCondo2OneAnswer() {
        val goal = { _: Term<*> -> success condo2 infiniteGoal() }

        val run = run(100, goal)

        assertTrue(run.size == 1)
    }

    @Test
    fun testCondo2ManyAnswers() {
        val goal = { _: Term<*> -> infiniteGoal() condo2 success }

        val run = run(100, goal)

        assertTrue(run.size == 100)
    }
}
