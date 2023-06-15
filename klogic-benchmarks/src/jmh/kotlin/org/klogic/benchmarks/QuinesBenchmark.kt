package org.klogic.benchmarks

import org.klogic.utils.computing.findQuines
import org.klogic.utils.withEmptyContext
import org.openjdk.jmh.annotations.Benchmark

open class QuinesBenchmark : AbstractKlogicBenchmark() {
    @Benchmark
    fun benchmarkQuines() {
        withEmptyContext { findQuines(100) }
    }
}
