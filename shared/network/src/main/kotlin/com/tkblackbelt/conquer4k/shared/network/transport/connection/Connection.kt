package com.tkblackbelt.conquer4k.shared.network.transport.connection

import kotlinx.coroutines.flow.Flow
import kotlinx.io.Buffer

interface Connection : AutoCloseable {
    fun incomingFrames(): Flow<Buffer>

    suspend fun sendFrame(frame: Buffer)
}
