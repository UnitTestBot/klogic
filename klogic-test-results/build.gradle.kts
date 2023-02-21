plugins {
    id("org.klogic.klogic-base")
    id("test-report-aggregation")
}

repositories {
    gradlePluginPortal()
}

dependencies {
    testReportAggregation(project(":klogic-core"))
    testReportAggregation(project(":klogic-utils"))
}

tasks.check {
    dependsOn(tasks.named<TestReport>("testAggregateTestReport"))
}