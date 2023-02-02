package org.klogic.utils

import org.klogic.core.ConsStream
import org.klogic.core.Symbol
import org.klogic.core.Symbol.Companion.toSymbol
import org.klogic.core.ThunksStream

internal fun <T> repeat(element: T): ThunksStream<T> = ThunksStream {
    ConsStream(element, repeat(element))
}

internal fun ones(): ThunksStream<Int> = repeat(1)

internal val one: Symbol = "1".toSymbol()
internal val two: Symbol = "2".toSymbol()
internal val three: Symbol = "3".toSymbol()
