package com.tkblackbelt.conquer4k.shared.network.api

import com.tkblackbelt.conquer4k.shared.network.crypto.PacketCipher
import kotlinx.coroutines.flow.Flow
import kotlinx.io.Buffer

interface Connection : AutoCloseable {
    fun setCipher(cipher: PacketCipher?)

    fun incomingFrames(): Flow<Buffer>

    suspend fun sendFrame(frame: Buffer)

    override fun close()
}
