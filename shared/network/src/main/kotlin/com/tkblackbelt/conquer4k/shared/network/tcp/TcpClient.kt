package com.tkblackbelt.conquer4k.shared.network.tcp

import com.tkblackbelt.conquer4k.shared.network.api.Connection
import com.tkblackbelt.conquer4k.shared.network.api.ConnectionFactory
import com.tkblackbelt.conquer4k.shared.network.api.NetworkClient
import io.ktor.network.selector.SelectorManager
import io.ktor.network.sockets.InetSocketAddress
import io.ktor.network.sockets.aSocket
import kotlinx.coroutines.CoroutineScope

data class TcpClientConfig(
    val host: String,
    val port: Int,
)

class TcpClient(
    private val config: TcpClientConfig,
    private val selector: SelectorManager,
    private val scope: CoroutineScope,
    private val connectionFactory: ConnectionFactory,
) : NetworkClient {
    override suspend fun connect(): Connection {
        val socket = aSocket(selector).tcp().connect(InetSocketAddress(config.host, config.port))
        return connectionFactory.create(scope, socket)
    }
}
