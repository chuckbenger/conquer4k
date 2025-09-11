plugins {
    // Shared Kotlin JVM library for networking abstractions.
    id("buildsrc.convention.kotlin-jvm")
}

dependencies {
    api(libs.bundles.ktor.server)
    implementation(libs.bundles.kotlinxEcosystem)
    implementation(libs.bundles.logging)
    implementation(project(":shared:protocol"))
}
