package com.tkblackbelt.conquer4k.shared.network.codec

import kotlinx.io.Buffer

interface FrameCodec {
    fun encodeLength(length: Short): Short

    fun decodeLength(length: Short): Short

    fun encodeBody(buffer: Buffer): Buffer

    fun decodeBody(buffer: Buffer): Buffer
}

object PlainCodec : FrameCodec {
    override fun encodeLength(length: Short) = length

    override fun decodeLength(length: Short) = length

    override fun encodeBody(buffer: Buffer) = buffer

    override fun decodeBody(buffer: Buffer) = buffer
}
