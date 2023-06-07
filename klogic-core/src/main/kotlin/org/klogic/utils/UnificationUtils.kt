package org.klogic.utils

import org.klogic.core.UnificationListener

/**
 * A [UnificationListener] that coule be useful to count a number of unifications for a particular [org.klogic.core.run]
 * (reified or not). To use it, just pass it as a one of unification listeners to the [org.klogic.core.run] method,
 * and then use the [counter] value as a resulting number of unifications.
 *
 * NOTE: Do not forget to invoke the [clear] method among different runs.
 */
object UnificationCounter : UnificationListener {
    var counter: Int = 0
        private set

    override fun invoke() {
        counter++
    }

    fun clear() {
        counter = 0
    }
}
