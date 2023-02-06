package org.klogic.core

import org.klogic.unify.toUnificationResult

/**
 * Represents a result of invoking [Constraint.check].
 */
sealed class ConstraintCheckResult

/**
 * Represents a successful [Constraint.check] result, with [resultedConstraints] as transformed constraints.
 */
class SatisfiedConstraintResult(val resultedConstraints: List<Constraint>) : ConstraintCheckResult()

/**
 * Represents a failed [Constraint.check] result — means that constraint is always violated.
 */
object ViolatedConstraintResult : ConstraintCheckResult()

/**
 * Represents any constraint that could be applied for the current [Goal].
 */
interface Constraint {
    /**
     * Checks this constraint for satisfiability with a substitution from [state].
     */
    fun check(state: State): ConstraintCheckResult
}

/**
 * Represents an inequality constraint - [left] term cannot be equal to [right] term.
 * The standard operation that applies this constraint is [Term.ineq].
 */
data class InequalityConstraint(val left: Term, val right: Term) : Constraint {
    override fun check(state: State): ConstraintCheckResult =
        with(state) {
            // To check whether it is possible to violate this inequality constraint,
            // we need to try to unify its left term with its right term.
            toUnificationResult().unify(left, right)?.let { inequalityUnificationResult ->
                val delta = inequalityUnificationResult.substitutionDifference
                // If the substitution from unification does not differ from the current substitution,
                // it means that this constraint is always violated.
                if (delta.isEmpty()) {
                    return@check ViolatedConstraintResult
                }

                // Otherwise, we should transform this constraint to the list of inequality constraints from resulted
                // substitution difference.
                delta.entries.map {
                    InequalityConstraint(it.key, it.value)
                }
            } ?: emptyList()
        }.toSatisfiedConstraintResult()

    override fun toString(): String = "$left !== $right"
}

fun List<Constraint>.toSatisfiedConstraintResult(): SatisfiedConstraintResult = SatisfiedConstraintResult(this)
