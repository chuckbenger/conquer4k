package com.tkblackbelt.conquer4k.shared.network.transport.tcp

import com.tkblackbelt.conquer4k.shared.network.api.Connection
import com.tkblackbelt.conquer4k.shared.network.crypto.PacketCipher
import com.tkblackbelt.conquer4k.shared.network.transport.frames
import com.tkblackbelt.conquer4k.shared.network.transport.launchOutboundWriter
import io.ktor.network.sockets.Socket
import io.ktor.network.sockets.openReadChannel
import io.ktor.network.sockets.openWriteChannel
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.ByteWriteChannel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.io.Buffer

class TcpConnection(
    private val socket: Socket,
    private val scope: CoroutineScope,
) : Connection {
    private val input: ByteReadChannel = socket.openReadChannel()
    private val output: ByteWriteChannel = socket.openWriteChannel(autoFlush = true)
    private var writeChannel: SendChannel<Buffer>? = null

    @Volatile
    private var cipher: PacketCipher? = null

    override fun setCipher(cipher: PacketCipher?) {
        this.cipher = cipher
    }

    override fun incomingFrames(): Flow<Buffer> = input.frames(cipher)

    override suspend fun sendFrame(frame: Buffer) {
        if (writeChannel == null) {
            writeChannel =
                scope.launchOutboundWriter(
                    output,
                    cipher,
                )
        }
        writeChannel?.send(frame)
    }

    override fun close() {
        try {
            socket.close()
        } catch (_: Throwable) {
        }
    }
}
