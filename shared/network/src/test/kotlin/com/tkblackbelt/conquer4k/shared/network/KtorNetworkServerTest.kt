package com.tkblackbelt.conquer4k.shared.network

import io.ktor.network.selector.SelectorManager
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.openReadChannel
import io.ktor.network.sockets.openWriteChannel
import io.ktor.network.util.DefaultByteBufferPool
import io.ktor.utils.io.close
import io.ktor.utils.io.core.readBytes
import io.ktor.utils.io.pool.ObjectPool
import io.ktor.utils.io.readAvailable
import io.ktor.utils.io.readRemaining
import io.ktor.utils.io.writeFully
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean

class KtorNetworkServerTest {
    @Test
    fun `server echoes bytes using direct buffer`() =
        runBlocking {
            val directUsed = AtomicBoolean(false)
            val pool =
                object : ObjectPool<ByteBuffer> {
                    private val delegate = DefaultByteBufferPool

                    override val capacity: Int
                        get() = delegate.capacity

                    override fun borrow(): ByteBuffer {
                        val buffer = delegate.borrow()
                        if (buffer.isDirect) directUsed.set(true)
                        return buffer
                    }

                    override fun recycle(instance: ByteBuffer) {
                        delegate.recycle(instance)
                    }

                    override fun dispose() {
                        delegate.dispose()
                    }

                    override fun close() {
                        delegate.close()
                    }
                }

            val handler =
                TcpSessionHandler { socket, bufferPool ->
                    val reader = socket.openReadChannel()
                    val writer = socket.openWriteChannel(autoFlush = true)
                    val buffer = bufferPool.borrow()
                    try {
                        while (!reader.isClosedForRead) {
                            buffer.clear()
                            val read = reader.readAvailable(buffer)
                            if (read == -1) break
                            buffer.flip()
                            writer.writeFully(buffer)
                        }
                    } finally {
                        bufferPool.recycle(buffer)
                        socket.close()
                    }
                }

            val server = KtorNetworkServer(ServerConfig(port = 0), handler, pool)
            server.start()

            val selector = SelectorManager(Dispatchers.IO)
            val socket = aSocket(selector).tcp().connect("127.0.0.1", server.port)
            val writer = socket.openWriteChannel(autoFlush = true)
            val reader = socket.openReadChannel()

            val message = "hello"
            val sendBuffer = ByteBuffer.wrap(message.encodeToByteArray())
            writer.writeFully(sendBuffer)
            writer.close()

            val received = reader.readRemaining().readBytes()
            assertEquals(message, received.decodeToString())

            socket.close()
            selector.close()
            server.stop()

            assertTrue(directUsed.get())
        }
}
