package org.klogic.core

import org.klogic.core.RecursiveStream.Companion.nil
import org.klogic.core.RecursiveStream.Companion.single

typealias Goal = (State) -> RecursiveStream<State>

infix fun Goal.or(other: Goal): Goal = { st: State -> this(st) mplus ThunkStream { other(st) } }
infix fun Goal.and(other: Goal): Goal = { st: State -> this(st) bind other }

@Suppress("DANGEROUS_CHARACTERS")
infix fun Goal.`|||`(other: Goal): Goal = this or other
infix fun Goal.`&&&`(other: Goal): Goal = this and other

/**
 * Represents a [Goal] that always succeeds.
 */
val success: Goal = { st: State -> single(st) }
/**
 * Represents a [Goal] that always fails.
 */
val failure: Goal = { _: State -> nil() }

/**
 * Calculates g1 ||| (g2 ||| (g3 ||| ... gn)) for a non-empty list of goals.
 *
 * NOTE: right association!
 */
fun conde(vararg goals: Goal): Goal {
    require(goals.isNotEmpty()) {
        "Expected at least one goal for conde but got 0"
    }

    return goals.reduceRight(Goal::or)
}

/**
 * Invokes [this] [Goal]. If it succeeds, returns a [RecursiveStream] with its result.
 * Otherwise, returns a [Goal] with result of invoking [second] [Goal].
 */
infix fun Goal.condo2(second: Goal): Goal = { st: State ->
    this(st).msplit()?.let {
        ConsStream(it.first, it.second)
    } ?: second(st)
}

/**
 * Calculates g1 &&& (g2 &&& (g3 &&& ... gn)) for a non-empty list of goals.
 *
 * NOTE: right association!
 */
fun and(vararg goals: Goal): Goal {
    require(goals.isNotEmpty()) {
        "Expected at least one goal for `and` but got 0"
    }

    return goals.reduceRight(Goal::and)
}

/**
 * Creates a lazy [Goal] by passed goal generator [f].
 */
fun delay(f: () -> Goal): Goal = { st: State -> ThunkStream { f()(st) } }

/**
 * Creates a lazy [Goal] by passed goal generator [f] with a fresh variable with the specified type.
 *
 * @see [delay], [State.freshTypedVar].
 */
fun <T : Term<T>> freshTypedVars(f: (Term<T>) -> Goal): Goal = delay {
    { st: State -> f(st.freshTypedVar())(st) }
}

fun <T1 : Term<T1>, T2 : Term<T2>> freshTypedVars(f: (Term<T1>, Term<T2>) -> Goal): Goal = delay {
    { st: State -> f(st.freshTypedVar(), st.freshTypedVar())(st) }
}

fun <T1 : Term<T1>, T2 : Term<T2>, T3 : Term<T3>> freshTypedVars(f: (Term<T1>, Term<T2>, Term<T3>) -> Goal): Goal = delay {
    { st: State -> f(st.freshTypedVar(), st.freshTypedVar(), st.freshTypedVar())(st) }
}

fun <T1 : Term<T1>, T2 : Term<T2>, T3 : Term<T3>, T4 : Term<T4>> freshTypedVars(f: (Term<T1>, Term<T2>, Term<T3>, Term<T4>) -> Goal): Goal = delay {
    { st: State -> f(st.freshTypedVar(), st.freshTypedVar(), st.freshTypedVar(), st.freshTypedVar())(st) }
}

fun <T1 : Term<T1>, T2 : Term<T2>, T3 : Term<T3>, T4 : Term<T4>, T5 : Term<T5>> freshTypedVars(f: (Term<T1>, Term<T2>, Term<T3>, Term<T4>, Term<T5>) -> Goal): Goal = delay {
    { st: State -> f(st.freshTypedVar(), st.freshTypedVar(), st.freshTypedVar(), st.freshTypedVar(), st.freshTypedVar())(st) }
}

fun <T1 : Term<T1>, T2 : Term<T2>, T3 : Term<T3>, T4 : Term<T4>, T5 : Term<T5>, T6: Term<T6>> freshTypedVars(f: (Term<T1>, Term<T2>, Term<T3>, Term<T4>, Term<T5>, Term<T6>) -> Goal): Goal = delay {
    { st: State -> f(st.freshTypedVar(), st.freshTypedVar(), st.freshTypedVar(), st.freshTypedVar(), st.freshTypedVar(), st.freshTypedVar())(st) }
}

fun <T1 : Term<T1>, T2 : Term<T2>, T3 : Term<T3>, T4 : Term<T4>, T5 : Term<T5>, T6: Term<T6>, T7: Term<T7>> freshTypedVars(f: (Term<T1>, Term<T2>, Term<T3>, Term<T4>, Term<T5>, Term<T6>, Term<T7>) -> Goal): Goal = delay {
    { st: State -> f(st.freshTypedVar(), st.freshTypedVar(), st.freshTypedVar(), st.freshTypedVar(), st.freshTypedVar(), st.freshTypedVar(), st.freshTypedVar())(st) }
}

/**
 * Represents an answer to evaluating the [run] expression. Contains reified [term], calculated by walking the term,
 * passed to [run], with corresponding to calculated [Goal]s [Substitution]s,
 * and set [constraints] of [Constraint]s that must be satisfied for this [term] to be reified.
 *
 * @see [run], [Constraint].
 */
data class ReifiedTerm<T : Term<T>>(val term: Term<T>, val constraints: Set<Constraint<*>> = emptySet())

/**
 * Returns a result of invoking [run] overloading with goals for the new fresh variable created using the passed [state].
 * NOTE: [goals] must not be empty.
 */
fun <T : Term<T>> run(count: Int, goals: Array<(Term<T>) -> Goal>, state: State = State.empty): List<ReifiedTerm<T>> {
    val term = state.freshTypedVar<T>()
    val goalsWithCreatedFreshVar = goals.map { it(term) }.toTypedArray()

    return run(count, term, goalsWithCreatedFreshVar, state)
}

/**
 * Returns a result of invoking [run] overloading with passed goals.
 */
fun <T : Term<T>> run(
    count: Int,
    goal: (Term<T>) -> Goal,
    vararg nextGoals: (Term<T>) -> Goal,
    state: State = State.empty
): List<ReifiedTerm<T>> = run(count, arrayOf(goal, *nextGoals), state)

/**
 * Returns a result of invoking [run] overloading with first passed goal and the rest goals.
 * NOTE: [goals] must not be empty.
 */
fun <T : Term<T>> run(count: Int, term: Term<T>, goals: Array<Goal>, state: State = State.empty): List<ReifiedTerm<T>> {
    require(goals.isNotEmpty()) {
        "Could not `run` with empty goals"
    }

    return run(count, term, goals.first(), nextGoals = goals.drop(1).toTypedArray(), state)
}

/**
 * Runs [unreifiedRun] with and reifies the passed [term].
 *
 * @see [unreifiedRun], [State.reify] and [ReifiedTerm].
 */
// TODO pass user mapper function to stream.
fun <T : Term<T>> run(
    count: Int,
    term: Term<T>,
    goal: Goal,
    vararg nextGoals: Goal,
    state: State = State.empty
): List<ReifiedTerm<T>> = unreifiedRun(count, goal, nextGoals = nextGoals, state).reify(term)

/**
 * Returns a result of invoking [unreifiedRun] overloading with first passed goal and the rest goals.
 * NOTE: [goals] must not be empty.
 */
fun unreifiedRun(count: Int, goals: Array<Goal>, state: State = State.empty): List<State> {
    require(goals.isNotEmpty()) {
        "Could not `unreifiedRun` with empty goals"
    }

    return unreifiedRun(count, goals.first(), nextGoals = goals.drop(1).toTypedArray(), state)
}

/**
 * Collects all passed goals to one conjunction in the context of the passed [state] and producing one [RecursiveStream],
 * and returns at most [count] [State]s.
 */
fun unreifiedRun(count: Int, goal: Goal, vararg nextGoals: Goal, state: State = State.empty): List<State> =
    nextGoals
        .fold(goal) { acc, nextGoal -> acc `&&&` nextGoal }(state)
        .take(count)

// TODO add simplify:
// 1) Remove irrelevant constraints
// 2) Remove subsumed constraints
/**
 * Reifies passed [term] with [State.constraints] according to the current [State.substitution].
 */
fun <T : Term<T>> State.reify(term: Term<T>): ReifiedTerm<T> = ReifiedTerm(term.walk(substitution), constraints)

/**
 * Reifies passed [term] with each [State.substitution] from [this] states.
 *
 * @see [State.reify].
 */
fun <T : Term<T>> Iterable<State>.reify(term: Term<T>): List<ReifiedTerm<T>> = map { it.reify(term) }

/**
 * Creates a [ReifiedTerm] with [this] as [ReifiedTerm.term] and empty [ReifiedTerm.constraints].
 */
fun <T : Term<T>> Term<T>.reified(): ReifiedTerm<T> = ReifiedTerm(this)

/**
 * Creates a [List] of [ReifiedTerm]s with [this] elements as [ReifiedTerm.term] and empty [ReifiedTerm.constraints].
 *
 * See [Term.reified] for more details.
 */
fun <T : Term<T>> List<Term<T>>.reified(): List<ReifiedTerm<T>> = map { it.reified() }
