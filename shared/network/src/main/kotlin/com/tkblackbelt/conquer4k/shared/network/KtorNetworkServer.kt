package com.tkblackbelt.conquer4k.shared.network

import io.ktor.network.selector.SelectorManager
import io.ktor.network.sockets.ServerSocket
import io.ktor.network.sockets.Socket
import io.ktor.network.sockets.aSocket
import io.ktor.network.util.DefaultByteBufferPool
import io.ktor.utils.io.pool.ObjectPool
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.net.InetSocketAddress
import java.nio.ByteBuffer

/**
 * Ktor-based [NetworkServer] implementation using a [ByteBuffer] pool.
 * It accepts TCP connections and delegates handling to the provided [TcpSessionHandler].
 */
class KtorNetworkServer(
    private val config: ServerConfig,
    private val handler: TcpSessionHandler,
    private val pool: ObjectPool<ByteBuffer> = DefaultByteBufferPool,
) : NetworkServer {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val selector = SelectorManager(Dispatchers.IO)
    private var serverSocket: ServerSocket? = null

    /** The actual port the server is bound to. */
    val port: Int
        get() = (serverSocket?.localAddress as? InetSocketAddress)?.port ?: config.port

    override fun start() {
        serverSocket = runBlocking { aSocket(selector).tcp().bind(config.host, config.port) }
        scope.launch {
            val server = serverSocket ?: return@launch
            while (true) {
                val socket = server.accept()
                launch { handler.handle(socket, pool) }
            }
        }
    }

    override fun stop() {
        scope.cancel()
        serverSocket?.close()
        selector.close()
    }
}

/**
 * Functional interface for handling an individual TCP connection.
 * Implementations are responsible for borrowing and recycling buffers from [pool].
 */
fun interface TcpSessionHandler {
    suspend fun handle(
        socket: Socket,
        pool: ObjectPool<ByteBuffer>,
    )
}
