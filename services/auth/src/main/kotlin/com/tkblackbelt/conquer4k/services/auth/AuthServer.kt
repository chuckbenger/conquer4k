package com.tkblackbelt.conquer4k.services.auth

import com.tkblackbelt.conquer4k.shared.network.api.Connection
import com.tkblackbelt.conquer4k.shared.network.transport.tcp.TcpClient
import com.tkblackbelt.conquer4k.shared.network.transport.tcp.TcpClientConfig
import com.tkblackbelt.conquer4k.shared.network.transport.tcp.TcpServer
import com.tkblackbelt.conquer4k.shared.network.transport.tcp.TcpServerConfig
import com.tkblackbelt.conquer4k.shared.protocol.serder.decodePacket
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
        launch(Dispatchers.IO) {
            val socket = TcpClient(TcpClientConfig("0.0.0.0", 8921)).connect()

            val buffer = Buffer()
            buffer.writeShortLe(2)
            buffer.writeIntLe(3)
            socket.sendFrame(buffer)
        }

        while (true) {
            delay(1000)
        }
    }
}
