package org.klogic.core

import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.minus
import kotlinx.collections.immutable.persistentHashMapOf
import kotlinx.collections.immutable.toPersistentMap

data class Substitution(val innerSubstitution: PersistentMap<Var, Term> = persistentHashMapOf()) : Map<Var, Term> {
    constructor(map: Map<Var, Term>) : this(map as? PersistentMap<Var, Term> ?: map.toPersistentMap())

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
