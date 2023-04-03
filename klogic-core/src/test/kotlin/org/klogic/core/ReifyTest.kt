package org.klogic.core

import org.junit.jupiter.api.Test
import org.klogic.utils.terms.PeanoLogicNumber
import org.klogic.utils.terms.PeanoLogicNumber.Companion.succ
import org.klogic.utils.terms.ZeroNaturalNumber.Z

class ReifyTest {
    @Test
    fun testDebugVar() {
        // TODO test plus here.
    }

    // TODO rewrite using debugVar.
    private fun plus(x: Term<PeanoLogicNumber>, y: Term<PeanoLogicNumber>, z: Term<PeanoLogicNumber>): Goal = conde(
        (x `===` Z) and (z `===` y),
        freshTypedVars<PeanoLogicNumber, PeanoLogicNumber> { a, b ->
            (x `===` succ(a)) and (z `===` succ(b)) and plus(a, y, b)
        }
    )
}