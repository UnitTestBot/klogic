package org.klogic.utils.unifications

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.klogic.core.unificationCounter
import org.klogic.utils.computing.findQuines
import org.klogic.utils.computing.findThrines
import org.klogic.utils.computing.findTwines

class UnificationsMeasurement {
    @AfterEach
    fun clear() {
        unificationCounter = 0
    }

    @Test
    fun qwines() {
        findQuines(1)
        println("Quines(1):$unificationCounter")
    }

    @Test
    fun twines() {
        findTwines(1)
        println("Twines(1):$unificationCounter")
    }

    @Test
    fun thrines() {
        findThrines(1)
        println("Thrines(1):$unificationCounter")
    }
}