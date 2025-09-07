package com.tkblackbelt.conquer4k.shared.network.transport.tcp

import com.tkblackbelt.conquer4k.shared.network.transport.ByteTransport
import com.tkblackbelt.conquer4k.shared.network.transport.NetworkClient
import com.tkblackbelt.conquer4k.shared.network.transport.SocketByteTransport
import io.ktor.network.selector.ActorSelectorManager
import io.ktor.network.sockets.InetSocketAddress
import io.ktor.network.sockets.aSocket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

data class TcpClientConfig(
    val host: String,
    val port: Int,
)

class TcpClient(
    private val config: TcpClientConfig,
) : NetworkClient {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val selector = ActorSelectorManager(Dispatchers.IO)

    override suspend fun connect(): ByteTransport {
        val socket = aSocket(selector).tcp().connect(InetSocketAddress(config.host, config.port))
        return SocketByteTransport(scope, socket)
    }
}
