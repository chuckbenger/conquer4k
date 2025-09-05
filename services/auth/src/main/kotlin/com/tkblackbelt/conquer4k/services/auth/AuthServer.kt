package com.tkblackbelt.conquer4k.services.auth

import com.tkblackbelt.conquer4k.shared.network.transport.tcp.TcpServer
import com.tkblackbelt.conquer4k.shared.network.transport.tcp.TcpServerConfig
import com.tkblackbelt.conquer4k.shared.network.api.Connection
import com.tkblackbelt.conquer4k.shared.network.io.toDebugString
import io.ktor.network.selector.ActorSelectorManager
import io.ktor.network.sockets.InetSocketAddress
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.openWriteChannel
import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.write
import io.ktor.utils.io.writeBuffer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.io.Buffer
import kotlinx.io.writeShortLe

/**
 * Entry point for the Auth service.
 * For now, this only boots a placeholder server and logs lifecycle events.
 */
fun main() {
    runBlocking {
        val tcpServer = TcpServer(TcpServerConfig("0.0.0.0", 8921)) { conn: Connection ->
            conn.incomingFrames().collect { frame ->
                println("Received frame ${frame.toDebugString()}")
            }
        }
        tcpServer.start()


        val selector = ActorSelectorManager(Dispatchers.IO)
        launch(Dispatchers.IO) {
            val socket = aSocket(selector).tcp().connect(InetSocketAddress("0.0.0.0", 8921))
            println("Connected to 127.0.0.1:1234")

            val writeChannel: ByteWriteChannel = socket.openWriteChannel(autoFlush = false)
            val buffer = Buffer()
            buffer.writeShortLe(2)
            buffer.writeShortLe(3)
            writeChannel.writeBuffer(buffer)
            writeChannel.flush()

            val buffer2 = Buffer()
            buffer2.writeShortLe(2)
            buffer2.writeShortLe(5)
            writeChannel.writeBuffer(buffer2)
            writeChannel.flush()
        }

        while(true) {
            delay(1000)
        }
    }
}
