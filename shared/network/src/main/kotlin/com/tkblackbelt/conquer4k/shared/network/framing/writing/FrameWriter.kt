package com.tkblackbelt.conquer4k.shared.network.framing.writing

import kotlinx.io.Buffer

interface FrameWriter {
    suspend fun write(buffer: Buffer): Int

    suspend fun flush()
}
