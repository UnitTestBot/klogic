package org.klogic.core

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
 * Creates a lazy [Goal] by passed goal generator [f] with a fresh variable with the specified type.
 *
 * @see [delay], [State.freshTypedVar].
 */
fun <T : Term<T>> freshTypedVar(f: (Term<T>) -> Goal): Goal = delay {
    { st: State -> f(st.freshTypedVar())(st) }
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
 * Returns a result of invoking [run] overloading with first passed goal and the rest goals.
 * NOTE: [goals] must not be empty.
 */
fun <T : Term<T>> run(count: Int, term: Term<T>, goals: Array<Goal>): List<ReifiedTerm<T>> {
    require(goals.isNotEmpty()) {
        "Could not `run` with empty goals"
    }

    return run(count, term, goals.first(), *goals.drop(1).toTypedArray())
}

/**
 * Runs [unreifiedRun] with and reifies the passed [term].
 *
 * @see [unreifiedRun], [State.reify] and [ReifiedTerm].
 */
// TODO pass user mapper function to stream.
fun <T : Term<T>> run(count: Int, term: Term<T>, goal: Goal, vararg nextGoals: Goal): List<ReifiedTerm<T>> =
    unreifiedRun(count, goal, *nextGoals).reify(term)

/**
 * Returns a result of invoking [unreifiedRun] overloading with first passed goal and the rest goals.
 * NOTE: [goals] must not be empty.
 */
fun unreifiedRun(count: Int, goals: Array<Goal>): List<State> {
    require(goals.isNotEmpty()) {
        "Could not `unreifiedRun` with empty goals"
    }

    return unreifiedRun(count, goals.first(), *goals.drop(1).toTypedArray())
}

/**
 * Collects all passed goals to one conjunction, producing one [RecursiveStream],
 * and returns at most [count] [State]s.
 */
fun unreifiedRun(count: Int, goal: Goal, vararg nextGoals: Goal): List<State> =
    nextGoals
        .fold(goal) { acc, nextGoal -> acc `&&&` nextGoal }(State.empty)
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