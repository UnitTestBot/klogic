plugins {
    kotlin("jvm") version "1.8.0"
}

repositories{
    mavenCentral()
}

dependencies{
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.5")
    // https://mvnrepository.com/artifact/org.junit.jupiter/junit-jupiter-api
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.9.2")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += listOf("-Xjvm-default=all", "-Xcontext-receivers")
        allWarningsAsErrors = true
        jvmTarget = "11"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
