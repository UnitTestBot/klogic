package org.klogic.core

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.klogic.utils.withEmptyContext

class GoalTest {
    private val failingGoal: Goal = { error("Fail") }

    @Test
    fun testCondo2FirstBranch() {
        val run = withEmptyContext {
            run(100, { success condo2 failingGoal })
        }

        assertTrue(run.size == 1)
    }

    @Test
    fun testCondo2SecondBranch() {
        assertThrows<IllegalStateException> {
            withEmptyContext {
                run(100, { failure condo2 failingGoal })
            }
        }
    }
}
