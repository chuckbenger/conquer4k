package com.tkblackbelt.conquer4k.shared.network.framing.writing

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.io.Buffer
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

data class BufferFrameWriterConfig(
    val bufferCapacity: Int = 100,
    val flushInterval: Duration = 10.milliseconds,
    val flushBytesSize: Int = 1024,
    val overflowStrategy: BufferOverflow = BufferOverflow.SUSPEND,
)

internal class BufferedFrameWriter(
    private val delegate: FrameWriter,
    private val config: BufferFrameWriterConfig,
) : FrameWriter {
    private var bytesSinceLastFlush: Int = 0
    private var intervalNanos = config.flushInterval.inWholeNanoseconds
    private var nextFlushNanos = System.nanoTime() + intervalNanos

    override suspend fun write(buffer: Buffer): Int {
        val written = delegate.write(buffer)
        bytesSinceLastFlush += written
        flushIfNeeded()
        return written
    }

    override suspend fun flush() {
        if (bytesSinceLastFlush > 0) {
            delegate.flush()
            bytesSinceLastFlush = 0
        }
        nextFlushNanos = System.nanoTime() + intervalNanos
    }

    private suspend fun flushIfNeeded() {
        val now = System.nanoTime()
        val timeDue = now >= nextFlushNanos

        if (bytesSinceLastFlush >= config.flushBytesSize || (timeDue && bytesSinceLastFlush > 0)) {
            delegate.flush()
            bytesSinceLastFlush = 0
            nextFlushNanos = now + intervalNanos
        } else if (timeDue) {
            nextFlushNanos = now + intervalNanos
        }
    }
}
