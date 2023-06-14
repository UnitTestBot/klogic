package org.klogic.core

import org.klogic.core.DisequalityListener.Companion.EmptyDisequalityListener
import org.klogic.core.UnificationListener.Companion.EmptyUnificationListener
import org.klogic.core.Var.Companion.createTypedVar

/**
 * The context for relations and unifications.
 * It configures listeners for unification and disequality events and is responsible for creating new variables.
 */
open class RelationalContext : AutoCloseable {
    var unificationListener: UnificationListener = EmptyUnificationListener
    var disequalityListener: DisequalityListener = EmptyDisequalityListener

    /**
     * The index of the last variable created in this context.
     */
    private var lastCreatedVariableIndex: Int = 0

    fun removeUnificationListener() {
        unificationListener = EmptyUnificationListener
    }

    fun removeDisequalityListener() {
        disequalityListener = EmptyDisequalityListener
    }

    /**
     * Returns a new variable [Var] of the specified type with [lastCreatedVariableIndex] as its [Var.index]
     * and increments [lastCreatedVariableIndex].
     */
    fun <T : Term<T>> freshTypedVar(): Var<T> = (lastCreatedVariableIndex++).createTypedVar()

    /**
     * Returns a result of invoking [run] overloading with goals for the new fresh variable created using the passed [state].
     * NOTE: [goals] must not be empty.
     */
    fun <T : Term<T>> run(count: Int, goals: Array<(Term<T>) -> Goal>, state: State = State.empty): List<ReifiedTerm<T>> {
        val term = freshTypedVar<T>()
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

    override fun close() {
        // Do nothing for now
    }
}

inline fun <T : AutoCloseable?, R> T.useWith(block: T.() -> R): R = use { it.block() }

/**
 * A listener for [Term.unify] events.
 */
interface UnificationListener {
    fun onUnification(firstTerm: Term<*>, secondTerm: Term<*>, stateBefore: State, stateAfter: State?) = Unit

    companion object {
        /**
         * Listener that does nothing on [Term.unify] events.
         */
        internal object EmptyUnificationListener : UnificationListener
    }
}

/**
 * A listener for [Term.ineq] events.
 */
interface DisequalityListener {
    fun onDisequality(firstTerm: Term<*>, secondTerm: Term<*>, stateBefore: State, stateAfter: State?) = Unit

    companion object {
        /**
         * Listener that does nothing on [Term.ineq] events.
         */
        internal object EmptyDisequalityListener : DisequalityListener
    }
}
