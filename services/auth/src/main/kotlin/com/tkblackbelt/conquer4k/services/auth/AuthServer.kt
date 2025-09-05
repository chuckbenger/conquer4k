package com.tkblackbelt.conquer4k.services.auth

import com.tkblackbelt.conquer4k.shared.network.api.Connection
import com.tkblackbelt.conquer4k.shared.network.transport.tcp.TcpServer
import com.tkblackbelt.conquer4k.shared.network.transport.tcp.TcpServerConfig
import com.tkblackbelt.conquer4k.shared.protocol.serder.decodePacket
import io.ktor.network.selector.ActorSelectorManager
import io.ktor.network.sockets.InetSocketAddress
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.openWriteChannel
import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.writeBuffer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.io.Buffer
import kotlinx.io.writeIntLe
import kotlinx.io.writeShortLe

/**
 * Entry point for the Auth service.
 * For now, this only boots a placeholder server and logs lifecycle events.
 */
fun main() {
    runBlocking {
        val tcpServer =
            TcpServer(TcpServerConfig("0.0.0.0", 8921)) { conn: Connection ->
                conn.incomingFrames().decodePacket().collect { packet ->
                    println("Received packet $packet")
                    conn.close()
                }
            }
        tcpServer.start()

        val selector = ActorSelectorManager(Dispatchers.IO)
        launch(Dispatchers.IO) {
            val socket = aSocket(selector).tcp().connect(InetSocketAddress("0.0.0.0", 8921))
            println("Connected to 127.0.0.1:1234")

            val writeChannel: ByteWriteChannel = socket.openWriteChannel(autoFlush = false)
            val buffer = Buffer()
            buffer.writeShortLe(6)
            buffer.writeShortLe(2)
            buffer.writeIntLe(3)
            writeChannel.writeBuffer(buffer)
            writeChannel.flush()
        }

        while (true) {
            delay(1000)
        }
    }
}
