plugins {
    // Shared Kotlin JVM library for networking abstractions.
    id("buildsrc.convention.kotlin-jvm")
}

dependencies {
    implementation(libs.kotlinxCoroutines)
    implementation(libs.ktor.network)

    testImplementation(libs.bundles.test)
    testImplementation(libs.kotlinxCoroutines)
    testImplementation(libs.ktor.network)
}
