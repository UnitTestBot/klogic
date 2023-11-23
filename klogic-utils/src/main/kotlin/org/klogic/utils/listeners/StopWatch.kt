package org.klogic.utils.listeners

import org.klogic.core.State
import java.util.function.Consumer
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource

@OptIn(ExperimentalTime::class)
class AnswerStopWatch : Consumer<State>, Function1<State, Unit> {
    private val _answerDurations: MutableList<Pair<State, Duration>> = mutableListOf()
    private var timeMark = TimeSource.Monotonic.markNow()

    val answerDurations: List<Pair<State, Duration>> = _answerDurations

    override fun accept(state: State) {
        val updatedTimeMark = TimeSource.Monotonic.markNow()
        val elapsedTime = updatedTimeMark - timeMark
        timeMark = updatedTimeMark

        _answerDurations += state to elapsedTime
    }

    override fun invoke(state: State) {
        accept(state)
    }
}
