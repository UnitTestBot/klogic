package org.klogic.utils.stopwatch

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.klogic.core.*
import org.klogic.core.Var.Companion.createTypedVar
import org.klogic.utils.listeners.AnswerStopWatch
import org.klogic.utils.withEmptyContext

class AnswerStopWatchTest {
    private object SleepingTerm : CustomTerm<SleepingTerm> {
        override val subtreesToUnify: Array<*>
            get() {
                Thread.sleep(100)
                return emptyArray<Unit>()
            }

        override fun constructFromSubtrees(subtrees: Iterable<*>): CustomTerm<SleepingTerm> {
            return this
        }
    }

    @Test
    fun testStopWatchOnPermutations() {
        withEmptyContext {
            val answerStopWatch = AnswerStopWatch()

            val x = SleepingTerm
            val y = (-1).createTypedVar<SleepingTerm>()
            val goal = x `===` y

            val run = run(1, y, goal, elementConsumer = answerStopWatch)
            assertEquals(run.single().term, SleepingTerm)

            val timings = answerStopWatch.answerDurations
            assertEquals(1, timings.size)

            val durationMs = timings.single().second.inWholeMilliseconds
            println("Calculating answer took $durationMs ms")
            assertTrue(durationMs >= 100)
        }
    }
}
