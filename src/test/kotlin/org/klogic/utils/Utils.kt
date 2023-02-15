@file:Suppress("ObjectPropertyName", "unused")

package org.klogic.utils

import org.klogic.core.ConsStream
import org.klogic.core.Constraint
import org.klogic.core.ReifiedTerm
import org.klogic.core.Term
import org.klogic.core.ThunkStream
import org.klogic.core.Var
import org.klogic.core.Var.Companion.createTypedVar
import org.klogic.terms.RecursiveList
import org.klogic.terms.Symbol
import org.klogic.terms.Symbol.Companion.toSymbol

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
private val symbolVariables: List<Var<Symbol>> = (0..7).map { it.createTypedVar() }

internal val q: Var<Symbol> = symbolVariables[0]
internal val x: Var<Symbol> = symbolVariables[1]
internal val y: Var<Symbol> = symbolVariables[2]
internal val z: Var<Symbol> = symbolVariables[3]
internal val a: Var<Symbol> = symbolVariables[4]
internal val b: Var<Symbol> = symbolVariables[5]
internal val c: Var<Symbol> = symbolVariables[6]
internal val d: Var<Symbol> = symbolVariables[7]

private val listSymbolVariables: List<Var<RecursiveList<Symbol>>> = (0..7).map { it.createTypedVar() }

internal val listQ: Var<RecursiveList<Symbol>> = listSymbolVariables[0]
internal val listX: Var<RecursiveList<Symbol>> = listSymbolVariables[1]
internal val listY: Var<RecursiveList<Symbol>> = listSymbolVariables[2]
internal val listZ: Var<RecursiveList<Symbol>> = listSymbolVariables[3]
internal val listA: Var<RecursiveList<Symbol>> = listSymbolVariables[4]
internal val listB: Var<RecursiveList<Symbol>> = listSymbolVariables[5]
internal val listC: Var<RecursiveList<Symbol>> = listSymbolVariables[6]
internal val listD: Var<RecursiveList<Symbol>> = listSymbolVariables[7]

val List<ReifiedTerm<out Any>>.singleReifiedTerm: Term<out Any>
    get() = single().term
val List<ReifiedTerm<out Any>>.singleReifiedTermConstraints: Set<Constraint<*>>
    get() = single().constraints
