package org.klogic.benchmarks

import org.openjdk.jmh.annotations.*
import java.util.concurrent.TimeUnit

@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 3)
@Fork(1)
@Measurement(iterations = 10, timeUnit = TimeUnit.MILLISECONDS)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
abstract class AbstractKlogicBenchmark
