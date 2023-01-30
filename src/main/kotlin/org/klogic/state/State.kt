package org.klogic.state

import org.klogic.streams.Stream
import org.klogic.streams.ThunksStream
import org.klogic.streams.bind
import org.klogic.streams.empty
import org.klogic.streams.mplus
import org.klogic.streams.single
import org.klogic.streams.take
import org.klogic.types.Term
import org.klogic.types.Var
import org.klogic.unify.Substitution
import org.klogic.unify.unify
import org.klogic.unify.walk

var lastIndex: Int = 0

class State(val substitution: Substitution = emptyMap()) {
    fun fresh(): Var = Var(lastIndex++)

    fun extend(index: Int, term: Term): State {
        require(index !in substitution) {
            "Index $index already in $substitution"
        }

        val result = substitution.toMutableMap()
        result[index] = term

        return State(result)
    }

    override fun toString(): String {
        return substitution.entries.joinToString("\n", prefix = "\n", postfix = "\n") { "${it.key} - ${it.value}" }
    }
}

typealias Goal = (State) -> Stream<State>

fun fresh(f: (Term) -> Goal): Goal = { st: State -> f(st.fresh())(st) }

infix fun Goal.disjunction(second: Goal): Goal = { st: State -> this(st) mplus second(st) }
infix fun Goal.conjuction(second: Goal): Goal =
    { st: State -> this(st) bind second }

fun delay(f: () -> Goal): Goal = { st: State -> ThunksStream { f()(st) } }

fun unify(first: Term, second: Term): Goal {

    return { st: State ->
//        println("first - ${walk(first, st.substitution)}, second - ${walk(second, st.substitution)}")

        unify(st.substitution, first, second)?.let { single(State(it)) } ?: empty()
    }
}

/*fun run(count: Int, term: Term, goal: Goal): List<State> =
    goal(State(emptyMap()))
        .take(count)
//        .map { walk(term, it.substitution) }*/

fun run(count: Int, term: Term, goal: Goal): List<Term> =
    goal(State(emptyMap()))
        .take(count)
        .map { st ->
//            println("state - $st")
            walk(term, st.substitution)
        }
