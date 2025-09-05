package com.tkblackbelt.conquer4k.shared.network.transport

import com.tkblackbelt.conquer4k.shared.network.crypto.PacketCipher
import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.writeBuffer
import io.ktor.utils.io.writeByte
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.io.Buffer

private const val BATCH_BYTES = 16 * 1024 // flush threshold
private const val FLUSH_EVERY_MS = 10L // time-based flush

private suspend inline fun ByteWriteChannel.writeShortLe(v: Int) {
    writeByte((v and 0xFF).toByte())
    writeByte(((v ushr 8) and 0xFF).toByte())
}

internal fun CoroutineScope.launchOutboundWriter(
    writer: ByteWriteChannel,
    cipher: PacketCipher?,
    bufferCapacity: Int = Channel.BUFFERED,
): SendChannel<Buffer> {
    val inbox = Channel<Buffer>(capacity = bufferCapacity)

    val job =
        launch(context = Dispatchers.IO) {
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
                    val encLen = cipher?.encryptShortLe(sz.toShort()) ?: sz
                    cipher?.encrypt(body)
                    writer.writeShortLe(encLen.toInt())
                    writer.writeBuffer(body)
                    bytesSinceFlush += 2 + sz
            }

            try {
                while (isActive) {
                    val first = inbox.receiveCatching().getOrNull() ?: break
                    handleBuffer(first)

                    // opportunistically drain without suspending to reduce wakeups
                    while (bytesSinceFlush < BATCH_BYTES) {
                        val next = inbox.tryReceive().getOrNull() ?: break
                        handleBuffer(next)
                    }

                    flushIfNeeded()
                }
                flushIfNeeded(force = true)
            } finally {
                try {
                    writer.flushAndClose()
                } catch (_: Throwable) {
                }
            }
        }

    inbox.invokeOnClose { job.cancel() }
    return inbox
}