package com.tkblackbelt.conquer4k.shared.network.transport.io

import com.tkblackbelt.conquer4k.shared.network.codec.FrameCodec
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.InternalAPI
import io.ktor.utils.io.availableForRead
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.io.Buffer
import kotlinx.io.readShortLe

private const val MIN_PACKET_SIZE = 1
private const val MAX_PACKET_SIZE = 1024
private const val NEED_HEADER = -1

@OptIn(InternalAPI::class)
internal class FrameReader(
    private val channel: ByteReadChannel,
    private val codec: FrameCodec,
) {
    private var pendingSize: Int = NEED_HEADER

    fun readFrameOrNull(): Buffer? {
        if (needsHeader()) {
            if (!hasHeaderBuffered()) return null

            readHeader()
        }

        if (!hasBodyBuffered()) return null

        return readBody()
    }

    private fun needsHeader() = pendingSize == NEED_HEADER
    private fun hasHeaderBuffered() = channel.availableForRead >= 2

    private fun readHeader() {
        val encoded = channel.readBuffer.readShortLe()
        val decoded = codec.decodeLength(encoded).toInt()

        require(decoded in MIN_PACKET_SIZE..MAX_PACKET_SIZE) {
            "Invalid frame length: $decoded (allowed $MIN_PACKET_SIZE..$MAX_PACKET_SIZE)"
        }

        pendingSize = decoded
    }

    private fun hasBodyBuffered() = pendingSize != NEED_HEADER && channel.availableForRead >= pendingSize

    private fun readBody(): Buffer {
        val buffer = Buffer()
        channel.readBuffer.readTo(buffer, pendingSize.toLong())
        pendingSize = NEED_HEADER
        return codec.decodeBody(buffer)
    }
}

internal fun ByteReadChannel.frames(codec: FrameCodec): Flow<Buffer> = flow {
    val reader = FrameReader(this@frames, codec)

    while (true) {
        awaitContent()

        while (true) {
            val frame = reader.readFrameOrNull() ?: break
            emit(frame)
        }

        if (isClosedForRead) break
    }
}
