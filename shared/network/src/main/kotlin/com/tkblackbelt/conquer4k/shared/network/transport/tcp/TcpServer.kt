package com.tkblackbelt.conquer4k.shared.network.transport.tcp

import com.tkblackbelt.conquer4k.shared.network.api.ConnectionHandler
import com.tkblackbelt.conquer4k.shared.network.api.NetworkServer
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.network.selector.SelectorManager
import io.ktor.network.sockets.aSocket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

private val logger = KotlinLogging.logger {}

data class TcpServerConfig(
    val host: String,
    val port: Int,
)

class TcpServer(
    private val config: TcpServerConfig,
    private val handler: ConnectionHandler,
) : NetworkServer {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override suspend fun start() {
        logger.info { "Starting Tcp server... $config" }

        val selector = SelectorManager(Dispatchers.IO)
        val server = aSocket(selector).tcp().bind(config.host, config.port)

        scope.launch {
            try {
                while (true) {
                    val client = server.accept()
                    logger.debug { "Client connected: $client" }
                    val conn = TcpConnection(client, scope)
                    launch { handler.handle(conn) }
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
