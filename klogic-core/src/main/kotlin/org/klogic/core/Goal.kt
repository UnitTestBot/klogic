package org.klogic.core

import org.klogic.core.RecursiveStream.Companion.single

typealias Goal = (State) -> RecursiveStream<State>

context(RelationalContext)
private infix fun (Goal).disjunctionBase(other: Goal) = { st: State ->
    this(st) mplus ThunkStream { other(st) }
}

context(RelationalContext)
infix fun Goal.or(other: Goal): Goal = { newState: State ->
    ThunkStream { disjunctionBase(other)(newState) }
}

infix fun Goal.and(other: Goal): Goal = { st: State -> this(st) bind other }

context(RelationalContext)
@Suppress("DANGEROUS_CHARACTERS")
infix fun Goal.`|||`(other: Goal): Goal = this or other

infix fun Goal.`&&&`(other: Goal): Goal = this and other

/**
 * Represents a [Goal] that always succeeds.
 */
context(RelationalContext)
val success: Goal
    inline get() = { st: State -> single(st) }

/**
 * Represents a [Goal] that always fails.
 */
context(RelationalContext)
val failure: Goal
    inline get() = { nilStream }

/**
 * Calculates g1 ||| (g2 ||| (g3 ||| ... gn)) for a sequence of goals.
 *
 * NOTE: right association!
 *
 * Such implementation is taken from [OCanren](https://github.com/PLTools/OCanren/blob/b1a4cb7b2fb7fd22e026f4a010cc21ce79676705/src/core/Core.ml#L498)
 */
context(RelationalContext)
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
context(RelationalContext)
inline infix fun Goal.condo2(crossinline second: Goal): Goal = { st: State ->
    this(st).msplit()?.let {
        ConsStream(it.first, it.second)
    } ?: second(st)
}

/**
 * Calculates ((...((g1 &&& g2) &&& g3) &&& ... gn)) for a sequence of goals.
 *
 * NOTE: left association!
 */
fun and(goal: Goal, vararg goals: Goal): Goal {
//    goal and goals.reduceRight(Goal::and)
    val gs : List<Goal> =  listOf(goal) + goals.asList();
    val f : (Goal, Goal) -> Goal = Goal::and
    return gs.reduce(f)
}

/**
 * Creates a lazy [Goal] by passed goal generator [f].
 */
context(RelationalContext)
inline fun delay(crossinline f: () -> Goal): Goal = { st: State -> ThunkStream { f()(st) } }

/**
 * Reifies walked [term] using the passed [reifier] and returns a Goal from the passed [callBack].
 */
inline fun <T : Term<T>> debugVar(
    term: Term<T>,
    crossinline reifier: (Term<T>) -> ReifiedTerm<T>,
    crossinline callBack: (ReifiedTerm<T>) -> Goal
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
context(RelationalContext)
inline fun <T : Term<T>> freshTypedVars(crossinline f: (Term<T>) -> Goal): Goal = delay {
    { st: State -> f(freshTypedVar())(st) }
}

context(RelationalContext)
inline fun <T1 : Term<T1>, T2 : Term<T2>> freshTypedVars(crossinline f: (Term<T1>, Term<T2>) -> Goal): Goal = delay {
    { st: State -> f(freshTypedVar(), freshTypedVar())(st) }
}

context(RelationalContext)
inline fun <T1 : Term<T1>, T2 : Term<T2>, T3 : Term<T3>> freshTypedVars(crossinline f: (Term<T1>, Term<T2>, Term<T3>) -> Goal): Goal = delay {
    { st: State -> f(freshTypedVar(), freshTypedVar(), freshTypedVar())(st) }
}

context(RelationalContext)
inline fun <T1 : Term<T1>, T2 : Term<T2>, T3 : Term<T3>, T4 : Term<T4>> freshTypedVars(crossinline f: (Term<T1>, Term<T2>, Term<T3>, Term<T4>) -> Goal): Goal = delay {
    { st: State -> f(freshTypedVar(), freshTypedVar(), freshTypedVar(), freshTypedVar())(st) }
}

context(RelationalContext)
inline fun <T1 : Term<T1>, T2 : Term<T2>, T3 : Term<T3>, T4 : Term<T4>, T5 : Term<T5>> freshTypedVars(crossinline f: (Term<T1>, Term<T2>, Term<T3>, Term<T4>, Term<T5>) -> Goal): Goal = delay {
    { st: State -> f(freshTypedVar(), freshTypedVar(), freshTypedVar(), freshTypedVar(), freshTypedVar())(st) }
}

context(RelationalContext)
inline fun <T1 : Term<T1>, T2 : Term<T2>, T3 : Term<T3>, T4 : Term<T4>, T5 : Term<T5>, T6: Term<T6>> freshTypedVars(crossinline f: (Term<T1>, Term<T2>, Term<T3>, Term<T4>, Term<T5>, Term<T6>) -> Goal): Goal = delay {
    { st: State -> f(freshTypedVar(), freshTypedVar(), freshTypedVar(), freshTypedVar(), freshTypedVar(), freshTypedVar())(st) }
}

context(RelationalContext)
inline fun <T1 : Term<T1>, T2 : Term<T2>, T3 : Term<T3>, T4 : Term<T4>, T5 : Term<T5>, T6 : Term<T6>, T7 : Term<T7>> freshTypedVars(crossinline f: (Term<T1>, Term<T2>, Term<T3>, Term<T4>, Term<T5>, Term<T6>, Term<T7>) -> Goal): Goal = delay {
    { st: State -> f(freshTypedVar(), freshTypedVar(), freshTypedVar(), freshTypedVar(), freshTypedVar(), freshTypedVar(), freshTypedVar())(st) }
}

context(RelationalContext)
inline fun <T1 : Term<T1>, T2 : Term<T2>, T3 : Term<T3>, T4 : Term<T4>, T5 : Term<T5>, T6 : Term<T6>, T7 : Term<T7>, T8 : Term<T8>> freshTypedVars(crossinline f: (Term<T1>, Term<T2>, Term<T3>, Term<T4>, Term<T5>, Term<T6>, Term<T7>, Term<T8>) -> Goal): Goal = delay {
    { st: State -> f(freshTypedVar(), freshTypedVar(), freshTypedVar(), freshTypedVar(), freshTypedVar(), freshTypedVar(), freshTypedVar(), freshTypedVar())(st) }
}

/**
 * Represents an answer to evaluating the [run] expression. Contains reified [term], calculated by walking the term,
 * passed to [run], with corresponding to calculated [Goal]s [Substitution]s,
 * and set [constraints] of [Constraint]s that must be satisfied for this [term] to be reified.
 *
 * @see [run], [Constraint].
 */
data class ReifiedTerm<T : Term<T>>(val term: Term<T>, val constraints: Set<Constraint<*>> = emptySet())

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
