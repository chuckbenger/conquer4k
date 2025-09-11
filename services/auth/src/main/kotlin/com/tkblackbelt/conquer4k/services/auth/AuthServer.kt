package com.tkblackbelt.conquer4k.services.auth

import com.tkblackbelt.conquer4k.shared.network.framing.FramedFactoryConfig
import com.tkblackbelt.conquer4k.shared.network.framing.codec.PlainCodec
import com.tkblackbelt.conquer4k.shared.network.framing.framedFactory
import com.tkblackbelt.conquer4k.shared.network.framing.writing.BufferFrameWriterConfig
import com.tkblackbelt.conquer4k.shared.network.tcp.TcpClient
import com.tkblackbelt.conquer4k.shared.network.tcp.TcpClientConfig
import com.tkblackbelt.conquer4k.shared.network.tcp.TcpServer
import com.tkblackbelt.conquer4k.shared.network.tcp.TcpServerConfig
import com.tkblackbelt.conquer4k.shared.protocol.serder.decodePacket
import io.ktor.network.selector.SelectorManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.io.Buffer
import kotlinx.io.writeIntLe
import kotlinx.io.writeShortLe
import kotlin.time.Duration.Companion.milliseconds

/**
 * Entry point for the Auth service.
 * For now, this only boots a placeholder server and logs lifecycle events.
 */
fun main() {
    val manager = SelectorManager(Dispatchers.IO)

    runBlocking {
        val serverFactory = framedFactory(PlainCodec, FramedFactoryConfig(BufferFrameWriterConfig()))
        val tcpServer =
            TcpServer(TcpServerConfig("0.0.0.0", 8922), manager, serverFactory) {
                it.inbound().decodePacket().collect { packet ->
                    println("Received packet $packet")
                }
            }
        tcpServer.start()

        launch(Dispatchers.IO) {
            try {
                val clientFactory =
                    framedFactory(
                        PlainCodec,
                        FramedFactoryConfig(
                            BufferFrameWriterConfig(
                                flushInterval = 10.milliseconds,
                                flushBytesSize = 1024,
                            ),
                        ),
                    )
                val connection = TcpClient(TcpClientConfig("0.0.0.0", 8922), manager, this, clientFactory).connect()
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
                delay(10000)
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
