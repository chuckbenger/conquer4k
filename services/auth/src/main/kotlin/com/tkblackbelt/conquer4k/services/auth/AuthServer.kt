package com.tkblackbelt.conquer4k.services.auth

import com.tkblackbelt.conquer4k.shared.network.codec.PlainCodec
import com.tkblackbelt.conquer4k.shared.network.transport.ByteTransport
import com.tkblackbelt.conquer4k.shared.network.transport.connection.FramedConnection
import com.tkblackbelt.conquer4k.shared.network.transport.tcp.TcpClient
import com.tkblackbelt.conquer4k.shared.network.transport.tcp.TcpClientConfig
import com.tkblackbelt.conquer4k.shared.network.transport.tcp.TcpServer
import com.tkblackbelt.conquer4k.shared.network.transport.tcp.TcpServerConfig
import com.tkblackbelt.conquer4k.shared.protocol.serder.decodePacket
import io.ktor.network.selector.SelectorManager
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
        val manager = SelectorManager(Dispatchers.IO)
        val tcpServer =
            TcpServer(TcpServerConfig("0.0.0.0", 8921), manager) { transport: ByteTransport ->
                val codec = PlainCodec
                val connection = FramedConnection(transport, codec)

                connection.inbound().decodePacket().collect { packet ->
                    println("Received packet $packet")
                }

                println("Done")
            }
        tcpServer.start()

        launch(Dispatchers.IO) {
            try {
                val transport = TcpClient(TcpClientConfig("0.0.0.0", 8921)).connect()
                val codec = PlainCodec
                val connection = FramedConnection(transport, codec)
                var test = 0
                repeat(5) {
                    try {
                        val buffer = Buffer()
                        buffer.writeShortLe(2)
                        buffer.writeIntLe(test)
                        connection.send(buffer)
                        test++
//                        delay(10000)
//                        tcpServer.close()
                    } catch (e: Exception) {
                        println("Error: ${e.message}")
                    }
                }
                delay(1000)
                connection.close()
            } catch (e: Exception) {
                println("Error: ${e.message}")
            }

            while (true) {
                delay(1000)
            }
        }
    }
}
