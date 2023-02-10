package org.klogic.core

import org.klogic.unify.UnificationState
import org.klogic.unify.toUnificationState

/**
 * Represents a result of invoking [Constraint.verify].
 */
sealed class ConstraintVerificationResult<out T : Constraint<*>>

/**
 * Represents a successful [Constraint.verify] result, with [simplifiedConstraint] as a simplified passed constraint.
 */
class SatisfiedConstraintResult<out T : Constraint<*>>(val simplifiedConstraint: T) : ConstraintVerificationResult<T>()

/**
 * Represents a [Constraint.verify] result that indicates that constraint can never be violated, i.e., it is redundant.
 */
object RedundantConstraintResult : ConstraintVerificationResult<Nothing>()

/**
 * Represents a failed [Constraint.verify] result — means that constraint is always violated.
 */
object ViolatedConstraintResult : ConstraintVerificationResult<Nothing>()

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
data class InequalityConstraint(val simplifiedConstraints: List<SingleInequalityConstraint>) : Constraint<InequalityConstraint> {
    constructor(variable: Var, term: Term) : this(listOf(SingleInequalityConstraint(variable, term)))
    constructor(vararg pairs: Pair<Var, Term>) : this(pairs.map {
        SingleInequalityConstraint(it.first, it.second) }
    )

    override fun verify(substitution: Substitution): ConstraintVerificationResult<InequalityConstraint> =
        substitution.toUnificationState().verify(simplifiedConstraints)?.let { unificationResult ->
            val delta = unificationResult.substitutionDifference
            // If the substitution from unification does not differ from the current substitution,
            // it means that this constraint is always violated.
            if (delta.isEmpty()) {
                return ViolatedConstraintResult
            }

            val simplifiedConstraints = delta.map { SingleInequalityConstraint(it.key, it.value) }
            val singleConstraint = InequalityConstraint(simplifiedConstraints)

            singleConstraint.toSatisfiedConstraintResult()
        } ?: RedundantConstraintResult

    private fun UnificationState.verify(remainingSimplifiedConstraints: List<SingleInequalityConstraint>): UnificationState? {
        if (remainingSimplifiedConstraints.isEmpty()) {
            return this
        }

        val firstSingleConstraint = remainingSimplifiedConstraints.first()

        return unify(firstSingleConstraint.variable, firstSingleConstraint.term)
            ?.verify(remainingSimplifiedConstraints.subList(1, remainingSimplifiedConstraints.size))
    }

    override fun toString(): String = simplifiedConstraints.joinToString(separator = ", ", prefix = "[", postfix = "]")


    /**
     * Represents a simple inequality constraint - [variable] cannot be equal to [term].
     */
    data class SingleInequalityConstraint(val variable: Var, val term: Term) {
        override fun toString(): String = "$variable !== $term"
    }
}

/**
 * Creates a [SatisfiedConstraintResult] from [this].
 */
fun <T : Constraint<T>> T.toSatisfiedConstraintResult(): SatisfiedConstraintResult<T> = SatisfiedConstraintResult(this)
