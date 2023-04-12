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
    private val simplifiedConstraints: List<SingleInequalityConstraint<*>>
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
            val simplifiedConstraints = delta.entries.map {
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

        return unify(firstSingleConstraint.variable, firstSingleConstraint.term.cast())
            ?.verify(remainingSimplifiedConstraints.subList(1, remainingSimplifiedConstraints.size))
    }

    override fun toString(): String = simplifiedConstraints.joinToString(separator = ", ", prefix = "[", postfix = "]")

    companion object {
        fun <T : Term<T>> of(variable: Var<T>, term: Term<T>): InequalityConstraint =
            InequalityConstraint(listOf(SingleInequalityConstraint(variable, term)))

        // This method does not check that variable type equals to type of corresponding term,
        // as it has to be in SingleInequalityConstraint, so it should be used very carefully
        private fun unsafeOf(vararg pairs: Pair<Var<out Term<*>>, Term<*>>): InequalityConstraint {
            val singleInequalityConstraints = pairs.map {
                SingleInequalityConstraint(it.first, it.second.cast())
            }

            return InequalityConstraint(singleInequalityConstraints)
        }
    }


    /**
     * Represents a simple inequality constraint - [variable] cannot be equal to [term] of the same type.
     */
    data class SingleInequalityConstraint<T : Term<T>>(val variable: Var<T>, val term: Term<T>) {
        override fun toString(): String = "$variable !== $term"
    }
}

/**
 * Creates a [SatisfiableConstraintResult] from [this].
 */
fun <T : Constraint<T>> T.toSatisfiedConstraintResult(): SatisfiableConstraintResult<T> =
    SatisfiableConstraintResult(this)
