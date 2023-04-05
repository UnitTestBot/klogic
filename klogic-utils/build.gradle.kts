plugins {
    id("org.klogic.klogic-base")
    `java-test-fixtures`
}

dependencies {
    implementation(project(":klogic-core"))
    testFixturesImplementation(project(":klogic-core"))
}

configurations.asMap.let { configurationsMap ->
    with(components["java"] as AdhocComponentWithVariants) {
        listOf("testFixturesApiElements", "testFixturesRuntimeElements").forEach {
            configurationsMap[it]?.let { configuration ->
                withVariantsFromConfiguration(configuration) {
                    skip()
                }
            }
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            artifact(tasks["kotlinSourcesJar"])
        }
    }
}
