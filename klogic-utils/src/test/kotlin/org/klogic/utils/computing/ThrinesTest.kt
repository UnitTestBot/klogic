package org.klogic.utils.computing

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.klogic.utils.computing.utils.*
import org.klogic.utils.computing.utils.doubleQuote
import org.klogic.utils.computing.utils.lambdaSymb
import org.klogic.utils.computing.utils.listSymb
import org.klogic.utils.computing.utils.quoteSymb
import org.klogic.utils.computing.utils.repeatedPartInQuines

class ThrinesTest {
    @Test
    fun testThrines() {
        val thrines = findThrines(3)
        val firstThrine = thrines.first()
        val reifiedThrine = firstThrine.term.asReified()

        val p = reifiedThrine.first as Seq
        val q = reifiedThrine.second as Seq
        val r = reifiedThrine.third as Seq

        println(p)
        println(q)
        println(r)
        println(firstThrine.constraints)

        val variableSymb = p.extractVariable()
        val repeatedPart = Seq(
            lambdaSymb,
            Seq(variableSymb),
            Seq(
                listSymb,
                doubleQuote,
                Seq(
                    listSymb,
                    doubleQuote,
                    repeatedPartInQuines(variableSymb)
                )
            )
        )

        val expectedR = Seq(
            repeatedPart,
            Seq(
                quoteSymb,
                repeatedPart
            )
        )
        val expectedP = Seq(
            quoteSymb,
            Seq(
                quoteSymb,
                expectedR
            )
        )
        val expectedQ = Seq(
            quoteSymb,
            expectedR
        )

        assertEquals(expectedP, p)
        assertEquals(expectedQ, q)
        assertEquals(expectedR, r)
    }
}
