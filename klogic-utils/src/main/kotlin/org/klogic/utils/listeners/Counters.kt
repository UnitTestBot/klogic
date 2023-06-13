package org.klogic.utils.listeners

import org.klogic.core.DisequalityListener
import org.klogic.core.State
import org.klogic.core.Term
import org.klogic.core.UnificationListener

class UnificationCounter : UnificationListener {
    var counter: Int = 0
        private set

    override fun onUnification(firstTerm: Term<*>, secondTerm: Term<*>, stateBefore: State, stateAfter: State?) {
        counter++
    }
}

class DisequalityCounter : DisequalityListener {
    var counter: Int = 0
        private set

    override fun onDisequality(firstTerm: Term<*>, secondTerm: Term<*>, stateBefore: State, stateAfter: State?) {
        counter++
    }
}
