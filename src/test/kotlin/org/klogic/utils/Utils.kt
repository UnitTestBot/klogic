package org.klogic.utils

import org.klogic.core.ConsStream
import org.klogic.core.CurStream
import org.klogic.core.SequenceStream.Companion.lazyStream
import org.klogic.core.ThunksStream
import org.klogic.core.toSequenceStream

/*internal fun <T> repeat(element: T): ThunksStream<T> = ThunksStream {
    ConsStream(element, repeat(element))
}*/
internal fun <T> repeat(element: T): CurStream<T> = generateSequence(element) { element }.toSequenceStream()

internal fun ones(): CurStream<Int> = repeat(1)
