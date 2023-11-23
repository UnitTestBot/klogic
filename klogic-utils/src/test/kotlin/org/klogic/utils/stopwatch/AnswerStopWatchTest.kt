package org.klogic.utils.stopwatch

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.klogic.core.CustomTerm
import org.klogic.core.State
import org.klogic.core.Var.Companion.createTypedVar
import org.klogic.core.conde
import org.klogic.utils.listeners.AnswerStopWatch
import org.klogic.utils.withEmptyContext
import java.util.function.Consumer
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource

private const val sleepDurationMs: Long = 500

class AnswerStopWatchTest {
    private object SleepingTerm : CustomTerm<SleepingTerm> {
        override val subtreesToUnify: Array<*>
            get() {
                Thread.sleep(sleepDurationMs)
                return emptyArray<Unit>()
            }

        override fun constructFromSubtrees(subtrees: Iterable<*>): CustomTerm<SleepingTerm> {
            return this
        }
    }

    @Test
    fun testStopWatchWithSleep() {
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
            assertTrue(durationMs >= sleepDurationMs)
        }
    }

    @OptIn(ExperimentalTime::class)
    private class PrintingStopWatch : Consumer<State>, Function1<State, Unit> {
        private var timeMark = TimeSource.Monotonic.markNow()

        override fun accept(state: State) {
            val updatedTimeMark = TimeSource.Monotonic.markNow()
            val elapsedTime = updatedTimeMark - timeMark
            timeMark = updatedTimeMark

            println("Got a new answer $state, elapsed time: ${elapsedTime.inWholeMilliseconds} ms")
        }

        override fun invoke(state: State) {
            accept(state)
        }
    }

    @Test
    fun testAnswerOneByOne() {
        withEmptyContext {
            val printingStopWatch = PrintingStopWatch()

            val size = 10
            val term = (-1).createTypedVar<SleepingTerm>()
            val goals = Array(size) { term `===` SleepingTerm }
            val goalWithFewAnswers = conde(term `===` SleepingTerm, *goals)

            val run = run(size + 1, term, goalWithFewAnswers, elementConsumer = printingStopWatch)
            assertEquals(size + 1, run.size)
        }
    }
}
