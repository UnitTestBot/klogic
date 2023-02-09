package org.klogic.core

import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentHashSetOf
import kotlinx.collections.immutable.toPersistentHashSet
import org.klogic.unify.toUnificationState

typealias InequalityConstraints = PersistentSet<InequalityConstraint>

/**
 * Represents a current immutable state of current [run] expression with [substitution] for [Var]s, passed [Constraint]s,
 * and an index of the last created with [fresh] variable.
 */
data class State(
    val substitution: Substitution,
    val constraints: PersistentSet<Constraint<*>> = persistentHashSetOf(),
    private var lastCreatedVariableIndex: Int = 0
) {
    constructor(map: Map<Var, Term>, constraints: PersistentSet<Constraint<*>>, lastCreatedVariableIndex: Int = 0) :
            this(Substitution(map), constraints, lastCreatedVariableIndex)

    private val inequalityConstraints: InequalityConstraints =
        constraints.filterIsInstance<InequalityConstraint>().toPersistentHashSet()

    /**
     * Returns a new variable [Var] with [lastCreatedVariableIndex] as its [Var.index] and increments [lastCreatedVariableIndex].
     */
    fun fresh(): Var = Var(lastCreatedVariableIndex++)

    /**
     * Returns a new state with [substitution] extended with passed not already presented association of [variable] to [term].
     */
    fun extend(variable: Var, term: Term): State {
        require(variable !in substitution) {
            "Variable $variable already exists in substitution $substitution"
        }

        return State(substitution + (variable to term), inequalityConstraints, lastCreatedVariableIndex)
    }

    fun unifyWithConstraintsVerification(left: Term, right: Term): State? {
        val unificationState = toUnificationState()
        val successfulUnificationState = unificationState.unify(left, right) ?: return null

        if (successfulUnificationState.substitutionDifference.isEmpty()) {
            // Empty difference allows us to not verify constraints as they should be already verified.
            return this
        }

        val unificationSubstitution = successfulUnificationState.substitution
        val verifiedConstraints = verify(unificationSubstitution, constraints) ?: return null

        return copy(substitution = unificationSubstitution, constraints = verifiedConstraints.toPersistentHashSet())
    }

    operator fun plus(pair: Pair<Var, Term>): State = extend(pair.first, pair.second)

    companion object {
        private val EMPTY_STATE: State = State(Substitution.empty)

        val empty: State = EMPTY_STATE
    }
}

// TODO change docs.
/**
 * Verifies [constraints] by invoking [Constraint.verify] - if any constraint is always violated, returns null.
 * Otherwise, returns a new state with new constraints simplified according to theirs [Constraint.verify].
 */
fun <T : Constraint<T>> verify(substitution: Substitution, constraints: Collection<T>): Collection<T>? {
    val simplifiedConstraints = mutableSetOf<T>()

    for (constraint in constraints) {
        when (val constraintVerificationResult = constraint.verify(substitution)) {
            is ViolatedConstraintResult -> return null
            is SatisfiedConstraintResult<T> -> simplifiedConstraints += constraintVerificationResult.simplifiedConstraint
            is RedundantConstraintResult -> {
                // Skip this constraint
            }
        }
    }

    return simplifiedConstraints
}
