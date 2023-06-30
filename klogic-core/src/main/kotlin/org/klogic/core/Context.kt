package org.klogic.core

import org.klogic.core.Var.Companion.createTypedVar
import java.util.concurrent.ConcurrentHashMap

/**
 * The context for relations and unifications.
 * It configures listeners for unification and disequality events and is responsible for creating new variables.
 */
open class RelationalContext : AutoCloseable {
    val unificationListeners: MutableSet<UnificationListener> = ConcurrentHashMap.newKeySet()
    val disequalityListeners: MutableSet<DisequalityListener> = ConcurrentHashMap.newKeySet()
    val mplusListeners: MutableSet<StreamMplusListener> = ConcurrentHashMap.newKeySet()
    val bindListeners: MutableSet<StreamBindListener> = ConcurrentHashMap.newKeySet()

    /**
     * Determines whether current calculation of streams should be interrupted or not.
     */
    var shouldStopCalculations: () -> Boolean = { false }

    val nilStream: RecursiveStream<Nothing> = NilStream()

    /**
     * The index of the last [Var] created in this context.
     */
    private var lastCreatedVariableIndex: Int = 0

    /**
     * The index of the last [Wildcard] created in this context.
     */
    private var lastCreatedWildcardIndex: Int = 0

    fun addUnificationListener(unificationListener: UnificationListener) {
        unificationListeners += unificationListener
    }
    fun addDisequalityListener(disequalityListener: DisequalityListener) {
        disequalityListeners += disequalityListener
    }
    fun addMplusListener(mplusListener: StreamMplusListener) {
        mplusListeners += mplusListener
    }
    fun addBindListener(bindListener: StreamBindListener) {
        bindListeners += bindListener
    }

    fun removeUnificationListener(unificationListener: UnificationListener) {
        unificationListeners -= unificationListener
    }
    fun removeDisequalityListener(disequalityListener: DisequalityListener) {
        disequalityListeners -= disequalityListener
    }
    fun removeMplusListener(mplusListener: StreamMplusListener) {
        mplusListeners -= mplusListener
    }
    fun removeBindListener(bindListener: StreamBindListener) {
        bindListeners -= bindListener
    }

    /**
     * Returns a new variable [Var] of the specified type with [lastCreatedVariableIndex] as its [Var.index]
     * and increments [lastCreatedVariableIndex].
     *
     * NOTE: this method is not thread-safe and requires explicit outer synchronization in multithreading applications.
     */
    fun <T : Term<T>> freshTypedVar(): Var<T> = (lastCreatedVariableIndex++).createTypedVar()

    /**
     * Returns a new wildcard [Wildcard] of the specified type with [lastCreatedWildcardIndex] as its [Wildcard.index]
     * and increments [lastCreatedWildcardIndex].
     *
     * NOTE: this method is not thread-safe and requires explicit outer synchronization in multithreading applications.
     */
    fun <T : Term<T>> freshTypedWildcard(): Wildcard<T> = Wildcard(lastCreatedWildcardIndex++)

    /**
     * Returns a result of invoking [run] overloading with goals for the fresh variable created using the passed [state].
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
fun interface UnificationListener {
    fun onUnification(firstTerm: Term<*>, secondTerm: Term<*>, stateBefore: State, stateAfter: State?)
}

/**
 * A listener for [Term.ineq] events.
 */
fun interface DisequalityListener {
    fun onDisequality(firstTerm: Term<*>, secondTerm: Term<*>, stateBefore: State, stateAfter: State?)
}

/**
 * A listener for [RecursiveStream.mplus] events.
 */
interface StreamMplusListener {
    fun <T> onMplus(firstStream: RecursiveStream<T>, secondStream: RecursiveStream<T>)
}

/**
 * A listener for [RecursiveStream.bind] events.
 */
interface StreamBindListener {
    fun <T, R> onBind(stream: RecursiveStream<T>, f: (T) -> RecursiveStream<R>)
}
