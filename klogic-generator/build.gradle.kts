plugins {
    id("org.klogic.klogic-base")
}

repositories {
    mavenCentral()
}

dependencies {
//    compileOnly(project(":klogic-core"))

    implementation("com.google.devtools.ksp:symbol-processing-api:1.8.0-1.0.9")
    implementation("com.squareup:kotlinpoet-ksp:1.14.2")
}
