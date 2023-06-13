package org.klogic.benchmarks

import org.klogic.core.RelationalContext
import org.klogic.core.useWith
import org.klogic.utils.computing.findThrines
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.infra.Blackhole

open class ThrinesBenchmark : AbstractKlogicBenchmark() {
    @Benchmark
    fun benchmarkThrines(bh: Blackhole) {
        bh.consume(RelationalContext().useWith { findThrines(3) })
    }
}
