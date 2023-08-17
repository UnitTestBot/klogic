package org.klogic.benchmarks

import org.klogic.utils.computing.findThrines
import org.klogic.utils.withEmptyContext
import org.openjdk.jmh.annotations.Benchmark

open class ThrinesBenchmark : AbstractKlogicBenchmark() {
    @Benchmark
    fun benchmarkThrines() {
        withEmptyContext { findThrines(2) }
    }
}
