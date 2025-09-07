package com.tkblackbelt.conquer4k.shared.network.transport.io

import com.tkblackbelt.conquer4k.shared.network.codec.FrameCodec
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.InternalAPI
import io.ktor.utils.io.availableForRead
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.io.Buffer
import kotlinx.io.readShortLe
import java.io.EOFException

private const val MIN_PACKET_SIZE = 1
private const val MAX_PACKET_SIZE = 1024
private const val NEED_HEADER = -1

@OptIn(InternalAPI::class)
internal fun ByteReadChannel.frames(codec: FrameCodec): Flow<Buffer> =
    flow {
        var state: Int = NEED_HEADER // -1 => need header; otherwise => body size

        while (true) {
            awaitContent()
            if (isClosedForRead) break

            var progressed = false
            while (true) {
                if (state == NEED_HEADER) {
                    if (availableForRead < 2) break
                    val encLen = readBuffer.readShortLe()
                    val frameSize = codec.decodeLength(encLen).toInt()
                    if (frameSize !in MIN_PACKET_SIZE..MAX_PACKET_SIZE) {
                        throw IllegalStateException("Invalid frame size: $frameSize")
                    }
                    state = frameSize
                    progressed = true
                } else {
                    val size = state
                    if (availableForRead < size) break

                    val buf = Buffer()
                    readBuffer.readTo(buf, size.toLong())
                    emit(codec.decodeBody(buf))

                    state = NEED_HEADER
                    progressed = true
                }
            }

            if (!progressed && isClosedForRead) break
        }

        if (state != NEED_HEADER) {
            throw EOFException("Stream ended mid-frame")
        }
    }
