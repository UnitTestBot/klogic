package org.klogic.core

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import org.klogic.unify.occurs
import org.klogic.unify.walk

typealias InequalityConstraints = PersistentList<InequalityConstraint>

data class State(
    val substitution: Substitution,
    val inequalityConstraints: InequalityConstraints = persistentListOf(),
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

    @Suppress("NAME_SHADOWING")
    fun unify(left: Term, right: Term): State? {
        val left = walk(left, substitution)
        val right = walk(right, substitution)

        return when (left) {
            is Var -> {
                when (right) {
                    is Var -> {
                        if (left == right) {
                            this
                        } else {
                            this + (left to right)
                        }
                    }
                    is Symbol, is Cons, Nil -> {
                        if (occurs(left, right)) {
                            null
                        } else {
                            this + (left to right)
                        }
                    }
                }
            }
            is Cons -> when (right) {
                is Cons -> {
                    unify(left.head, right.head)?.unify(left.tail, right.tail)
                }
                is Var -> this.unify(right, left)
                is Symbol, Nil -> null
            }
            Nil -> when (right) {
                is Var -> this.unify(right, left)
                Nil -> this
                is Symbol, is Cons -> null
            }
            is Symbol -> when (right) {
                is Var -> this.unify(right, left)
                is Symbol -> if (left == right) this else null
                is Cons, Nil -> null
            }
        }
    }

    fun ineq(left: Term, right: Term): State =
        copy(inequalityConstraints = inequalityConstraints.add(InequalityConstraint(left, right)))

    operator fun plus(pair: Pair<Var, Term>): State = extend(pair.first, pair.second)

    companion object {
        private val EMPTY_STATE: State = State(Substitution.empty)

        val empty: State = EMPTY_STATE
    }
}
