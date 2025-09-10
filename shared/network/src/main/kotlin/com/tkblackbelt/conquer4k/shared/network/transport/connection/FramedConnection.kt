package com.tkblackbelt.conquer4k.shared.network.transport.connection

import com.tkblackbelt.conquer4k.shared.network.codec.FrameCodec
import com.tkblackbelt.conquer4k.shared.network.transport.ByteTransport
import com.tkblackbelt.conquer4k.shared.network.transport.io.BufferFrameWriterConfig
import com.tkblackbelt.conquer4k.shared.network.transport.io.frames
import com.tkblackbelt.conquer4k.shared.network.transport.io.launchOutboundWriter
import kotlinx.coroutines.flow.Flow
import kotlinx.io.Buffer

class FramedConnection(
    private val transport: ByteTransport,
    private val codec: FrameCodec,
    private val config: BufferFrameWriterConfig = BufferFrameWriterConfig(),
) : Connection {
    private val writer =
        transport.scope.launchOutboundWriter(
            transport.output,
            codec,
            config,
        )

    override fun inbound(): Flow<Buffer> = transport.input.frames(codec)

    override suspend fun send(buffer: Buffer) {
        writer.send(buffer)
    }

    override fun close() {
        runCatching { writer.close() }
        runCatching { transport.close() }
    }
}
