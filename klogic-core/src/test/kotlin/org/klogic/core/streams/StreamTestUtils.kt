package org.klogic.core.streams

import org.klogic.core.ConsStream
import org.klogic.core.ThunkStream

internal fun <T> repeat(element: T): ThunkStream<T> = ThunkStream {
    ConsStream(element, repeat(element))
}

internal fun ones(): ThunkStream<Int> = repeat(1)
