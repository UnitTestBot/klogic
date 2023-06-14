package org.klogic.utils.computing

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.klogic.core.RelationalContext
import org.klogic.core.useWith
import org.klogic.utils.computing.utils.*
import org.klogic.utils.listeners.UnificationCounter
import org.klogic.utils.withEmptyContext

class TwinesTest {
    @Test
    fun testTwines() {
        val unificationCounter = UnificationCounter()

        val twines = withEmptyContext {
            unificationListener = unificationCounter
            findTwines(15)
        }
        val firstTwine = twines.first()
        val reifiedTwine = firstTwine.term.asReified()

        val p = reifiedTwine.first.asReified() as Seq
        val q = reifiedTwine.second.asReified() as Seq

        println(p)
        println(q)
        println(firstTwine.constraints)

        val variableSymb = p.extractVariable()
        val smallRepeatedPart = Seq(
            lambdaSymb,
            Seq(variableSymb),
            Seq(
                listSymb,
                doubleQuote,
                repeatedPartInQuines(variableSymb)
            )
        )
        val bigRepeatedPart = Seq(
            quoteSymb,
            smallRepeatedPart
        )

        val expectedQ = Seq(
            smallRepeatedPart,
            bigRepeatedPart
        )
        val expectedP = Seq(
            quoteSymb,
            expectedQ
        )

        assertEquals(expectedP, p)
        assertEquals(expectedQ, q)

        println("Unifications: ${unificationCounter.counter}")
    }
}
