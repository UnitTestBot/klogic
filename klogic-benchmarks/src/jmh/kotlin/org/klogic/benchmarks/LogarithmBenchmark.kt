package org.klogic.benchmarks

import org.klogic.core.Term
import org.klogic.core.run
import org.klogic.utils.terms.OlegLogicNumber
import org.klogic.utils.terms.OlegLogicNumber.Companion.toOlegLogicNumber
import org.klogic.utils.terms.logᴼ
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.infra.Blackhole
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.OutputTimeUnit
import org.openjdk.jmh.runner.options.Options
import org.openjdk.jmh.runner.options.OptionsBuilder

open class LogarithmBenchmark : AbstractKlogicBenchmark() {
    @Benchmark
//    @BenchmarkMode(Mode.AverageTime)
//    @OutputTimeUnit(TimeUnit.SECONDS)
    fun benchmarkLog243base3(bh: Blackhole) {
        val n = 243u.toOlegLogicNumber()
        val b = 3u.toOlegLogicNumber()
        val r = 0u.toOlegLogicNumber()

        bh.consume(run(10, { q: Term<OlegLogicNumber> -> logᴼ(n, b, q, r) }))
    }
}
