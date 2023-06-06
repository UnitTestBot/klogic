package org.klogic.utils.computing

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.klogic.utils.computing.utils.*

class TwinesTest {
    @Test
    fun testTwines() {
        val twines = findTwines(15)
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
    }
}
