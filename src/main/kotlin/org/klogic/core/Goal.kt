package org.klogic.core

import org.klogic.unify.walk

typealias Goal = (State) -> KanrenStream<State>

infix fun Goal.or(other: Goal): Goal = { st: State -> this(st) mplus ThunksStream { other(st) } }
infix fun Goal.and(other: Goal): Goal = { st: State -> this(st) bind other }

@Suppress("DANGEROUS_CHARACTERS")
infix fun Goal.`|||`(other: Goal): Goal = this or other
infix fun Goal.`&&&`(other: Goal): Goal = this and other

fun delay(f: () -> Goal): Goal = { st: State -> ThunksStream { f()(st) } }

fun fresh(f: (Term) -> Goal): Goal = delay {
    { st: State -> f(st.fresh())(st) }
}

data class RunAnswer(val terms: List<Term>, val inequalityConstraints: List<InequalityConstraint> = emptyList())

fun run(count: Int, term: Term, goal: Goal, vararg nextGoals: Goal): RunAnswer =
    nextGoals.fold(goal) { acc, nextGoal ->
        acc `&&&` nextGoal
    }(State.empty)
        .check()
        .take(count)
        .let { states ->
            val terms = states.map { st -> walk(term, st.substitution) }
            val inequalityConstraints = states.flatMap { it.inequalityConstraints }

            RunAnswer(terms, inequalityConstraints)
        }

fun run(count: Int, term: Term, goals: Array<Goal>): RunAnswer {
    require(goals.isNotEmpty()) {
        "Could not `run` with empty goals"
    }

    return run(count, term, goals.first(), *goals.drop(1).toTypedArray())
}

fun List<Term>.toRunAnswer(): RunAnswer = RunAnswer(this)
