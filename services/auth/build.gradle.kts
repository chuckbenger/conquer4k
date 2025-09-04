plugins {
    // Apply the shared build logic from a convention plugin.
    id("buildsrc.convention.kotlin-jvm")
    // Application plugin to run this service.
    application
}

dependencies {
    implementation(project(":shared:network"))
    implementation(libs.bundles.kotlinxEcosystem)
    implementation(libs.bundles.koin)
    implementation(libs.bundles.logging)
}

application {
    // Kotlin top-level function in AuthServer.kt compiles to AuthServerKt
    mainClass = "com.tkblackbelt.services.auth.AuthServerKt"
}
