package org.klogic.core

import org.klogic.unify.occurs
import org.klogic.unify.walk

class State(val substitution: Substitution, private var lastCreatedVariableIndex: Int = 0) {
    constructor(map: Map<Var, Term>, lastCreatedVariableIndex: Int = 0) :
            this(Substitution(map), lastCreatedVariableIndex)

    fun fresh(): Var = Var(lastCreatedVariableIndex++)

    fun extend(variable: Var, term: Term): State {
        require(variable !in substitution) {
            "Variable $variable already exists in substitution $substitution"
        }

        return State(substitution + (variable to term), lastCreatedVariableIndex)
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

    operator fun plus(pair: Pair<Var, Term>): State = extend(pair.first, pair.second)

    override fun toString(): String = "State(substitution = $substitution, lastIndex = $lastCreatedVariableIndex)"
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as State

        if (substitution != other.substitution) return false
        if (lastCreatedVariableIndex != other.lastCreatedVariableIndex) return false

        return true
    }

    override fun hashCode(): Int {
        var result = substitution.hashCode()
        result = 31 * result + lastCreatedVariableIndex
        return result
    }


    companion object {
        private val EMPTY_STATE: State = State(Substitution.empty)

        val empty: State = EMPTY_STATE
    }
}
