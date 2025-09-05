package com.tkblackbelt.conquer4k.shared.network.api

import kotlinx.coroutines.flow.Flow
import kotlinx.io.Buffer
import com.tkblackbelt.conquer4k.shared.network.crypto.PacketCipher

interface Connection : AutoCloseable {
    fun setCipher(cipher: PacketCipher?)

    fun incomingFrames(): Flow<Buffer>

    suspend fun sendFrame(frame: Buffer)

    override fun close()
}
