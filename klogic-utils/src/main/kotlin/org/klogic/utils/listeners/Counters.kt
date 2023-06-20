package org.klogic.utils.listeners

import org.klogic.core.*

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

class MplusCounter : StreamMplusListener {
    var counter: Int = 0
        private set

    override fun <T> onMplus(firstStream: RecursiveStream<T>, secondStream: RecursiveStream<T>) {
        counter++
    }
}

class BindCounter : StreamBindListener {
    var counter: Int = 0
        private set

    override fun <T, R> onBind(stream: RecursiveStream<T>, f: (T) -> RecursiveStream<R>) {
        counter++
    }
}
