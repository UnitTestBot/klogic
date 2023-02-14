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
class SatisfiableConstraintResult<out T : Constraint<*>>(val simplifiedConstraint: T) : ConstraintVerificationResult<T>()

/**
 * Represents a [Constraint.verify] result that indicates that constraint can never be violated, i.e., it is redundant.
 */
object RedundantConstraintResult : ConstraintVerificationResult<Nothing>()

/**
 * Represents a failed [Constraint.verify] result — means that constraint is violated.
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
class InequalityConstraint internal constructor(private val simplifiedConstraints: List<SingleInequalityConstraint<out Term>>) : Constraint<InequalityConstraint> {
    override fun verify(substitution: Substitution): ConstraintVerificationResult<InequalityConstraint> =
        substitution.toUnificationState().verify(simplifiedConstraints)?.let { unificationResult ->
            val delta = unificationResult.substitutionDifference
            // If the substitution from unification does not differ from the current substitution,
            // it means that this constraint is violated.
            if (delta.isEmpty()) {
                return ViolatedConstraintResult
            }


            val simplifiedConstraints = delta.entries.map {
                SingleInequalityConstraint(it.key, it.value)
            }


//            val simplifiedConstraints = delta.map { SingleInequalityConstraint(it.key, it.value) }
            val singleConstraint = InequalityConstraint(simplifiedConstraints)

            singleConstraint.toSatisfiedConstraintResult()
        } ?: RedundantConstraintResult

    private fun UnificationState.verify(remainingSimplifiedConstraints: List<SingleInequalityConstraint<out Term>>): UnificationState? {
        if (remainingSimplifiedConstraints.isEmpty()) {
            return this
        }

        val firstSingleConstraint = remainingSimplifiedConstraints.first()

        return unify(firstSingleConstraint.variable, firstSingleConstraint.term)
            ?.verify(remainingSimplifiedConstraints.subList(1, remainingSimplifiedConstraints.size))
    }



    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as InequalityConstraint

        if (simplifiedConstraints != other.simplifiedConstraints) return false

        return true
    }

    override fun hashCode(): Int {
        return simplifiedConstraints.hashCode()
    }

    override fun toString(): String = simplifiedConstraints.joinToString(separator = ", ", prefix = "[", postfix = "]")

    /**
     * Represents a simple inequality constraint - [variable] cannot be equal to [term].
     */
    data class SingleInequalityConstraint<T : Term>(val variable: Var<T>, val term: T) {
        override fun toString(): String = "$variable !== $term"

        companion object {
//            private fun unsafeOf(variable: Var<out Any>, term: Term<out Any>): SingleInequalityConstraint<Any> =
//                SingleInequalityConstraint(variable, term)
        }
    }

    companion object {
        fun of(vararg pairs: Pair<Var<out Term>, Term>): InequalityConstraint {
            val singleInequalityConstraints = pairs.map {
                SingleInequalityConstraint(it.first.cast(), it.second)
            }

            return InequalityConstraint(singleInequalityConstraints)
        }
    }
}

/**
 * Creates a [SatisfiableConstraintResult] from [this].
 */
fun <T : Constraint<T>> T.toSatisfiedConstraintResult(): SatisfiableConstraintResult<T> = SatisfiableConstraintResult(this)
