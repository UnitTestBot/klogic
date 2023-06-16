package org.klogic.core.streams

import org.klogic.core.ConsStream
import org.klogic.core.RelationalContext
import org.klogic.core.ThunkStream

context(RelationalContext)
internal fun <T> repeat(element: T): ThunkStream<T> = ThunkStream {
    ConsStream(element, repeat(element))
}

context(RelationalContext)
internal fun ones(): ThunkStream<Int> = repeat(1)
