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
data class Substitution(private val innerSubstitution: PersistentMap<Var<out Any>, Term<out Any>> = persistentHashMapOf()) {
    constructor(map: Map<Var<out Any>, Term<out Any>>) : this(map.toPersistentHashMap())

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
            val simplifiedConstraints = delta.entries.map { InequalityConstraint.SingleInequalityConstraint(it.key, it.value) }
            val singleConstraint = InequalityConstraint(simplifiedConstraints)

            SatisfiableConstraintResult(singleConstraint)
        } ?: RedundantConstraintResult // Failed unification means this constraint is never violated, i.e., it is redundant.
    }

//    override val entries: Set<Map.Entry<Var<out Any>, Term<out Any>>> = innerSubstitution.entries
//    override val keys: Set<Var<out Any>> = innerSubstitution.keys
//    override val size: Int = innerSubstitution.size
//    override val values: Collection<Term<out Any>> = innerSubstitution.values

    operator fun contains(key: Var<out Any>): Boolean = containsKey(key)

    fun containsKey(key: Var<out Any>): Boolean = innerSubstitution.containsKey(key)

    fun containsValue(value: Term<out Any>): Boolean = innerSubstitution.containsValue(value)

    @Suppress("UNCHECKED_CAST")
    operator fun <T : Any> get(key: Var<T>): Term<T>? = innerSubstitution[key] as Term<T>?

    fun isEmpty(): Boolean = innerSubstitution.isEmpty()

    operator fun plus(pair: Pair<Var<out Any>, Term<out Any>>): Substitution = (innerSubstitution + pair).toSubstitution()

//    operator fun minus(other: Substitution): Substitution = (innerSubstitution - other.keys).toSubstitution()

    override fun toString(): String = innerSubstitution.toString()

    companion object {
        @Suppress("PrivatePropertyName")
        private val EMPTY_SUBSTITUTION = Substitution()

        val empty: Substitution = EMPTY_SUBSTITUTION

        fun of(vararg pairs: Pair<Var<out Any>, Term<out Any>>): Substitution = Substitution(mapOf(*pairs))
    }
}

// TODO add more docs
// This class cannot extend Map interface because of `get` operator limitations
interface MapOfVariablesToTermsOfTheSameType {
    val keys: Set<Var<out Any>>
    val values: Collection<Term<out Any>>
    val entries: Set<Map.Entry<Var<out Any>, Term<out Any>>>

    operator fun <T: Any> get(key: Var<T>): Term<T>?
    operator fun contains(key: Var<out Any>): Boolean
    fun containsValue(value: Term<out Any>): Boolean

    operator fun <T: Any> plus(pair: Pair<Var<T>, Term<T>>): MapOfVariablesToTermsOfTheSameType

    operator fun minus(other: MapOfVariablesToTermsOfTheSameType): MapOfVariablesToTermsOfTheSameType

    fun isEmpty(): Boolean = entries.isEmpty()
}
interface MutableMapOfVariablesToTermsOfTheSameType : MapOfVariablesToTermsOfTheSameType {
    override val keys: MutableSet<Var<out Any>>
    override val values: MutableCollection<Term<out Any>>
    override val entries: MutableSet<MutableMap.MutableEntry<Var<out Any>, Term<out Any>>>

    operator fun <T: Any> plusAssign(pair: Pair<Var<T>, Term<T>>)

    override operator fun minus(other: MapOfVariablesToTermsOfTheSameType): MutableMapOfVariablesToTermsOfTheSameType
}

class MutableMapOfVariablesToTermsOfTheSameTypeImpl(private val innerMap: MutableMap<Var<out Any>, Term<out Any>>) : MutableMapOfVariablesToTermsOfTheSameType {
    constructor() : this(mutableMapOf())

    constructor(other: MutableMapOfVariablesToTermsOfTheSameTypeImpl) : this(other.innerMap.toMutableMap())

    override val keys: MutableSet<Var<out Any>> = innerMap.keys
    override val values: MutableCollection<Term<out Any>> = innerMap.values
    override val entries: MutableSet<MutableMap.MutableEntry<Var<out Any>, Term<out Any>>> = innerMap.entries

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> get(key: Var<T>): Term<T>? = innerMap[key] as? Term<T>

    override fun contains(key: Var<out Any>): Boolean = get(key) != null

    override fun containsValue(value: Term<out Any>): Boolean = innerMap.containsValue(value)

    override fun <T : Any> plus(pair: Pair<Var<T>, Term<T>>): MapOfVariablesToTermsOfTheSameType {
        val copy = MutableMapOfVariablesToTermsOfTheSameTypeImpl(this)
        copy += pair

        return copy
    }

    override fun <T : Any> plusAssign(pair: Pair<Var<T>, Term<T>>) {
        innerMap += pair
    }

    override fun minus(other: MapOfVariablesToTermsOfTheSameType): MutableMapOfVariablesToTermsOfTheSameType {
        val copy = MutableMapOfVariablesToTermsOfTheSameTypeImpl(this)
        copy.innerMap -= other.keys

        return copy
    }
}

fun Map<Var<out Any>, Term<out Any>>.toSubstitution(): Substitution = Substitution(this)
