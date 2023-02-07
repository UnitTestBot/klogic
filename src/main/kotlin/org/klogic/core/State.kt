package org.klogic.core

import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentHashSetOf
import kotlinx.collections.immutable.toPersistentHashSet
import org.klogic.core.InequalityConstraint.SingleInequalityConstraint
import org.klogic.unify.toUnificationResult

typealias InequalityConstraints = PersistentSet<InequalityConstraint>

/**
 * Represents a current immutable state of current [run] expression with [substitution] for [Var]s, passed [Constraint]s,
 * and an index of the last created with [fresh] variable.
 */
data class State(
    val substitution: Substitution,
    val constraints: PersistentSet<Constraint> = persistentHashSetOf(),
    private var lastCreatedVariableIndex: Int = 0
) {
    constructor(map: Map<Var, Term>, constraints: PersistentSet<Constraint>, lastCreatedVariableIndex: Int = 0) :
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

    /**
     * Returns a new state with [InequalityConstraint] of [left] and [right] terms added to [constraints].
     */
    fun ineq(left: Term, right: Term): State? {
        return toUnificationResult().unify(left, right)?.let { unificationResult ->
            val delta = unificationResult.substitutionDifference
            // If the substitution from unification does not differ from the current substitution,
            // it means that this constraint is always violated.
            if (delta.isEmpty()) {
                return null
            }

            val simplifiedConstraints = delta.map { SingleInequalityConstraint(it.key, it.value) }
            val singleConstraint = InequalityConstraint(simplifiedConstraints)

            copy(constraints = constraints.add(singleConstraint))
        } ?: this
    }

    /**
     * Verifies [constraints] by invoking [Constraint.verify] - if any constraint is always violated, returns null.
     * Otherwise, returns a new state with new constraints simplified according to theirs [Constraint.verify].
     */
    fun verify(): State? {
        val simplifiedConstraints = mutableSetOf<Constraint>()
        val constraintsResults = constraints.asSequence().map {
            it.verify(this)
        }

        for (constraintResult in constraintsResults) {
            when (constraintResult) {
                is ViolatedConstraintResult -> return null
                is SatisfiedConstraintResult -> simplifiedConstraints += constraintResult.simplifiedConstraint
                is RedundantConstraintResult -> {
                    // do nothing
                }
            }
        }

        return copy(constraints = simplifiedConstraints.toPersistentHashSet())
    }

    operator fun plus(pair: Pair<Var, Term>): State = extend(pair.first, pair.second)

    companion object {
        private val EMPTY_STATE: State = State(Substitution.empty)

        val empty: State = EMPTY_STATE
    }
}
