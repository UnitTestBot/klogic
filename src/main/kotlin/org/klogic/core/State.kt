package org.klogic.core

import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toPersistentSet
import org.klogic.unify.toUnificationResult

typealias InequalityConstraints = PersistentSet<InequalityConstraint>

data class State(
    val substitution: Substitution,
    val inequalityConstraints: InequalityConstraints = persistentSetOf(),
    private var lastCreatedVariableIndex: Int = 0
) {
    constructor(map: Map<Var, Term>, inequalityConstraints: InequalityConstraints, lastCreatedVariableIndex: Int = 0) :
            this(Substitution(map), inequalityConstraints, lastCreatedVariableIndex)

    fun fresh(): Var = Var(lastCreatedVariableIndex++)

    fun extend(variable: Var, term: Term): State {
        require(variable !in substitution) {
            "Variable $variable already exists in substitution $substitution"
        }

        return State(substitution + (variable to term), inequalityConstraints, lastCreatedVariableIndex)
    }

    fun ineq(left: Term, right: Term): State =
        copy(inequalityConstraints = inequalityConstraints.add(InequalityConstraint(left, right)))

    fun check(): State? =
        inequalityConstraints.flatMap { inequalityConstraint ->
            val left = inequalityConstraint.left
            val right = inequalityConstraint.right

            toUnificationResult().unify(left, right)?.let { inequalityUnificationResult ->
                val delta = inequalityUnificationResult.substitutionDifference
                if (delta.isEmpty()) {
                    return@check null
                }

                delta.entries.map {
                    InequalityConstraint(it.key, it.value)
                }
            } ?: emptyList()
        }.toPersistentSet().let {
            copy(inequalityConstraints = it)
        }

    operator fun plus(pair: Pair<Var, Term>): State = extend(pair.first, pair.second)

    companion object {
        private val EMPTY_STATE: State = State(Substitution.empty)

        val empty: State = EMPTY_STATE
    }
}
