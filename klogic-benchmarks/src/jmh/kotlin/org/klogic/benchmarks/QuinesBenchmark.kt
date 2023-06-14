package org.klogic.benchmarks

import org.klogic.utils.computing.findQuines
import org.klogic.utils.withEmptyContext
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.infra.Blackhole

open class QuinesBenchmark : AbstractKlogicBenchmark() {
    @Benchmark
    fun benchmarkQuines(bh: Blackhole) {
        bh.consume(withEmptyContext { findQuines(100) })
    }
}
