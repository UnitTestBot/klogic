import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("org.klogic.klogic-base")
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

dependencies{
    implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.5")
    testImplementation(project(":klogic-utils"))
    testImplementation(testFixtures(project(":klogic-utils")))
}

tasks.withType<ShadowJar> {
    minimize()
    // Replace the original jar with this executable jar
    archiveClassifier.set("")

    dependencies {
        // Do not include kotlin-stdlib to the executable jar
        exclude(dependency("org.jetbrains.kotlin:kotlin-stdlib"))
        exclude(dependency("org.jetbrains.kotlin:kotlin-stdlib-common"))
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            project.shadow.component(this)
            artifact(tasks["kotlinSourcesJar"])
        }
    }
}
