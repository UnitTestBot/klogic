plugins {
    id("org.klogic.klogic-base")
}

dependencies{
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.5")
    testImplementation(project(":klogic-utils"))
    testImplementation(testFixtures(project(":klogic-utils")))
}
