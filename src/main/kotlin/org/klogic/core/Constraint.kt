package org.klogic.core

sealed class Constraint

data class InequalityConstraint(val left: Term, val right: Term) : Constraint() {
    override fun toString(): String = "$left !== $right"
}
