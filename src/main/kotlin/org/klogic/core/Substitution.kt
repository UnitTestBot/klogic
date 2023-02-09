package org.klogic.core

import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.minus
import kotlinx.collections.immutable.persistentHashMapOf
import kotlinx.collections.immutable.toPersistentMap
import org.klogic.unify.toUnificationState
import org.klogic.unify.UnificationState

/**
 * Represents an immutable association of [Var]s and corresponding [Term]s.
 */
data class Substitution(val innerSubstitution: PersistentMap<Var, Term> = persistentHashMapOf()) : Map<Var, Term> {
    constructor(map: Map<Var, Term>) : this(map as? PersistentMap<Var, Term> ?: map.toPersistentMap())

    /**
     * Checks whether [InequalityConstraint] of [left] and [right] can be satisfied.
     *
     * It tries to [UnificationState.unify] [left] and [right] - if it fails, it means that [left] cannot be equal to
     * [right], i.e., this [InequalityConstraint] is redundant, and [RedundantConstraintResult] is returned.
     * Otherwise, if [UnificationState.substitutionDifference] is empty, it means that this constraint is always violated,
     * and [ViolatedConstraintResult] is returned.
     * Else, [InequalityConstraint] is created from the [UnificationState.substitutionDifference], and [SatisfiedConstraintResult]
     * is returned.
     *
     * @see [UnificationState.unify] for details.
     */
    fun ineq(left: Term, right: Term): ConstraintVerificationResult<InequalityConstraint> {
        return toUnificationState().unify(left, right)?.let { unificationState ->
            val delta = unificationState.substitutionDifference
            // If the substitution from unification does not differ from the current substitution,
            // it means that this constraint is always violated.
            if (delta.isEmpty()) {
                return ViolatedConstraintResult
            }

            val simplifiedConstraints = delta.map { InequalityConstraint.SingleInequalityConstraint(it.key, it.value) }
            val singleConstraint = InequalityConstraint(simplifiedConstraints)

            SatisfiedConstraintResult(singleConstraint)
        } ?: RedundantConstraintResult
    }

    override val entries: Set<Map.Entry<Var, Term>> = innerSubstitution.entries
    override val keys: Set<Var> = innerSubstitution.keys
    override val size: Int = innerSubstitution.size
    override val values: Collection<Term> = innerSubstitution.values

    override fun containsKey(key: Var): Boolean = innerSubstitution.containsKey(key)

    override fun containsValue(value: Term): Boolean = innerSubstitution.containsValue(value)

    override fun get(key: Var): Term? = innerSubstitution[key]

    override fun isEmpty(): Boolean = innerSubstitution.isEmpty()

    operator fun plus(pair: Pair<Var, Term>): Substitution = (innerSubstitution + pair).toSubstitution()
    operator fun minus(other: Substitution): Substitution = (innerSubstitution - other.keys).toSubstitution()

    override fun toString(): String = innerSubstitution.toString()

    companion object {
        @Suppress("PrivatePropertyName")
        private val EMPTY_SUBSTITUTION = Substitution()

        val empty: Substitution = EMPTY_SUBSTITUTION

        fun of(vararg pairs: Pair<Var, Term>): Substitution = Substitution(mapOf(*pairs))
    }
}

fun Map<Var, Term>.toSubstitution(): Substitution = Substitution(this)
