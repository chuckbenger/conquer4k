Protocol module scaffold (no code).

Structure:
- src/main/kotlin/com/tkblackbelt/shared/protocol/
  - packet/        — packet model namespaces (no classes yet)
  - codec/         — encoders/decoders (to be added later)
  - registry/      — opcode/handler registry stubs (later)
  - version/       — per-client-version strategy (later)
- src/test/kotlin/com/tkblackbelt/shared/protocol/ — tests to be added later

This module intentionally contains no Kotlin sources yet. It exists to
establish package layout and Gradle wiring before implementing packets.

