package com.tkblackbelt.conquer4k.shared.network.transport.io

import com.tkblackbelt.conquer4k.shared.network.codec.FrameCodec
import com.tkblackbelt.conquer4k.shared.network.ext.writeShortLe
import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.writeBuffer
import kotlinx.io.Buffer

class FrameWriter(
    private val channel: ByteWriteChannel,
    private val codec: FrameCodec,
) {
    suspend fun write(buffer: Buffer): Int {
        val size = buffer.size.toInt()
        val encLen = codec.encodeLength(size.toShort())
        val encBody = codec.encodeBody(buffer)
        channel.writeShortLe(encLen.toInt())
        channel.writeBuffer(encBody)

        return size
    }

    suspend fun flush() {
        channel.flush()
    }
}
