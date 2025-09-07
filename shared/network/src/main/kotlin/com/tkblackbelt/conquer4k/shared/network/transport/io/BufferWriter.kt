package com.tkblackbelt.conquer4k.shared.network.transport.io

import com.tkblackbelt.conquer4k.shared.network.codec.FrameCodec
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.writeBuffer
import io.ktor.utils.io.writeByte
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.io.Buffer
import kotlin.coroutines.cancellation.CancellationException

private const val BATCH_BYTES = 16 * 1024 // flush threshold
private const val FLUSH_EVERY_MS = 10L // time-based flush

private suspend inline fun ByteWriteChannel.writeShortLe(v: Int) {
    writeByte((v and 0xFF).toByte())
    writeByte(((v ushr 8) and 0xFF).toByte())
}

private val logger = KotlinLogging.logger { }

internal fun CoroutineScope.launchOutboundWriter(
    writer: ByteWriteChannel,
    codec: FrameCodec,
    bufferCapacity: Int = Channel.BUFFERED,
): SendChannel<Buffer> {
    val inbox = Channel<Buffer>(capacity = bufferCapacity)

    val job =
        launch {
            var bytesSinceFlush = 0
            val intervalNs = FLUSH_EVERY_MS * 1_000_000L
            var nextFlushNs = System.nanoTime() + intervalNs

            suspend fun flushIfNeeded(force: Boolean = false) {
                val now = System.nanoTime()
                val timeDue = now >= nextFlushNs
                if (force || bytesSinceFlush >= BATCH_BYTES || (timeDue && bytesSinceFlush > 0)) {
                    writer.flush()
                    bytesSinceFlush = 0
                    nextFlushNs = now + intervalNs
                } else if (timeDue) {
                    nextFlushNs = now + intervalNs
                }
            }

            suspend fun handleBuffer(body: Buffer) {
                val sz = body.size.toInt()
                val encLen = codec.encodeLength(sz.toShort())
                val encBody = codec.encodeBody(body)
                writer.writeShortLe(encLen.toInt())
                writer.writeBuffer(encBody)
                bytesSinceFlush += 2 + sz
            }

            try {
                while (isActive) {
                    val first = inbox.receive()
                    handleBuffer(first)

                    // opportunistically drain without suspending to reduce wakeups
                    while (bytesSinceFlush < BATCH_BYTES) {
                        val next = inbox.tryReceive().getOrNull() ?: break
                        handleBuffer(next)
                    }

                    flushIfNeeded()
                }
                flushIfNeeded(force = true)
            } catch (_: CancellationException) {
            } catch (e: Exception) {
                logger.error(e) { "Buffer write failed" }
                // Fail fast: notify senders and cancel the connection scope
                runCatching { inbox.close(e) }
                this@launchOutboundWriter.cancel(CancellationException("outbound writer failed", e))
            } finally {
                runCatching { writer.flushAndClose() }
            }
        }

    inbox.invokeOnClose { job.cancel() }
    job.invokeOnCompletion { cause ->
        if (cause != null) runCatching { inbox.close(cause) }
    }
    return inbox
}
