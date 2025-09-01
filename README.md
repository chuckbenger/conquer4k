# conquer4k

Private server project for the classic MMORPG Conquer Online (patch 4294). The goal is a clean, modular Kotlin codebase that can evolve from a modular monolith into separate services as features mature.

For educational and research use only. Not affiliated with or endorsed by TQ Digital

## Modules
- `services/auth`: Auth service entrypoint (`com.tkblackbelt.services.auth.AuthServerKt`).
- `shared/network`: Networking interfaces and stubs to decouple transports.
- `shared/protocol`: Protocol scaffold (no code yet) where packet models/codecs will live.
- `buildSrc`: Shared Gradle convention (`buildsrc.convention.kotlin-jvm`).

## Tech Stack
- Gradle 8.13 (Wrapper), Kotlin 2.2.0, JVM Toolchain 21 (Foojay resolver).
- Ktor 3.0.0 (server-core, server-netty, network).
- Koin 4.0.0 for DI.
- Database: Exposed 1.0.0-beta-3 (core/dao/r2dbc) + R2DBC Postgres 0.8.13.RELEASE, pool 1.0.1.RELEASE.
- Kotlinx: coroutines 1.9.0, serialization-json 1.7.3, datetime 0.6.1.
- Logging: kotlin-logging 5.1.0 + Logback 1.5.18.
- Testing: JUnit 5 (BOM 5.10.2), Kotest 5.9.1, MockK 1.13.10.
- Formatting: Spotless 6.25.0 (available via catalog).

## Build & Run
- List modules: `./gradlew projects`
- Build all: `./gradlew build`
- Run tests: `./gradlew test`
- Run Auth service: `./gradlew :services:auth:run`

The build uses the Gradle Wrapper (`./gradlew`) and a version catalog (`gradle/libs.versions.toml`) for dependency management.

## Development Notes
- Services should depend only on `shared:*` modules (avoid service→service deps).
- Protocol work belongs in `shared/protocol`; networking abstractions in `shared/network`.
- See `AGENTS.md` for contributor guidelines and conventions.
