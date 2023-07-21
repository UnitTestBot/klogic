package org.klogic.experiments

import org.junit.jupiter.api.Test
import org.klogic.core.State
import org.klogic.core.Term
import org.klogic.core.UnificationListener
import org.klogic.utils.experiments.OlegLogicNumberAsList
import org.klogic.utils.experiments.mulᴼ
import org.klogic.utils.experiments.toOlegLogicNumberAsList
import org.klogic.utils.listeners.UnificationCounter
import org.klogic.utils.singleReifiedTerm
import org.klogic.utils.withEmptyContext

class MulTestWithOlegNumberAsList {
    @Test
    fun testMul1x1() {
        val unificationCounter = UnificationCounter()

        withEmptyContext {
            addUnificationListener(unificationCounter)

            val first = 1u.toOlegLogicNumberAsList()
            val second = 1u.toOlegLogicNumberAsList()
            val goal = { q: Term<OlegLogicNumberAsList> -> mulᴼ(first, second, q) }
            val answer = run(1, goal)

            println(answer.singleReifiedTerm.asReified())
            println("Unifications: ${unificationCounter.counter}")
        }
    }

    @Test
    fun testMul2x3() {
        val unificationCounter = UnificationCounter()

        withEmptyContext {
            addUnificationListener(unificationCounter)

            @Suppress("UNUSED_ANONYMOUS_PARAMETER")
            val printingUnifications = UnificationListener { firstTerm, secondTerm, stateBefore, stateAfter ->
                println("${firstTerm.walk(stateBefore.substitution)} ${secondTerm.walk(stateBefore.substitution)}, ")
            }
            addUnificationListener(printingUnifications)

            val first = 2u.toOlegLogicNumberAsList()
            val second = 3u.toOlegLogicNumberAsList()
            val goal = { q: Term<OlegLogicNumberAsList> -> mulᴼ(first, second, q) }
            val answer = run(1, goal)

            println(answer.singleReifiedTerm.asReified())
            println("Unifications: ${unificationCounter.counter}")
        }
    }

    @Test
    fun testMul3x3() {
        val unificationCounter = UnificationCounter()

        withEmptyContext {
            addUnificationListener(unificationCounter)

            @Suppress("UNUSED_ANONYMOUS_PARAMETER")
            val printingUnifications = UnificationListener { firstTerm, secondTerm, stateBefore, stateAfter ->
                println("${firstTerm/*.walk(stateBefore.substitution)*/} ${secondTerm/*.walk(stateBefore.substitution)*/}, ")
            }
            addUnificationListener(printingUnifications)

            val first = 3u.toOlegLogicNumberAsList()
            val second = 3u.toOlegLogicNumberAsList()
            val goal = { q: Term<OlegLogicNumberAsList> -> mulᴼ(first, second, q) }
            val answer = run(1, goal)

            println(answer.singleReifiedTerm.asReified())
            println("Unifications: ${unificationCounter.counter}")
        }
    }

    @Test
    fun testMul6x1() {
        val unificationCounter = UnificationCounter()

        withEmptyContext {
            addUnificationListener(unificationCounter)

            @Suppress("UNUSED_ANONYMOUS_PARAMETER")
            val printingUnifications = UnificationListener { firstTerm, secondTerm, stateBefore, stateAfter ->
                println("${firstTerm.walk(stateBefore.substitution)} ${secondTerm.walk(stateBefore.substitution)}, ")
            }
            addUnificationListener(printingUnifications)

            val first = 6u.toOlegLogicNumberAsList()
            val second = 4u.toOlegLogicNumberAsList()
            val goal = { q: Term<OlegLogicNumberAsList> -> mulᴼ(first, second, q) }
            val answer = run(1, goal)

            println(answer.singleReifiedTerm.asReified())
            println("Unifications: ${unificationCounter.counter}")
        }
    }

    @Test
    fun testMul4x4() {
        val unificationCounter = UnificationCounter()

        withEmptyContext {
            addUnificationListener(unificationCounter)

            val first = 4u.toOlegLogicNumberAsList()
            val second = 4u.toOlegLogicNumberAsList()
            val goal = { q: Term<OlegLogicNumberAsList> -> mulᴼ(first, second, q) }
            val answer = run(1, goal)

            println(answer.singleReifiedTerm.asReified())
            println("Unifications: ${unificationCounter.counter}")
        }
    }

    @Test
    fun testMul5x3() {
        val unificationCounter = UnificationCounter()

        withEmptyContext {
            addUnificationListener(unificationCounter)

            @Suppress("UNUSED_ANONYMOUS_PARAMETER")
            val printingUnifications = UnificationListener { firstTerm, secondTerm, stateBefore, stateAfter ->
                println("${firstTerm/*.walk(stateBefore.substitution)*/} ${secondTerm/*.walk(stateBefore.substitution)*/}, ")
            }
            addUnificationListener(printingUnifications)

            val first = 5u.toOlegLogicNumberAsList()
            val second = 3u.toOlegLogicNumberAsList()
            val goal = { q: Term<OlegLogicNumberAsList> -> mulᴼ(first, second, q) }
            val answer = run(1, goal)

            println(answer.singleReifiedTerm.asReified())
            println("Unifications: ${unificationCounter.counter}")
        }
    }

    @Test
    fun testMul5x5() {
        val unificationCounter = UnificationCounter()

        withEmptyContext {
            addUnificationListener(unificationCounter)

            val five = 5u.toOlegLogicNumberAsList()
            val goal = { q: Term<OlegLogicNumberAsList> -> mulᴼ(five, five, q) }
            run(1, goal)

            println("Unifications: ${unificationCounter.counter}")
        }
    }

    @Test
    fun testMul5x6() {
        val unificationCounter = UnificationCounter()

        withEmptyContext {
            addUnificationListener(unificationCounter)

            val first = 5u.toOlegLogicNumberAsList()
            val second = 6u.toOlegLogicNumberAsList()
            val goal = { q: Term<OlegLogicNumberAsList> -> mulᴼ(first, second, q) }
            val answer = run(1, goal)

            println(answer.singleReifiedTerm.asReified())
            println("Unifications: ${unificationCounter.counter}")
        }
    }

    @Test
    fun testMul127x127() {
        val unificationCounter = UnificationCounter()

        withEmptyContext {
            addUnificationListener(unificationCounter)

            val `127` = 127u.toOlegLogicNumberAsList()
            val goal = { q: Term<OlegLogicNumberAsList> -> mulᴼ(`127`, `127`, q) }
            run(1, goal)

            println("Unifications: ${unificationCounter.counter}")
        }
    }
}