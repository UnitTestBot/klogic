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
        val n = 14u.toOlegLogicNumber()
        val b = 2u.toOlegLogicNumber()
        val q = 3u.toOlegLogicNumber()

        withEmptyContext {
            run(1, { r: Term<OlegLogicNumber> ->
                logᴼ(n, b, q, r)
            })
        }
    }
}
