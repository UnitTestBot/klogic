@file:Suppress("ObjectPropertyName", "unused")

package org.klogic.utils

import org.klogic.core.*
import org.klogic.core.Var.Companion.createTypedVar
import org.klogic.utils.terms.LogicList
import org.klogic.utils.terms.Symbol
import org.klogic.utils.terms.Symbol.Companion.toSymbol

private val digitSymbols: List<Symbol> = (0..9).map { it.toString() }.map { it.toSymbol() }

val `0`: Symbol = digitSymbols[0]
val `1`: Symbol = digitSymbols[1]
val `2`: Symbol = digitSymbols[2]
val `3`: Symbol = digitSymbols[3]
val `4`: Symbol = digitSymbols[4]
val `5`: Symbol = digitSymbols[5]
val `6`: Symbol = digitSymbols[6]
val `7`: Symbol = digitSymbols[7]
val `8`: Symbol = digitSymbols[8]
val `9`: Symbol = digitSymbols[9]

// TODO introduce a state for tests for creating fresh variables
private val symbolVariables: List<Var<Symbol>> = (0..7).map { it.createTypedVar() }

val q: Var<Symbol> = symbolVariables[0]
val x: Var<Symbol> = symbolVariables[1]
val y: Var<Symbol> = symbolVariables[2]
val z: Var<Symbol> = symbolVariables[3]
val a: Var<Symbol> = symbolVariables[4]
val b: Var<Symbol> = symbolVariables[5]
val c: Var<Symbol> = symbolVariables[6]
val d: Var<Symbol> = symbolVariables[7]

private val listSymbolVariables: List<Var<LogicList<Symbol>>> = (0..7).map { it.createTypedVar() }

val listQ: Var<LogicList<Symbol>> = listSymbolVariables[0]
val listX: Var<LogicList<Symbol>> = listSymbolVariables[1]
val listY: Var<LogicList<Symbol>> = listSymbolVariables[2]
val listZ: Var<LogicList<Symbol>> = listSymbolVariables[3]
val listA: Var<LogicList<Symbol>> = listSymbolVariables[4]
val listB: Var<LogicList<Symbol>> = listSymbolVariables[5]
val listC: Var<LogicList<Symbol>> = listSymbolVariables[6]
val listD: Var<LogicList<Symbol>> = listSymbolVariables[7]

val <T : Term<T>> List<ReifiedTerm<T>>.singleReifiedTerm: Term<T>
    get() = single().term
val List<ReifiedTerm<*>>.singleReifiedTermConstraints: Set<Constraint<*>>
    get() = single().constraints

inline fun <R> withEmptyContext(block: RelationalContext.() -> R): R = RelationalContext().useWith { block() }
