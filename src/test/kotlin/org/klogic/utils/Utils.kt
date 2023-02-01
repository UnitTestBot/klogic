package org.klogic.utils

import org.klogic.core.ConsStream
import org.klogic.core.ThunksStream

internal fun <T> repeat(element: T): ThunksStream<T> = ThunksStream {
    ConsStream(element, repeat(element))
}

internal fun ones(): ThunksStream<Int> = repeat(1)
