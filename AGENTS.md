# Repository Guidelines

## Project Structure & Module Organization
- Purpose: Private service for the classic MMORPG Conquer Online, patch 4294.
- Root: Gradle multi-module Kotlin/JVM (Java 21 toolchain).
- `services/auth`: Auth service entrypoint (`com.tkblackbelt.services.auth.AuthServerKt`).
- `shared/network`: Networking interfaces and TCP transport (e.g., `NetworkServer`, `NetworkClient`, `TcpServer`, `TcpClient`), plus a framed layer (`FrameCodec`, `FrameReader/Writer`, `framedFactory`). Supports configurable frame size limits and outbound buffering/backpressure.
- `shared/protocol`: Protocol scaffold (no code yet) for packets/codecs/registry.
- `buildSrc`: Shared Gradle convention (`buildsrc.convention.kotlin-jvm`).
- Key files: `settings.gradle.kts` (includes), `gradle/libs.versions.toml` (catalog).

## Tech Stack
- Build: Gradle 8.13 (Wrapper), Kotlin 2.2.0, JVM Toolchain 21, Foojay resolver 0.8.0.
- Std libs: kotlinx-coroutines 1.9.0, kotlinx-serialization-json 1.7.3, kotlinx-datetime 0.6.1.
- Web/IO: Ktor 3.0.0 (server-core, server-netty, network).
- DI: Koin 4.0.0 (core, Ktor integration).
- Persistence: Exposed 1.0.0-beta-3 (core/dao/r2dbc) + R2DBC Postgres 0.8.13.RELEASE and pool 1.0.1.RELEASE.
- Logging: kotlin-logging 5.1.0 + Logback 1.5.18.
- Quality: Spotless 6.25.0 (available via catalog).
- Testing: JUnit BOM 5.10.2, JUnit Jupiter, Kotest 5.9.1, MockK 1.13.10.

## Build, Test, and Development Commands
- `./gradlew projects`: Lists modules and verifies wiring.
- `./gradlew build`: Compiles all modules and runs tests.
- `./gradlew test`: Runs tests only.
- `./gradlew :services:auth:run`: Starts the Auth service locally.

## Coding Style & Naming Conventions
- Kotlin official style: 4-space indent, explicit imports, trailing commas allowed.
- Packages: `com.tkblackbelt.<area>...` (e.g., `com.tkblackbelt.shared.network`).
- Modules: `shared:<name>` for libraries; `services:<name>` for services.
- Naming: PascalCase types; camelCase functions/properties.
- Dependencies: Services depend only on `shared:*` (avoid service→service references).

## Testing Guidelines
- Names: `*Test.kt`; use descriptive display names (backticked identifiers allowed).
- Scope: Unit-test protocol/crypto/logic; add integration tests per service as needed.
- Run: `./gradlew test` or `./gradlew :<module>:test`.

## Commit & Pull Request Guidelines
- Commits: Imperative mood; prefix with module when useful.
  - Examples: `auth: add login flow skeleton`, `shared:network: refine server API`.
- PRs: Focused changes, clear description, linked issues, and build/run notes.
- Keep diffs minimal; update docs when structure or commands change.
