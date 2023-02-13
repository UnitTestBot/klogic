package org.klogic.core

import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.minus
import kotlinx.collections.immutable.persistentHashMapOf
import kotlinx.collections.immutable.toPersistentHashMap
import org.klogic.unify.toUnificationState
import org.klogic.unify.UnificationState

/**
 * Represents an immutable association of [Var]s and corresponding [Term]s.
 */
data class Substitution(val innerSubstitution: PersistentMap<Var<Any>, Term<Any>> = persistentHashMapOf()) : Map<Var<Any>, Term<Any>> {
    constructor(map: Map<Var<Any>, Term<Any>>) : this(map.toPersistentHashMap())

    /**
     * Checks whether [InequalityConstraint] of [left] and [right] can be satisfied.
     *
     * It tries to [UnificationState.unify] [left] and [right] - if it fails, it means that [left] cannot be equal to
     * [right], i.e., this [InequalityConstraint] is redundant, and [RedundantConstraintResult] is returned.
     * Otherwise, if [UnificationState.substitutionDifference] is empty, it means that this constraint is violated,
     * and [ViolatedConstraintResult] is returned.
     * Else, [InequalityConstraint] is created from the [UnificationState.substitutionDifference], and [SatisfiableConstraintResult]
     * is returned.
     *
     * @see [UnificationState.unify] for details.
     */
    fun <T : Any> ineq(left: Term<T>, right: Term<T>): ConstraintVerificationResult<InequalityConstraint> {
        return toUnificationState().unify(left, right)?.let { unificationState ->
            val delta = unificationState.substitutionDifference
            // If the substitution from unification does not differ from the current substitution,
            // it means that this constraint is violated.
            if (delta.isEmpty()) {
                return ViolatedConstraintResult
            }

            // Otherwise, this constraint can be satisfied, and we can simplify it according to calculated substitution delta.
            val simplifiedConstraints = delta.map { InequalityConstraint.SingleInequalityConstraint(it.key, it.value) }
            val singleConstraint = InequalityConstraint(simplifiedConstraints)

            SatisfiableConstraintResult(singleConstraint)
        } ?: RedundantConstraintResult // Failed unification means this constraint is never violated, i.e., it is redundant.
    }

    override val entries: Set<Map.Entry<Var<Any>, Term<Any>>> = innerSubstitution.entries
    override val keys: Set<Var<Any>> = innerSubstitution.keys
    override val size: Int = innerSubstitution.size
    override val values: Collection<Term<Any>> = innerSubstitution.values

    override fun containsKey(key: Var<Any>): Boolean = innerSubstitution.containsKey(key)

    override fun containsValue(value: Term<Any>): Boolean = innerSubstitution.containsValue(value)

    override fun get(key: Var<Any>): Term<Any>? = innerSubstitution[key]

    override fun isEmpty(): Boolean = innerSubstitution.isEmpty()

    operator fun plus(pair: Pair<Var<Any>, Term<Any>>): Substitution = (innerSubstitution + pair).toSubstitution()
    operator fun minus(other: Substitution): Substitution = (innerSubstitution - other.keys).toSubstitution()

    override fun toString(): String = innerSubstitution.toString()

    companion object {
        @Suppress("PrivatePropertyName")
        private val EMPTY_SUBSTITUTION = Substitution()

        val empty: Substitution = EMPTY_SUBSTITUTION

        fun of(vararg pairs: Pair<Var<Any>, Term<Any>>): Substitution = Substitution(mapOf(*pairs))
    }
}

fun Map<Var<Any>, Term<Any>>.toSubstitution(): Substitution = Substitution(this)
