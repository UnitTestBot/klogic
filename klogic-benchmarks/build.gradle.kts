plugins {
    id("org.klogic.klogic-base")
    id("me.champeau.jmh") version "0.7.1"
}

dependencies {
    implementation(project(":klogic-core"))
    implementation(project(":klogic-utils"))

    jmh("org.openjdk.jmh:jmh-core:1.36")
    jmh("org.openjdk.jmh:jmh-generator-annprocess:1.36")
}
