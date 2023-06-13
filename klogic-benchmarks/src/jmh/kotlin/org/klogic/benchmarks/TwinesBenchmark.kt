package org.klogic.benchmarks

import org.klogic.core.RelationalContext
import org.klogic.core.useWith
import org.klogic.utils.computing.findTwines
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.infra.Blackhole

open class TwinesBenchmark : AbstractKlogicBenchmark() {
    @Benchmark
    fun benchmarkTwines(bh: Blackhole) {
        bh.consume(RelationalContext().useWith { findTwines(15) })
    }
}
