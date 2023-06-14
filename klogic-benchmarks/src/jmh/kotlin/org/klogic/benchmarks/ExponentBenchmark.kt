package org.klogic.benchmarks

import org.klogic.core.Term
import org.klogic.core.run
import org.klogic.utils.terms.OlegLogicNumber
import org.klogic.utils.terms.OlegLogicNumber.Companion.toOlegLogicNumber
import org.klogic.utils.terms.expᴼ
import org.klogic.utils.terms.mulᴼ
import org.openjdk.jmh.infra.Blackhole
import org.openjdk.jmh.runner.options.Options
import org.openjdk.jmh.runner.options.OptionsBuilder
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.Measurement
import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.annotations.OutputTimeUnit
import org.openjdk.jmh.annotations.Warmup
import java.util.concurrent.TimeUnit
import java.util.concurrent.*

open class ExponentBenchmark : AbstractKlogicBenchmark() {
    @Benchmark
    @Measurement(iterations = 5, time = 1)
    fun benchmark3pow5(bh: Blackhole) {
        val base = 3u.toOlegLogicNumber()
        val power = 5u.toOlegLogicNumber()

        bh.consume(run(10, { result: Term<OlegLogicNumber> -> expᴼ(base, power, result) }))
    }

    @Benchmark
//    @BenchmarkMode(Mode.AverageTime)
//    @OutputTimeUnit(TimeUnit.SECONDS)
    fun benchmarkMul7pow2(bh: Blackhole) {
        val a = 7u.toOlegLogicNumber()
        val b = 2u.toOlegLogicNumber()

        bh.consume(run(10, { result: Term<OlegLogicNumber> -> expᴼ(a, b, result) }))
    }

    @Benchmark
    fun benchmarkMul127x127(bh: Blackhole) {
        val a = 127u.toOlegLogicNumber()
        val b = 127u.toOlegLogicNumber()

        bh.consume(run(10, { result: Term<OlegLogicNumber> -> mulᴼ(a, b, result) }))
    }
    @Benchmark
    fun benchmarkMul255x255(bh: Blackhole) {
        val a = 255u.toOlegLogicNumber()
        val b = 255u.toOlegLogicNumber()

        bh.consume(run(10, { result: Term<OlegLogicNumber> -> mulᴼ(a, b, result) }))
    }
}
