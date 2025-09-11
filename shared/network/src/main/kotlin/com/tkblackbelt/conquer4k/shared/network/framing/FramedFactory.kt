package com.tkblackbelt.conquer4k.shared.network.framing

import com.tkblackbelt.conquer4k.shared.network.api.Connection
import com.tkblackbelt.conquer4k.shared.network.api.ConnectionFactory
import com.tkblackbelt.conquer4k.shared.network.framing.codec.FrameCodec
import com.tkblackbelt.conquer4k.shared.network.framing.reading.FrameReader
import com.tkblackbelt.conquer4k.shared.network.framing.writing.BasicFrameWriter
import com.tkblackbelt.conquer4k.shared.network.framing.writing.BufferFrameWriterConfig
import com.tkblackbelt.conquer4k.shared.network.framing.writing.BufferedFrameWriter
import io.ktor.network.sockets.Socket
import io.ktor.network.sockets.openReadChannel
import io.ktor.network.sockets.openWriteChannel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.selects.onTimeout
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.withTimeout
import kotlinx.io.Buffer
import kotlin.time.Duration

fun framedFactory(
    codec: FrameCodec,
    config: FramedFactoryConfig,
) = ConnectionFactory { scope, socket -> socket.framed(scope, codec, config) }

data class FramedFactoryConfig(
    val bufferingConfig: BufferFrameWriterConfig,
    val minFrameSize: Int = 1,
    val maxFrameSize: Int = 1024,
    val sendTimeout: Duration? = null,
)

@OptIn(ExperimentalCoroutinesApi::class)
private fun Socket.framed(
    scope: CoroutineScope,
    codec: FrameCodec,
    config: FramedFactoryConfig,
): Connection {
    val inChannel = openReadChannel()
    val outChannel = openWriteChannel()
    val inbox =
        Channel<Buffer>(
            capacity = config.bufferingConfig.bufferCapacity,
            onBufferOverflow = config.bufferingConfig.overflowStrategy,
        )
    val reader = FrameReader(inChannel, codec, config.minFrameSize, config.maxFrameSize)
    val writer =
        BufferedFrameWriter(
            delegate = BasicFrameWriter(outChannel, codec),
            config = config.bufferingConfig,
        )

    val writerJob =
        scope.launch {
            try {
                while (isActive) {
                    select {
                        inbox.onReceiveCatching { received ->
                            val buffer = received.getOrNull() ?: return@onReceiveCatching
                            writer.write(buffer)

                            while (true) {
                                val next = inbox.tryReceive().getOrNull() ?: break
                                writer.write(next)
                            }
                        }
                        onTimeout(config.bufferingConfig.flushInterval) {
                            writer.flush()
                        }
                    }
                }
            } finally {
                writer.flush()
            }
        }

    val inboundFlow =
        flow {
            while (currentCoroutineContext().isActive) {
                inChannel.awaitContent()
                if (inChannel.isClosedForRead) break
                val frame = reader.readFrameOrNull()
                if (frame != null) emit(frame)
            }
        }

    return object : Connection {
        override fun inbound() = inboundFlow

        override suspend fun send(buffer: Buffer) {
            val res = inbox.trySend(buffer)
            if (res.isSuccess) return
            res.exceptionOrNull()?.let { throw it }

            if (config.bufferingConfig.overflowStrategy == BufferOverflow.SUSPEND) {
                config.sendTimeout?.let { timeout ->
                    withTimeout(timeout) { inbox.send(buffer) }
                } ?: inbox.send(buffer)
            }
        }

        override fun close() {
            runCatching { inbox.close() }
            runCatching { runBlocking { writerJob.cancel() } }
            runCatching { this@framed.close() }
        }
    }
}
