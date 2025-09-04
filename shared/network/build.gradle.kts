plugins {
    // Shared Kotlin JVM library for networking abstractions.
    id("buildsrc.convention.kotlin-jvm")
}

dependencies {
    implementation(libs.bundles.kotlinxEcosystem)
    implementation(libs.bundles.ktor.server)
    implementation(libs.bundles.logging)
}
