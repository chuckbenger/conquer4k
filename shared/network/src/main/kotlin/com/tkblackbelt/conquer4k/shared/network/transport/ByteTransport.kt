package com.tkblackbelt.conquer4k.shared.network.transport

import io.ktor.network.sockets.Socket
import io.ktor.network.sockets.openReadChannel
import io.ktor.network.sockets.openWriteChannel
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.ByteWriteChannel
import kotlinx.coroutines.CoroutineScope

interface ByteTransport : AutoCloseable {
    val scope: CoroutineScope
    val input: ByteReadChannel
    val output: ByteWriteChannel

    override fun close()
}

internal class SocketByteTransport(
    override val scope: CoroutineScope,
    private val socket: Socket,
) : ByteTransport {
    override val input = socket.openReadChannel()
    override val output = socket.openWriteChannel()

    override fun close() {
        runCatching { socket.close() }
    }
}
