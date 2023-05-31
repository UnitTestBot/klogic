package org.klogic.benchmarks

import org.klogic.core.Term
import org.klogic.core.run
import org.klogic.utils.terms.OlegLogicNumber
import org.klogic.utils.terms.OlegLogicNumber.Companion.toOlegLogicNumber
import org.klogic.utils.terms.expᴼ
import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole

open class ExponentBenchmark : AbstractKlogicBenchmark() {
    @Benchmark
    fun benchmarkExponent(bh: Blackhole) {
        val base = 3u.toOlegLogicNumber()
        val power = 5u.toOlegLogicNumber()

        bh.consume(run(10, { result: Term<OlegLogicNumber> -> expᴼ(base, power, result) }))
    }
}
