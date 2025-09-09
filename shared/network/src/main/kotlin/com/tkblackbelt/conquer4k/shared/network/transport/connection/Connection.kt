package com.tkblackbelt.conquer4k.shared.network.transport.connection

import kotlinx.coroutines.flow.Flow
import kotlinx.io.Buffer

interface Connection : AutoCloseable {
    fun inbound(): Flow<Buffer>
    suspend fun send(buffer: Buffer)
}
