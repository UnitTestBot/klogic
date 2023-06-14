package org.klogic.benchmarks

import org.klogic.core.Term
import org.klogic.core.run
import org.klogic.utils.terms.OlegLogicNumber
import org.klogic.utils.terms.OlegLogicNumber.Companion.toOlegLogicNumber
import org.klogic.utils.terms.logᴼ
import org.klogic.utils.withEmptyContext
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.infra.Blackhole

open class LogarithmBenchmark : AbstractKlogicBenchmark() {
    @Benchmark
    fun benchmarkLogarithm(bh: Blackhole) {
        val n = 14u.toOlegLogicNumber()
        val b = 2u.toOlegLogicNumber()
        val q = 3u.toOlegLogicNumber()

        bh.consume(run(10, { r: Term<OlegLogicNumber> ->
            withEmptyContext { logᴼ(n, b, q, r) }
        }))
    }
}
