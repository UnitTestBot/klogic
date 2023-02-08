@file:Suppress("ObjectPropertyName", "unused")

package org.klogic.utils

import org.klogic.core.ConsStream
import org.klogic.core.Constraint
import org.klogic.core.ReifiedTerm
import org.klogic.core.Symbol
import org.klogic.core.Symbol.Companion.toSymbol
import org.klogic.core.Term
import org.klogic.core.ThunkStream
import org.klogic.core.Var
import org.klogic.core.Var.Companion.toVar

internal fun <T> repeat(element: T): ThunkStream<T> = ThunkStream {
    ConsStream(element, repeat(element))
}

internal fun ones(): ThunkStream<Int> = repeat(1)

private val digitSymbols: List<Symbol> = (0..9).map { it.toString() }.map { it.toSymbol() }

internal val `0`: Symbol = digitSymbols[0]
internal val `1`: Symbol = digitSymbols[1]
internal val `2`: Symbol = digitSymbols[2]
internal val `3`: Symbol = digitSymbols[3]
internal val `4`: Symbol = digitSymbols[4]
internal val `5`: Symbol = digitSymbols[5]
internal val `6`: Symbol = digitSymbols[6]
internal val `7`: Symbol = digitSymbols[7]
internal val `8`: Symbol = digitSymbols[8]
internal val `9`: Symbol = digitSymbols[9]

// TODO introduce a state for tests for creating fresh variables
private val variables: List<Var> = (0..7).map { it.toVar() }

internal val q: Var = variables[0]
internal val x: Var = variables[1]
internal val y: Var = variables[2]
internal val z: Var = variables[3]
internal val a: Var = variables[4]
internal val b: Var = variables[5]
internal val c: Var = variables[6]
internal val d: Var = variables[7]

val List<ReifiedTerm>.singleReifiedTerm: Term
    get() = single().term
val List<ReifiedTerm>.singleReifiedTermConstraints: Set<Constraint>
    get() = single().constraints
