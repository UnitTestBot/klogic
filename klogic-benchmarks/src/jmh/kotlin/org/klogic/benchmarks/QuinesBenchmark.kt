package org.klogic.benchmarks

import org.klogic.core.RelationalContext
import org.klogic.core.useWith
import org.klogic.utils.computing.findQuines
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.infra.Blackhole

open class QuinesBenchmark : AbstractKlogicBenchmark() {
    @Benchmark
    fun benchmarkQuines(bh: Blackhole) {
        bh.consume(RelationalContext().useWith { findQuines(100) })
    }
}
