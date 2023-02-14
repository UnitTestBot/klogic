package org.klogic.core.unify

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.klogic.core.Substitution.Companion.of
import org.klogic.terms.Nil.nilRecursiveList
import org.klogic.terms.Symbol
import org.klogic.utils.x

class WalkTest {
    @Test
    fun testWalk() {
        val substitution = of(x to nilRecursiveList<Symbol>())
        val term = x.walk(substitution)

        val expected = nilRecursiveList<Symbol>()
        assertEquals(expected, term)
    }
}
