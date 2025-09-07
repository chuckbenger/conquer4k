package com.tkblackbelt.conquer4k.shared.network.transport.connection

import com.tkblackbelt.conquer4k.shared.network.codec.FrameCodec
import com.tkblackbelt.conquer4k.shared.network.transport.ByteTransport
import com.tkblackbelt.conquer4k.shared.network.transport.io.frames
import com.tkblackbelt.conquer4k.shared.network.transport.io.launchOutboundWriter
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.io.Buffer

class FramedConnection(
    private val transport: ByteTransport,
    private val codec: FrameCodec,
    writeBufferCapacity: Int = Channel.BUFFERED,
) : Connection {
    private val writer =
        transport.scope.launchOutboundWriter(
            transport.output,
            codec,
            writeBufferCapacity,
        )

    override fun incomingFrames(): Flow<Buffer> = transport.input.frames(codec)

    override suspend fun sendFrame(frame: Buffer) {
        writer.send(frame)
    }

    override fun close() {
        runCatching { writer.close() }
        runCatching { transport.close() }
    }
}
