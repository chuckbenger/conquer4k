package com.tkblackbelt.conquer4k.shared.network.transport.io

import com.tkblackbelt.conquer4k.shared.network.codec.FrameCodec
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.utils.io.ByteWriteChannel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.onTimeout
import kotlinx.coroutines.selects.select
import kotlinx.io.Buffer
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

private val logger = KotlinLogging.logger { }

class BufferedFrameWriter(
    private val delegate: FrameWriter,
    private val config: BufferFrameWriterConfig = BufferFrameWriterConfig(),
) {
    private var bytesSinceLastFlush: Int = 0
    private var intervalNanos = config.flushInternal.inWholeNanoseconds
    private var nextFlushNanos = System.nanoTime() + intervalNanos

    suspend fun write(buffer: Buffer) {
        delegate.write(buffer)
        flushIfNeeded()
    }

    suspend fun forceFlush() {
        if (bytesSinceLastFlush > 0) {
            delegate.flush()
            bytesSinceLastFlush = 0
        }
        nextFlushNanos = System.nanoTime() + intervalNanos
    }

    private suspend fun flushIfNeeded() {
        val now = System.nanoTime()
        val timeDue = now >= nextFlushNanos

        if (bytesSinceLastFlush >= config.flushBatchSize || (timeDue && bytesSinceLastFlush > 0)) {
            delegate.flush()
            bytesSinceLastFlush = 0
            nextFlushNanos = now + intervalNanos
        } else if (timeDue) {
            nextFlushNanos = now + intervalNanos
        }
    }
}

data class BufferFrameWriterConfig(
    val bufferCapacity: Int = 100,
    val flushInternal: Duration = 10.milliseconds,
    val flushBatchSize: Int = 1024,
)

@OptIn(ExperimentalCoroutinesApi::class)
internal fun CoroutineScope.launchOutboundWriter(
    writer: ByteWriteChannel,
    codec: FrameCodec,
    config: BufferFrameWriterConfig,
): SendChannel<Buffer> {
    val inbox = Channel<Buffer>(capacity = config.bufferCapacity)
    val writer =
        BufferedFrameWriter(
            delegate = FrameWriter(writer, codec),
            config = config,
        )

    val job =
        launch {
            try {
                while (isActive) {
                    select {
                        inbox.onReceiveCatching { res ->
                            val buffer = res.getOrNull() ?: return@onReceiveCatching
                            writer.write(buffer)

                            while (true) {
                                val next = inbox.tryReceive().getOrNull() ?: break
                                writer.write(next)
                            }
                        }
                        onTimeout(config.flushInternal) {
                            writer.forceFlush()
                        }
                    }
                }
            } catch (_: CancellationException) {
            } catch (e: Exception) {
                logger.error(e) { "Buffer write failed" }
                // Fail fast: notify senders and cancel the connection scope
                runCatching { inbox.close(e) }
                this@launchOutboundWriter.cancel(CancellationException("outbound writer failed", e))
            } finally {
                writer.forceFlush()
            }
        }

    inbox.invokeOnClose { job.cancel() }
    job.invokeOnCompletion { cause ->
        if (cause != null) runCatching { inbox.close(cause) }
    }
    return inbox
}
