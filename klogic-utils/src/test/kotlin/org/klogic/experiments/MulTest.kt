package org.klogic.experiments

import org.junit.jupiter.api.Test
import org.klogic.core.Term
import org.klogic.utils.listeners.UnificationCounter
import org.klogic.utils.singleReifiedTerm
import org.klogic.utils.terms.*
import org.klogic.utils.terms.OlegLogicNumber.Companion.toOlegLogicNumber
import org.klogic.utils.withEmptyContext

class MulTest {
    @Test
    fun testMul1x1() {
        val unificationCounter = UnificationCounter()

        withEmptyContext {
            addUnificationListener(unificationCounter)

            val first = 1u.toOlegLogicNumber()
            val second = 1u.toOlegLogicNumber()
            val goal = { q: Term<OlegLogicNumber> -> mulᴼ(first, second, q) }
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

            val first = 2u.toOlegLogicNumber()
            val second = 3u.toOlegLogicNumber()
            val goal = { q: Term<OlegLogicNumber> -> mulᴼ(first, second, q) }
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

            val five = 5u.toOlegLogicNumber()
            val goal = { q: Term<OlegLogicNumber> -> mulᴼ(five, five, q) }
            run(1, goal)

            println("Unifications: ${unificationCounter.counter}")
        }
    }

    @Test
    fun testMul127x127() {
        val unificationCounter = UnificationCounter()

        withEmptyContext {
            addUnificationListener(unificationCounter)

            val `127` = 127u.toOlegLogicNumber()
            val goal = { q: Term<OlegLogicNumber> -> mulᴼ(`127`, `127`, q) }
            run(1, goal)

            println("Unifications: ${unificationCounter.counter}")
        }
    }
}