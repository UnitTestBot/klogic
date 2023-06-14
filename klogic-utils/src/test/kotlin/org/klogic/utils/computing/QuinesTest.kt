package org.klogic.utils.computing

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.klogic.core.RelationalContext
import org.klogic.core.useWith
import org.klogic.utils.computing.utils.extractVariable
import org.klogic.utils.computing.utils.lambdaSymb
import org.klogic.utils.computing.utils.quoteSymb
import org.klogic.utils.computing.utils.repeatedPartInQuines
import org.klogic.utils.listeners.UnificationCounter
import org.klogic.utils.withEmptyContext

class QuinesTest {
    @Test
    fun testQuines() {
        val unificationCounter = UnificationCounter()

        val quines = withEmptyContext {
            unificationListener = unificationCounter

            findQuines(10)
        }
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

        println("Unifications: ${unificationCounter.counter}")
    }
}
