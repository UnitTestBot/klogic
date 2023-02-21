package org.klogic.core.unify

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.klogic.core.Substitution.Companion.of
import org.klogic.utils.terms.Nil.nilLogicList
import org.klogic.utils.terms.Symbol
import org.klogic.utils.x

class WalkTest {
    @Test
    fun testWalk() {
        val substitution = of(x to nilLogicList<Symbol>())
        val term = x.walk(substitution)

        val expected = nilLogicList<Symbol>()
        assertEquals(expected, term)
    }
}
