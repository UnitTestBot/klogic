package org.klogic.core

import org.klogic.unify.walk

typealias Goal = (State) -> RecursiveStream<State>

infix fun Goal.or(other: Goal): Goal = { st: State -> this(st) mplus ThunkStream { other(st) } }
infix fun Goal.and(other: Goal): Goal = { st: State -> this(st) bind other }

@Suppress("DANGEROUS_CHARACTERS")
infix fun Goal.`|||`(other: Goal): Goal = this or other
infix fun Goal.`&&&`(other: Goal): Goal = this and other

/**
 * Creates a lazy [Goal] by passed goal generator [f].
 */
fun delay(f: () -> Goal): Goal = { st: State -> ThunkStream { f()(st) } }

/**
 * Creates a lazy [Goal] by passed goal generator [f] with a fresh variable.
 *
 * @see [delay], [State.fresh].
 */
fun fresh(f: (Term) -> Goal): Goal = delay {
    { st: State -> f(st.fresh())(st) }
}

/**
 * Represents an answer to evaluating the [run] expression. Contains list [terms] with a size of at most passed to
 * [run] count, calculated by walking the term, passed to [run], with corresponding to calculated [Goal]s [Substitution]s,
 * and set [constraints] of [Constraint]s that must be satisfied.
 *
 * NOTE: pay attention that [constraints] are related to ALL [terms].
 *
 * @see [run], [Constraint].
 */
data class RunAnswer(val terms: List<Term>, val constraints: Set<Constraint> = emptySet())

/**
 * Finds up to [count] ways to instantiate the logic variable bound to [term] such that all the [goals] in its body succeed.
 * [goals] must not be empty.
 */
fun run(count: Int, term: Term, goals: Array<Goal>): RunAnswer {
    require(goals.isNotEmpty()) {
        "Could not `run` with empty goals"
    }

    return run(count, term, goals.first(), *goals.drop(1).toTypedArray())
}

/**
 * Collects all passed goals to one conjunction, producing one [RecursiveStream],
 * [check]s its [Constraint]s for satisfiability,
 * takes at most [count] [State]s,
 * and returns list of [RunAnswer] by walking passed [term].
 *
 * For more details, see [RunAnswer] docs.
 */
fun run(count: Int, term: Term, goal: Goal, vararg nextGoals: Goal): RunAnswer =
    nextGoals.fold(goal) { acc, nextGoal ->
        acc `&&&` nextGoal
    }(State.empty)
        .check()
        .take(count)
        .let { states ->
            val terms = states.map { st -> walk(term, st.substitution) }
            val constraints = states.flatMapTo(mutableSetOf()) { it.constraints }

            RunAnswer(terms, constraints)
        }

/**
 * Returns a [RunAnswer] with [this] as [RunAnswer.terms] and empty [RunAnswer.constraints].
 */
fun List<Term>.toRunAnswer(): RunAnswer = RunAnswer(this)
