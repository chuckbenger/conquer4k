package com.tkblackbelt.conquer4k.shared.network.framing.codec

import com.tkblackbelt.conquer4k.shared.protocol.crypto.PacketCipher
import kotlinx.io.Buffer

class CipherFrameCodec(
    private val cipher: PacketCipher,
) : FrameCodec {
    override fun encodeLength(length: Short): Short = cipher.encryptShortLe(length)

    override fun decodeLength(length: Short): Short = cipher.decryptShortLe(length)

    override fun encodeBody(buffer: Buffer): Buffer = buffer.apply { cipher.encrypt(this) }

    override fun decodeBody(buffer: Buffer): Buffer = buffer.apply { cipher.decrypt(this) }
}

fun PacketCipher.asFrameCodec(): FrameCodec = CipherFrameCodec(this)
