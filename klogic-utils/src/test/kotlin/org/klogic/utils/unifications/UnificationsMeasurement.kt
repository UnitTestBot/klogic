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
        val quines = findQuines(1)

        println(quines.first().term.asReified())
        println(quines.first().constraints)

        println("Quines(1):$unificationCounter")
    }

    @Test
    fun twines() {
        val twines = findTwines(1)

        val term = twines.single().term.asReified()
        term.let {
            println(it.first)
            println(it.second)
            println(twines.single().constraints)
        }

        println("Twines(1):$unificationCounter")
    }

    @Test
    fun thrines() {
        val thrines = findThrines(1)

        val term = thrines.single().term.asReified()
        term.let {
            println(it.first)
            println(it.second)
            println(it.third)
            println(thrines.single().constraints)
        }

        println("Thrines(1):$unificationCounter")
    }
}