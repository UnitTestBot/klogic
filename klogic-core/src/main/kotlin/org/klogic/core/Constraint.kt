package org.klogic.core

import org.klogic.unify.UnificationState
import org.klogic.unify.toUnificationState

/**
 * Represents a result of invoking [Constraint.verify].
 */
sealed interface ConstraintVerificationResult<out T : Constraint<*>>

/**
 * Represents a successful [Constraint.verify] result, with [simplifiedConstraint] as a simplified passed constraint.
 */
@JvmInline
value class SatisfiableConstraintResult<out T : Constraint<*>>(val simplifiedConstraint: T) : ConstraintVerificationResult<T>

/**
 * Represents a [Constraint.verify] result that indicates that constraint can never be violated, i.e., it is redundant.
 */
object RedundantConstraintResult : ConstraintVerificationResult<Nothing>

/**
 * Represents a failed [Constraint.verify] result — means that constraint is violated.
 */
object ViolatedConstraintResult : ConstraintVerificationResult<Nothing>

/**
 * Represents any constraint that could be applied for the current [Goal].
 */
interface Constraint<out T : Constraint<T>> {
    /**
     * Verifies this constraint with the [substitution].
     */
    fun verify(substitution: Substitution): ConstraintVerificationResult<T>
}

/**
 * Represents an inequality constraint that contains some [SingleInequalityConstraint]s.
 * The standard operation that applies this constraint is [Term.ineq].
 */
data class InequalityConstraint internal constructor(
    internal val simplifiedConstraints: List<SingleInequalityConstraint<*>>
) : Constraint<InequalityConstraint> {
    override fun verify(substitution: Substitution): ConstraintVerificationResult<InequalityConstraint> {
        if (simplifiedConstraints.isEmpty()) {
            return RedundantConstraintResult
        }

        return substitution.toUnificationState().verify(simplifiedConstraints)?.let { unificationResult ->
            val delta = unificationResult.substitutionDifference
            // If the substitution from unification does not differ from the current substitution,
            // it means that this constraint is violated.
            if (delta.isEmpty()) {
                return ViolatedConstraintResult
            }

            // Simplify this inequality constraint according to difference in substitutions
            val simplifiedConstraints = delta.map {
                SingleInequalityConstraint(it.key, it.value.cast())
            }
            val singleConstraint = InequalityConstraint(simplifiedConstraints)

            singleConstraint.toSatisfiedConstraintResult()
        } ?: RedundantConstraintResult
    }

    private tailrec fun UnificationState.verify(
        remainingSimplifiedConstraints: List<SingleInequalityConstraint<*>>
    ): UnificationState? {
        if (remainingSimplifiedConstraints.isEmpty()) {
            return this
        }

        val firstSingleConstraint = remainingSimplifiedConstraints.first()

        return unify(firstSingleConstraint.unboundedValue, firstSingleConstraint.term.cast())
            ?.verify(remainingSimplifiedConstraints.subList(1, remainingSimplifiedConstraints.size))
    }

    override fun toString(): String = simplifiedConstraints.joinToString(separator = ", ", prefix = "[", postfix = "]")

    companion object {
        fun <T : Term<T>> of(unboundedValue: UnboundedValue<T>, term: Term<T>): InequalityConstraint =
            InequalityConstraint(listOf(SingleInequalityConstraint(unboundedValue, term)))
    }


    /**
     * Represents a simple inequality constraint - [unboundedValue] cannot be equal to [term] of the same type.
     */
    data class SingleInequalityConstraint<T : Term<T>>(val unboundedValue: UnboundedValue<T>, val term: Term<T>) {
        override fun toString(): String = "$unboundedValue !== $term"
    }
}

/**
 * Creates a [SatisfiableConstraintResult] from [this].
 */
fun <T : Constraint<T>> T.toSatisfiedConstraintResult(): SatisfiableConstraintResult<T> =
    SatisfiableConstraintResult(this)
