package org.klogic.core

import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentHashMapOf
import kotlinx.collections.immutable.toPersistentHashMap
import org.klogic.unify.UnificationState
import org.klogic.unify.toUnificationState

/**
 * Represents an immutable association of [Var]s with arbitrary types and bounded [Term]s with corresponding types.
 */
@JvmInline
value class Substitution(private val innerSubstitution: PersistentMap<Var<*>, Term<*>> = persistentHashMapOf()) {
    constructor(map: Map<Var<*>, Term<*>>) : this(map.toPersistentHashMap())

    /**
     * Checks whether [InequalityConstraint] for [left] and [right] of the same type can be satisfied.
     *
     * It tries to [UnificationState.unify] [left] and [right] - if it fails, it means that [left] cannot be equal to
     * [right], i.e., this [InequalityConstraint] is redundant, and [RedundantConstraintResult] is returned.
     * Otherwise, if [UnificationState.substitutionDifference] is empty, it means that this constraint is violated,
     * and [ViolatedConstraintResult] is returned.
     * Else, [InequalityConstraint] is created from the [UnificationState.substitutionDifference],
     * and [SatisfiableConstraintResult] is returned.
     *
     * @see [UnificationState.unify] for details.
     */
    fun <T : Term<T>> ineq(left: Term<T>, right: Term<T>): ConstraintVerificationResult<InequalityConstraint> {
        return toUnificationState().unify(left, right)?.let { unificationState ->
            val delta = unificationState.substitutionDifference
            // If the substitution from unification does not differ from the current substitution,
            // it means that this constraint is violated.
            if (delta.isEmpty()) {
                return ViolatedConstraintResult
            }

            // Otherwise, this constraint can be satisfied, and we can simplify it according to calculated substitution delta.
            val simplifiedConstraints = delta.entries.map {
                InequalityConstraint.SingleInequalityConstraint(it.key, it.value.cast())
            }
            val singleConstraint = InequalityConstraint(simplifiedConstraints)

            SatisfiableConstraintResult(singleConstraint)
        } ?: RedundantConstraintResult // Failed unification means this constraint is never violated, i.e., it is redundant.
    }

    operator fun contains(key: Var<*>): Boolean = containsKey(key)

    private fun containsKey(key: Var<*>): Boolean = innerSubstitution.containsKey(key)

    fun containsValue(value: Term<*>): Boolean = innerSubstitution.containsValue(value)

    @Suppress("UNCHECKED_CAST")
    operator fun <T : Term<T>> get(key: Var<T>): Term<T>? = innerSubstitution[key] as Term<T>?

    fun isEmpty(): Boolean = innerSubstitution.isEmpty()

    operator fun plus(pair: Pair<Var<*>, Term<*>>): Substitution =
        (innerSubstitution.put(pair.first, pair.second)).toSubstitution()

    operator fun minus(other: Substitution): Substitution =
        other.innerSubstitution.keys.fold(innerSubstitution) { acc, key ->
            acc.remove(key)
        }.toSubstitution()

    override fun toString(): String = innerSubstitution.toString()

    companion object {
        private val EMPTY_SUBSTITUTION = Substitution()

        val empty: Substitution = EMPTY_SUBSTITUTION

        fun of(vararg pairs: Pair<Var<*>, Term<*>>): Substitution = Substitution(mapOf(*pairs))
    }
}

fun Map<Var<*>, Term<*>>.toSubstitution(): Substitution = Substitution(this)
