package org.klogic.core

import org.klogic.core.SequenceStream.Companion.lazyStream
import org.klogic.unify.walk

typealias CurStream<T> = SequenceStream<T>
typealias Goal = (State) -> CurStream<State>

infix fun Goal.or(other: Goal): Goal = { st: State -> this(st) mplus lazyStream { other(st) } }
infix fun Goal.and(other: Goal): Goal = { st: State -> this(st) bind other }

@Suppress("DANGEROUS_CHARACTERS")
infix fun Goal.`|||`(other: Goal): Goal = this or other
infix fun Goal.`&&&`(other: Goal): Goal = this and other

fun delay(f: () -> Goal): Goal = { st: State -> lazyStream { f()(st) } }

fun fresh(f: (Term) -> Goal): Goal = delay {
    { st: State -> f(st.fresh())(st) }
}

fun run(count: Int, term: Term, goal: Goal): List<Term> =
    goal(State.empty)
        .take(count)
        .map { st -> walk(term, st.substitution) }
