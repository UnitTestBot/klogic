plugins {
    id("org.klogic.klogic-base")
    `java-test-fixtures`
}

dependencies {
    implementation(project(":klogic-core"))
    testFixturesImplementation(project(":klogic-core"))
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            artifact(tasks["kotlinSourcesJar"])
        }
    }
}
