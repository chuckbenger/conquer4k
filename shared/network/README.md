# shared/network

Networking abstractions and a framed TCP transport built on Ktor. This module provides small, composable interfaces for servers/clients plus a framing layer you can plug codecs into.

## Components
- `NetworkServer`, `NetworkClient` — minimal contracts for starting a server and dialing a connection.
- `Connection` — inbound `Flow<Buffer>` and `suspend fun send(Buffer)`.
- `ConnectionFactory` — wraps a raw Ktor `Socket` into a `Connection` (e.g., framed transport).
- TCP implementations — `TcpServer`, `TcpClient`.
- Framing — `FrameCodec`, `FrameReader`, `FrameWriter` with `BufferedFrameWriter`.
- Factory — `framedFactory(codec, FramedFactoryConfig)` to create framed `Connection`s.

## Quick Start

### Server
```kotlin
import com.tkblackbelt.conquer4k.shared.network.api.*
import com.tkblackbelt.conquer4k.shared.network.tcp.*
import com.tkblackbelt.conquer4k.shared.network.framing.*
import com.tkblackbelt.conquer4k.shared.network.framing.codec.*
import io.ktor.network.selector.SelectorManager
import kotlinx.coroutines.*

val selector = SelectorManager(Dispatchers.IO)
val server = TcpServer(
    config = TcpServerConfig(host = "0.0.0.0", port = 9876, maxConnections = 10_000),
    selector = selector,
    connectionFactory = framedFactory(
        codec = PlainCodec, // or your custom FrameCodec
        config = FramedFactoryConfig(
            bufferingConfig = BufferFrameWriterConfig(
                bufferCapacity = 512,
                flushInterval = 5.milliseconds,
                flushBatchSize = 4096,
                // Overflow strategy controls backpressure behavior (see below)
            ),
            minFrameSize = 1,
            maxFrameSize = 1024,
        ),
    ),
) { conn: Connection ->
    // Per-connection handler
    coroutineScope {
        launch {
            conn.inbound().collect { frame ->
                // Handle decoded frame (kotlinx-io Buffer)
            }
        }
    }
}

runBlocking { server.start() }
```

### Client
```kotlin
import com.tkblackbelt.conquer4k.shared.network.api.*
import com.tkblackbelt.conquer4k.shared.network.tcp.*
import com.tkblackbelt.conquer4k.shared.network.framing.*
import com.tkblackbelt.conquer4k.shared.network.framing.codec.*
import io.ktor.network.selector.SelectorManager
import kotlinx.coroutines.*

val client = TcpClient(
    config = TcpClientConfig(host = "127.0.0.1", port = 9876),
    selector = SelectorManager(Dispatchers.IO),
    scope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
    connectionFactory = framedFactory(PlainCodec, FramedFactoryConfig(BufferFrameWriterConfig())),
)

runBlocking {
    val conn = client.connect()
    // Send a frame
    val out = kotlinx.io.Buffer().apply { write(byteArrayOf(1,2,3)) }
    conn.send(out)
}
```

## Framing

Each frame is serialized as:
- 2-byte little-endian length header (from `FrameCodec.encodeLength`)
- followed by the encoded body (`FrameCodec.encodeBody`).

`FrameReader` validates frame sizes within `[minFrameSize..maxFrameSize]`, which are provided via `FramedFactoryConfig` (defaults: `1..1024`).

### FrameCodec contract
- `encodeLength(length: Short): Short` and `decodeLength(length: Short): Short` — length header transform (e.g., XOR, encryption) while staying 16-bit.
- `encodeBody(buffer: Buffer): Buffer` and `decodeBody(buffer: Buffer): Buffer` — body transform.
- The current `BasicFrameWriter` writes the encoded length and the encoded body as returned by `FrameCodec`. If your codec changes body size, keep the length semantics consistent with your protocol (typical is “encoded body size”). The provided `PlainCodec` is identity.

## Outbound buffering, flushing, and backpressure

Outbound frames are placed on a per-connection channel (“inbox”), then written via `BufferedFrameWriter` which batches flushes by:
- `flushBatchSize` — flush once written bytes since last flush meet/exceed this threshold, and
- `flushInterval` — best-effort periodic flush if data is pending.

### Overflow strategy
`BufferFrameWriterConfig` exposes `overflowStrategy` (default: `BufferOverflow.DROP_OLDEST`). This controls what happens when the inbox is full:
- `SUSPEND` (default): preserves order and enqueues every frame; producers suspend until space is available (may stall if peer is not reading).
- `DROP_OLDEST`: non-blocking; oldest queued frame is dropped to make room for the latest. Keeps writers responsive under slow/bad clients.

`Connection.send` first tries `trySend` (non-blocking). If the overflow strategy is `SUSPEND` or the immediate try fails due to timing, it falls back to `send` and will suspend according to the chosen strategy.

## TCP server specifics
- `TcpServerConfig`: `host`, `port`, `backlog`, `maxConnections`, `lowDelayTos` (IP TOS LOWDELAY).
- Limits concurrent accepts using a semaphore when `maxConnections` is set.
- Per-connection handlers run on a `SupervisorJob`-backed `Dispatchers.IO` scope.

## Utilities
- `Buffer.toDebugString(consume=false)` — renders bytes `[dec,...] hex "ascii"` (avoid on very large buffers in hot paths).
- `ByteWriteChannel.writeShortLe(Int)` — writes a 16-bit little-endian short.

## Testing
- Build: `./gradlew :shared:network:build`
- Tests: `./gradlew :shared:network:test`

Suggested unit tests:
- Frame size boundaries and invalid lengths in `FrameReader`.
- `BufferedFrameWriter` flush-by-batch and flush-by-interval behavior.
- Round-trip with custom `FrameCodec` to confirm length/body semantics.
