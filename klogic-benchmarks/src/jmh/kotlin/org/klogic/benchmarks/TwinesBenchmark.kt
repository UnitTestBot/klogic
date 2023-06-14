package org.klogic.benchmarks

import org.klogic.utils.computing.findTwines
import org.klogic.utils.withEmptyContext
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.infra.Blackhole

open class TwinesBenchmark : AbstractKlogicBenchmark() {
    @Benchmark
    fun benchmarkTwines(bh: Blackhole) {
        bh.consume(withEmptyContext { findTwines(15) })
    }
}
