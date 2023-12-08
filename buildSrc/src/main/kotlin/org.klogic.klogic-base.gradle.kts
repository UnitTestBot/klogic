import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    `maven-publish`
}

group = "org.klogic"
version = "0.2.2"

repositories {
    mavenCentral()
}

dependencies {
    // https://mvnrepository.com/artifact/org.junit.jupiter/junit-jupiter-api
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.9.2")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += listOf("-Xjvm-default=all", "-Xcontext-receivers")
        allWarningsAsErrors = true
        jvmTarget = "1.8"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()

    // TODO use it only for huge tests
    maxHeapSize = "4096m"

    systemProperty("junit.jupiter.execution.parallel.enabled", true)
}

configurations.asMap.let { configurationsMap ->
    with(components["java"] as AdhocComponentWithVariants) {
        listOf("testFixturesApiElements", "testFixturesRuntimeElements").forEach {
            configurationsMap[it]?.let {
                withVariantsFromConfiguration(it) {
                    skip()
                }
            }
        }
    }
}

publishing {
    repositories {
        maven {
            name = "releaseDir"
            url = uri(layout.buildDirectory.dir("release"))
        }
    }
}
