# conquer4k

Learning project on building scalable, distributed network servers — using a private server for the classic MMORPG Conquer Online (patch 4294) as the concrete domain. The focus is clean architecture, reproducible patterns, and production‑grade networking fundamentals.

For educational and research use only. Not affiliated with or endorsed by TQ Digital.

## Why This Project
- Practice designing modular JVM services that can evolve from a monolith to distributed components.
- Explore high‑performance networking with Ktor: framing, backpressure, buffering, and structured concurrency.
- Establish opinionated patterns for configuration, observability, and testability.

## Architecture at a Glance
- Gradle multi‑module Kotlin/JVM (Java 21 toolchain)
- Ktor networking (sockets, Netty), coroutines, and kotlinx‑io buffers
- Koin for dependency injection
- Exposed + R2DBC Postgres for persistence
- kotlin‑logging + Logback for structured logs
- JUnit/Kotest/MockK for testing

## Modules
- `services/auth` — Auth service entrypoint (`com.tkblackbelt.services.auth.AuthServerKt`).
- `shared/network` — Networking interfaces, TCP server/client, and a framed transport (codecs, buffering, backpressure). See `shared/network/README.md` for usage and design notes.
- `shared/protocol` — Protocol scaffold where packet models and codecs will live.
- `buildSrc` — Shared Gradle convention (`buildsrc.convention.kotlin-jvm`).

## Networking Highlights
- Minimal, composable APIs: `NetworkServer`, `NetworkClient`, `Connection`, `ConnectionFactory`.
- Framed transport with pluggable `FrameCodec` for length/body encoding/decoding.
## Tech Stack
- Gradle 8.13 (Wrapper), Kotlin 2.2.0, JVM Toolchain 21 (Foojay resolver)
- Ktor 3.0.0 (server-core, server-netty, network)
- Koin 4.0.0 for DI
- Database: Exposed 1.0.0-beta-3 (core/dao/r2dbc) + R2DBC Postgres 0.8.13.RELEASE, pool 1.0.1.RELEASE
- Kotlinx: coroutines 1.9.0, serialization-json 1.7.3, datetime 0.6.1
- Logging: kotlin-logging 5.1.0 + Logback 1.5.18
- Testing: JUnit 5 (BOM 5.10.2), Kotest 5.9.1, MockK 1.13.10
- Formatting: Spotless 6.25.0 (version catalog)

## Build & Run
- List modules: `./gradlew projects`
- Build all: `./gradlew build`
- Run tests: `./gradlew test`
- Run Auth service: `./gradlew :services:auth:run`

The build uses the Gradle Wrapper (`./gradlew`) and a version catalog (`gradle/libs.versions.toml`) for dependency management.

## Learning & Contribution Notes
- Services depend only on `shared:*` libraries (avoid service→service deps).
- Protocol logic belongs in `shared/protocol`; networking in `shared/network`.
- See `shared/network/README.md` for framing/backpressure details and examples.
- See `AGENTS.md` for contributor guidelines and conventions.
