package org.klogic.utils.computing

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.klogic.utils.computing.utils.extractVariable
import org.klogic.utils.computing.utils.lambdaSymb
import org.klogic.utils.computing.utils.quoteSymb
import org.klogic.utils.computing.utils.repeatedPartInQuines

class QuinesTest {
    @Test
    fun testQuines() {
        val quines = findQuines(10)
        val quine = quines.first()
        val actualQuine = quine.term.asReified() as Seq

        println(actualQuine)
        println(quine.constraints)

        val variableSymb = actualQuine.extractVariable()
        val repeatedPart = repeatedPartInQuines(variableSymb)
        val expectedQuine = Seq(
            Seq(
                lambdaSymb,
                Seq(variableSymb),
                repeatedPart
            ),
            Seq(
                quoteSymb,
                Seq(
                    lambdaSymb,
                    Seq(variableSymb),
                    repeatedPart
                )
            )
        )

        assertEquals(expectedQuine, actualQuine)
    }
}
