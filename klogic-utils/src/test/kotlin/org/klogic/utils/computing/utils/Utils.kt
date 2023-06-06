package org.klogic.utils.computing.utils

import org.klogic.core.Term
import org.klogic.utils.computing.*
import org.klogic.utils.terms.Symbol

internal fun Term<Symbol>.toSymb(): Symb = Symb(this)

internal val quoteSymb: Symb = quoteSymbol.toSymb()
internal val listSymb: Symb = listSymbol.toSymb()
internal val lambdaSymb: Symb = lambdaSymbol.toSymb()

internal val doubleQuote: Seq = Seq(quoteSymb, quoteSymb)

internal fun innerPart(variableSymb: Symb): Seq = Seq(
    listSymb,
    doubleQuote,
    variableSymb
)

internal fun repeatedPartInQuines(variableSymb: Symb): Seq = Seq(
    listSymb,
    variableSymb,
    innerPart(variableSymb)
)

internal fun Seq.extractVariable(): Symb = toList().first { it is Symb && it.s.isVar() } as Symb
