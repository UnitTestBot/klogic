package org.klogic.core

import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentHashSetOf
import kotlinx.collections.immutable.toPersistentHashSet
import org.klogic.core.Var.Companion.createTypedVar
import org.klogic.unify.toUnificationState

typealias InequalityConstraints = PersistentSet<InequalityConstraint>

/**
 * Represents a current immutable state of current [run] expression with [substitution] for [Var]s,
 * passed satisfiable [Constraint]s,
 * and an index of the last created with [freshTypedVar] variable.
 */
data class State(
    val substitution: Substitution,
    val constraints: PersistentSet<Constraint<*>> = persistentHashSetOf(),
    @PublishedApi internal var lastCreatedVariableIndex: Int = 0
) {
    constructor(
        map: Map<Var<out Term>, Term>,
        constraints: PersistentSet<Constraint<*>>,
        lastCreatedVariableIndex: Int = 0
    ) : this(Substitution(map), constraints, lastCreatedVariableIndex)

    private val inequalityConstraints: InequalityConstraints =
        constraints.filterIsInstance<InequalityConstraint>().toPersistentHashSet()

    /**
     * Returns a new variable [Var] with [lastCreatedVariableIndex] as its [Var.index] and increments [lastCreatedVariableIndex].
     */
    inline fun <reified T: Term> freshTypedVar(): Var<T> = (lastCreatedVariableIndex++).createTypedVar()

    /**
     * Returns a new state with [substitution] extended with passed not already presented association of [variable] to [term].
     */
    fun <T: Term> extend(variable: Var<T>, term: T): State {
        require(variable !in substitution) {
            "Variable $variable already exists in substitution $substitution"
        }

        return State(substitution + (variable to term), inequalityConstraints, lastCreatedVariableIndex)
    }

    /**
     * Tries to unify [left] and [right] terms with the current [substitution].
     * If the unification succeeds, tries to [verify] current [constraints] with calculated unification substitution,
     * and returns null otherwise.
     * If constraints verification succeeds, returns new [State] with unification substitution and verified simplified
     * constraints, and returns null otherwise.
     */
    fun <T : Term> unifyWithConstraintsVerification(left: T, right: T): State? {
        val unificationState = toUnificationState()
        val successfulUnificationState = left.unify(right, unificationState) ?: return null

        if (successfulUnificationState.substitutionDifference.isEmpty()) {
            // Empty difference allows us to not verify constraints as they should be already verified.
            return this
        }

        val unificationSubstitution = successfulUnificationState.substitution
        val verifiedConstraints = verify(unificationSubstitution, constraints) ?: return null

        return copy(substitution = unificationSubstitution, constraints = verifiedConstraints.toPersistentHashSet())
    }

    operator fun <T : Term> plus(pair: Pair<Var<T>, T>): State = extend(pair.first, pair.second)

    companion object {
        private val EMPTY_STATE: State = State(Substitution.empty)

        val empty: State = EMPTY_STATE
    }
}

/**
 * Verifies [constraints] with passed [substitution] by invoking [Constraint.verify] - if any constraint is violated, returns null.
 * Otherwise, returns a [Collection] of new constraints simplified according to theirs [Constraint.verify].
 */
fun <T : Constraint<T>> verify(substitution: Substitution, constraints: Collection<T>): Collection<T>? {
    val simplifiedConstraints = mutableSetOf<T>()

    for (constraint in constraints) {
        when (val constraintVerificationResult = constraint.verify(substitution)) {
            is ViolatedConstraintResult -> return null
            is SatisfiableConstraintResult<T> -> simplifiedConstraints += constraintVerificationResult.simplifiedConstraint
            is RedundantConstraintResult -> {
                // Skip this constraint
            }
        }
    }

    return simplifiedConstraints
}
