package com.tkblackbelt.conquer4k.shared.network.transport.tcp

import com.tkblackbelt.conquer4k.shared.network.api.ConnectionHandler
import com.tkblackbelt.conquer4k.shared.network.api.NetworkServer
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.network.selector.SelectorManager
import io.ktor.network.sockets.ServerSocket
import io.ktor.network.sockets.Socket
import io.ktor.network.sockets.TypeOfService
import io.ktor.network.sockets.aSocket
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore
import java.util.concurrent.atomic.AtomicBoolean

private val logger = KotlinLogging.logger {}

data class TcpServerConfig(
    val host: String,
    val port: Int,
    val backlog: Int = 1024,
    val maxConnections: Int = Int.MAX_VALUE,
    val lowDelayTos: Boolean = true,
)

class TcpServer(
    private val config: TcpServerConfig,
    private val selector: SelectorManager,
    private val handler: ConnectionHandler,
) : NetworkServer {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val started = AtomicBoolean(false)
    private val connectionLimiter =
        if (config.maxConnections == Int.MAX_VALUE) null else Semaphore(config.maxConnections)

    @Volatile
    private var serverJob: Job? = null

    override suspend fun start() {
        if (!started.compareAndSet(false, true)) return

        try {
            val serverSocket = bindServer()
            serverJob = launchAcceptLoop(serverSocket)
        } catch (t: Throwable) {
            started.set(false)
            logger.error(t) { "TCP server failed to bind." }
            throw t
        }
    }

    private suspend fun bindServer(): ServerSocket {
        val builder = aSocket(selector).tcp()

        return builder.bind(config.host, config.port) {
            backlogSize = config.backlog
            if (config.lowDelayTos) typeOfService = TypeOfService.IPTOS_LOWDELAY
        }
    }

    private fun launchAcceptLoop(serverSocket: ServerSocket): Job =
        scope.launch(CoroutineName("tcp-accept-loop-${config.port}")) {
            try {
                while (isActive) {
                    acceptOnce(serverSocket)?.let { acceptedSocket ->
                        launchHandler(acceptedSocket)
                    }
                }
            } finally {
                runCatching { serverSocket.close() }
            }
        }

    private suspend fun CoroutineScope.acceptOnce(serverSocket: ServerSocket): Socket? {
        connectionLimiter?.acquire()
        val socket =
            try {
                serverSocket.accept()
            } catch (ce: CancellationException) {
                connectionLimiter?.release()
                throw ce
            } catch (t: Throwable) {
                if (isActive) logger.warn(t) { "Accept failed." }
                connectionLimiter?.release()
                null
            }
        return socket
    }

    private fun CoroutineScope.launchHandler(socket: Socket) =
        launch(CoroutineName("tcp-conn-${socket.remoteAddress}")) {
            val connection = TcpConnection(socket, scope)
            try {
                handler.handle(connection)
            } catch (_: CancellationException) {
            } catch (t: Throwable) {
                logger.error(t) { "Handler failed for ${socket.remoteAddress}" }
            } finally {
                runCatching { connection.close() }
                connectionLimiter?.release()
            }
        }

    override fun close() {
        if (!started.get()) return

        runCatching { runBlocking { serverJob?.cancelAndJoin() } }
        scope.cancel()

        serverJob = null

        started.set(false)
        logger.info { "TCP Server stopped" }
    }
}
