package org.klogic.benchmarks

import org.klogic.core.Term
import org.klogic.utils.terms.OlegLogicNumber
import org.klogic.utils.terms.OlegLogicNumber.Companion.toOlegLogicNumber
import org.klogic.utils.terms.logᴼ
import org.klogic.utils.withEmptyContext
import org.openjdk.jmh.annotations.Benchmark

open class LogarithmBenchmark : AbstractKlogicBenchmark() {
    @Benchmark
    fun benchmarkLogarithm() {
        val n = 243u.toOlegLogicNumber()
        val b = 3u.toOlegLogicNumber()
        val r = 0u.toOlegLogicNumber()

        withEmptyContext {
            run(1, { q: Term<OlegLogicNumber> ->
                logᴼ(n, b, q, r)
            })
        }
    }
}
