package org.klogic.benchmarks

import org.klogic.core.RelationalContext
import org.klogic.core.Term
import org.klogic.core.run
import org.klogic.core.useWith
import org.klogic.utils.terms.OlegLogicNumber
import org.klogic.utils.terms.OlegLogicNumber.Companion.toOlegLogicNumber
import org.klogic.utils.terms.expᴼ
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.infra.Blackhole

open class ExponentBenchmark : AbstractKlogicBenchmark() {
    @Benchmark
    fun benchmarkExponent(bh: Blackhole) {
        val base = 3u.toOlegLogicNumber()
        val power = 5u.toOlegLogicNumber()

        bh.consume(run(10, { result: Term<OlegLogicNumber> -> RelationalContext().useWith { expᴼ(base, power, result) } }))
    }
}
