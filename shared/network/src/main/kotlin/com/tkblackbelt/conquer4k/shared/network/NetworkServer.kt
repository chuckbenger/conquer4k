package com.tkblackbelt.conquer4k.shared.network

/** Lightweight networking abstractions.
 * Concrete implementations (e.g., Netty) can be added later without changing service code.
 */
interface NetworkServer {
    fun start()

    fun stop()
}

data class ServerConfig(
    val host: String = "0.0.0.0",
    val port: Int,
)

/** No-op server placeholder until a real transport is chosen. */
class NoopNetworkServer(
    private val config: ServerConfig,
) : NetworkServer {
    override fun start() {
        println("[network] Noop server listening on ${'$'}{config.host}:${'$'}{config.port} (placeholder)")
    }

    override fun stop() {
        println("[network] Noop server stopped")
    }
}
