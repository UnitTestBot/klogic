package org.klogic.core

import org.klogic.core.RecursiveStream.Companion.nilStream
import org.klogic.core.RecursiveStream.Companion.single

typealias Goal = (State) -> RecursiveStream<State>

private infix fun (Goal).disjunctionBase(other: Goal) = { st: State ->
    this(st) mplus ThunkStream { other(st) }
}

infix fun Goal.or(other: Goal): Goal = { newState: State ->
    ThunkStream{ disjunctionBase(other)(newState) }
}

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
val failure: Goal = { nilStream() }

/**
 * Calculates g1 ||| (g2 ||| (g3 ||| ... gn)) for a sequence of goals.
 *
 * NOTE: right association!
 *
 * Such implementation is taken from [OCanren](https://github.com/PLTools/OCanren/blob/b1a4cb7b2fb7fd22e026f4a010cc21ce79676705/src/core/Core.ml#L498)
 */
fun conde(goal: Goal, vararg goals: Goal): Goal = { state: State ->
    val allGoals = listOf(goal, *goals)

    fun inner(innerGoals: List<Goal>): Goal = when {
        innerGoals.size == 1 -> innerGoals.first()
        innerGoals.size > 1 -> {
            val nextGoals = innerGoals.subList(1, innerGoals.size)

            innerGoals.first() disjunctionBase inner(nextGoals)
        }
        else -> error("Unexpected empty goals for inner disjunction in conde")
    }

    val innerResult = inner(allGoals)

    ThunkStream { innerResult(state) }
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
 * Calculates g1 &&& (g2 &&& (g3 &&& ... gn)) for a sequence of goals.
 *
 * NOTE: right association!
 */
fun and(goal: Goal, vararg goals: Goal): Goal = goal and goals.reduceRight(Goal::and)

/**
 * Creates a lazy [Goal] by passed goal generator [f].
 */
fun delay(f: () -> Goal): Goal = { st: State -> ThunkStream { f()(st) } }

/**
 * Reifies walked [term] using the passed [reifier] and returns a Goal from the passed [callBack].
 */
fun <T : Term<T>> debugVar(
    term: Term<T>,
    reifier: (Term<T>) -> ReifiedTerm<T>,
    callBack: (ReifiedTerm<T>) -> Goal
): Goal = { st: State ->
    val walkedTerm = term.walk(st.substitution)
    val reified = reifier(walkedTerm)

    callBack(reified)(st)
}

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

fun <T1 : Term<T1>, T2 : Term<T2>, T3 : Term<T3>, T4 : Term<T4>, T5 : Term<T5>, T6 : Term<T6>, T7 : Term<T7>> freshTypedVars(f: (Term<T1>, Term<T2>, Term<T3>, Term<T4>, Term<T5>, Term<T6>, Term<T7>) -> Goal): Goal = delay {
    { st: State -> f(st.freshTypedVar(), st.freshTypedVar(), st.freshTypedVar(), st.freshTypedVar(), st.freshTypedVar(), st.freshTypedVar(), st.freshTypedVar())(st) }
}

fun <T1 : Term<T1>, T2 : Term<T2>, T3 : Term<T3>, T4 : Term<T4>, T5 : Term<T5>, T6 : Term<T6>, T7 : Term<T7>, T8 : Term<T8>> freshTypedVars(f: (Term<T1>, Term<T2>, Term<T3>, Term<T4>, Term<T5>, Term<T6>, Term<T7>, Term<T8>) -> Goal): Goal = delay {
    { st: State -> f(st.freshTypedVar(), st.freshTypedVar(), st.freshTypedVar(), st.freshTypedVar(), st.freshTypedVar(), st.freshTypedVar(), st.freshTypedVar(), st.freshTypedVar())(st) }
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
fun <T : Term<T>> run(
    count: Int,
    goals: Array<(Term<T>) -> Goal>,
    state: State = State.empty,
    unificationListeners: Iterable<UnificationListener> = emptyList()
): List<ReifiedTerm<T>> {
    val term = state.freshTypedVar<T>()
    val goalsWithCreatedFreshVar = goals.map { it(term) }.toTypedArray()

    return run(count, term, goalsWithCreatedFreshVar, state, unificationListeners)
}

/**
 * Returns a result of invoking [run] overloading with passed goals.
 */
fun <T : Term<T>> run(
    count: Int,
    goal: (Term<T>) -> Goal,
    vararg nextGoals: (Term<T>) -> Goal,
    state: State = State.empty,
    unificationListeners: Iterable<UnificationListener> = emptyList()
): List<ReifiedTerm<T>> = run(count, arrayOf(goal, *nextGoals), state, unificationListeners)

/**
 * Returns a result of invoking [run] overloading with first passed goal and the rest goals.
 * NOTE: [goals] must not be empty.
 */
fun <T : Term<T>> run(
    count: Int,
    term: Term<T>,
    goals: Array<Goal>,
    state: State = State.empty,
    unificationListeners: Iterable<UnificationListener> = emptyList()
): List<ReifiedTerm<T>> {
    require(goals.isNotEmpty()) {
        "Could not `run` with empty goals"
    }

    return run(count, term, goals.first(), nextGoals = goals.drop(1).toTypedArray(), state, unificationListeners)
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
    state: State = State.empty,
    unificationListeners: Iterable<UnificationListener> = emptyList()
): List<ReifiedTerm<T>> =
    unreifiedRun(count, goal, nextGoals = nextGoals, state, unificationListeners = unificationListeners).reify(term)

/**
 * Returns a result of invoking [unreifiedRun] overloading with first passed goal and the rest goals.
 * NOTE: [goals] must not be empty.
 */
fun unreifiedRun(
    count: Int,
    goals: Array<Goal>,
    state: State = State.empty,
    unificationListeners: Iterable<UnificationListener> = emptyList()
): List<State> {
    require(goals.isNotEmpty()) {
        "Could not `unreifiedRun` with empty goals"
    }

    return unreifiedRun(
        count,
        goals.first(),
        nextGoals = goals.drop(1).toTypedArray(),
        state,
        unificationListeners = unificationListeners
    )
}

/**
 * Collects all passed goals to one conjunction in the context of the passed [state] and producing one [RecursiveStream],
 * and returns at most [count] [State]s.
 *
 * @param unificationListeners all [UnificationListener]s that can listen [Term.unify] events for this particular run.
 */
fun unreifiedRun(
    count: Int,
    goal: Goal,
    vararg nextGoals: Goal,
    state: State = State.empty,
    unificationListeners: Iterable<UnificationListener> = emptyList()
): List<State> {
    Term.unificationListeners = unificationListeners

    val result = nextGoals
        .fold(goal) { acc, nextGoal -> acc `&&&` nextGoal }(state)
        .take(count)

    Term.clearListeners()

    return result
}

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
