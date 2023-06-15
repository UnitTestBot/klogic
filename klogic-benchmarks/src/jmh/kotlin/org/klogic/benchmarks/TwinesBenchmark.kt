package org.klogic.benchmarks

import org.klogic.utils.computing.findTwines
import org.klogic.utils.withEmptyContext
import org.openjdk.jmh.annotations.Benchmark

open class TwinesBenchmark : AbstractKlogicBenchmark() {
    @Benchmark
    fun benchmarkTwines() {
        withEmptyContext { findTwines(15) }
    }
}
