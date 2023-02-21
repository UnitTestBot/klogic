plugins {
    id("org.klogic.klogic-base")
    `java-test-fixtures`
}

dependencies {
    implementation(project(":klogic-core"))
    testFixturesImplementation(project(":klogic-core"))
}
