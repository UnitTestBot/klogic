package org.klogic.core

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.klogic.core.RecursiveList.Companion.nil
import org.klogic.core.Substitution.Companion.of
import org.klogic.unify.walk

class WalkTest {
    @Test
    fun testWalk() {
        val substitution = of(0.toTerm() as Var to nil)
        val term = walk(0.toTerm(), substitution)

        val expected = nil
        assertEquals(expected, term)
    }
}