package org.klogic.benchmarks

import org.klogic.core.RelationalContext
import org.klogic.core.Term
import org.klogic.core.run
import org.klogic.core.useWith
import org.klogic.utils.terms.*
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.infra.Blackhole

open class SortingBenchmark : AbstractKlogicBenchmark() {
    @Benchmark
    fun benchmarkSorting(bh: Blackhole) {
        val numbers = listOf(4, 3, 2, 1).map { it.toPeanoLogicNumber() }.toLogicList()

        bh.consume(run(1, { sorted: Term<LogicList<PeanoLogicNumber>> -> RelationalContext().useWith { sortᴼ(numbers, sorted) } }))
    }
}
