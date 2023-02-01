package org.klogic.utils

import org.klogic.core.ConsStream
import org.klogic.core.KanrenStream
import org.klogic.core.Stream.Companion.lazyStream

internal fun <T> repeat(element: T): KanrenStream<T> = lazyStream {
    ConsStream(element, repeat(element))
}

internal fun ones(): KanrenStream<Int> = repeat(1)
