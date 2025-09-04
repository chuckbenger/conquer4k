package com.tkblackbelt.conquer4k.shared.network.server

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.network.selector.SelectorManager
import io.ktor.network.sockets.Socket
import io.ktor.network.sockets.aSocket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.Closeable

private val logger = KotlinLogging.logger {}

fun interface TcpServerHandler {
    suspend fun handle(socket: Socket)
}

data class TcpServerConfig(
    val host: String,
    val port: Int,
)

class TcpServer(
    private val config: TcpServerConfig,
    private val handler: TcpServerHandler,
) : Closeable {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    suspend fun start() {
        logger.info { "Starting Tcp server... $config" }

        val selector = SelectorManager(Dispatchers.IO)
        val server = aSocket(selector).tcp().bind(config.host, config.port)

        scope.launch {
            try {
                while (true) {
                    val client = server.accept()
                    logger.debug { "Client connected: $client" }
                    launch { handler.handle(client) }
                }
            } finally {
                server.close()
            }
        }
    }

    override fun close() {
        scope.cancel()
    }
}
