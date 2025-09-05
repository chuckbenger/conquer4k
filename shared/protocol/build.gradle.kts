plugins {
    // Shared Kotlin JVM library for networking abstractions.
    id("buildsrc.convention.kotlin-jvm")
    alias(libs.plugins.kotlinPluginSerialization)
}

dependencies {
    implementation(libs.bundles.ktor.server)
    implementation(libs.bundles.kotlinxEcosystem)
    implementation(libs.bundles.logging)
}
