package com.tkblackbelt.conquer4k.shared.network.framing.writing

import com.tkblackbelt.conquer4k.shared.network.ext.writeShortLe
import com.tkblackbelt.conquer4k.shared.network.framing.codec.FrameCodec
import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.writeBuffer
import kotlinx.io.Buffer

internal class BasicFrameWriter(
    private val channel: ByteWriteChannel,
    private val codec: FrameCodec,
) : FrameWriter {
    override suspend fun write(buffer: Buffer): Int {
        val size = buffer.size.toInt()
        val encLen = codec.encodeLength(size.toShort())
        val encBody = codec.encodeBody(buffer)
        channel.writeShortLe(encLen.toInt())
        channel.writeBuffer(encBody)

        return size
    }

    override suspend fun flush() {
        channel.flush()
    }
}
