package org.klogic.utils.terms

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.klogic.core.RelationalContext
import org.klogic.core.Var.Companion.createTypedVar
import org.klogic.core.run
import org.klogic.core.useWith
import org.klogic.utils.listeners.UnificationCounter
import org.klogic.utils.withEmptyContext
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

@OptIn(ExperimentalTime::class)
@Tag("huge")
class HugeTests {
    @Test
    fun testAllPermutations() {
        val unificationCounter = UnificationCounter()

        withEmptyContext {
            unificationListener = unificationCounter

            val size = 9

            val unsortedList = (-1).createTypedVar<LogicList<PeanoLogicNumber>>()

            val sortedList = (1..size).map { it.toPeanoLogicNumber() }.toLogicList()

            val goal = sortᴼ(unsortedList, sortedList)

            val count = (1..size).reduce(Int::times)
            val run = measureTimedValue { run(count + 1, unsortedList, goal) }

            assertEquals(count, run.value.size)

            println("Generating all permutations of size $size took: ${run.duration.inWholeMilliseconds} ms")
            println("Unifications: ${unificationCounter.counter}")
        }
    }
}
