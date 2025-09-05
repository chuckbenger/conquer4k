package com.tkblackbelt.conquer4k.shared.protocol.serder

import kotlinx.io.Buffer
import kotlinx.io.writeDoubleLe
import kotlinx.io.writeFloatLe
import kotlinx.io.writeIntLe
import kotlinx.io.writeLongLe
import kotlinx.io.writeShortLe
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.AbstractEncoder
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.SerializersModule
import java.nio.charset.StandardCharsets.US_ASCII

@OptIn(ExperimentalSerializationApi::class)
internal class ReusablePacketEncoder : AbstractEncoder() {
    override lateinit var serializersModule: SerializersModule
        private set
    lateinit var dst: Buffer
        private set
    private var index: Int = 0

    fun reset(
        module: SerializersModule,
        dst: Buffer,
    ) {
        this.serializersModule = module
        this.dst = dst
        this.index = 0
    }

    override fun encodeNotNullMark() { // all fields considered present
    }

    override fun encodeNull() { // protocol has no null marker; caller should avoid
    }

    override fun encodeBoolean(value: Boolean) {
        dst.writeByte(if (value) 1 else 0)
    }

    override fun encodeByte(value: Byte) {
        dst.writeByte(value)
    }

    override fun encodeShort(value: Short) {
        dst.writeShortLe(value)
    }

    override fun encodeInt(value: Int) {
        dst.writeIntLe(value)
    }

    override fun encodeLong(value: Long) {
        dst.writeLongLe(value)
    }

    override fun encodeFloat(value: Float) {
        dst.writeFloatLe(value)
    }

    override fun encodeDouble(value: Double) {
        dst.writeDoubleLe(value)
    }

    override fun encodeChar(value: Char) {
        dst.writeShortLe(value.code.toShort())
    }

    override fun encodeString(value: String) {
        if (value.isEmpty()) {
            dst.writeByte(0)
            return
        }
        val bytes = value.toByteArray(US_ASCII)
        require(bytes.size <= 0xFF) { "String too long (${bytes.size} > 255) for 1-byte length prefix" }
        dst.writeByte(bytes.size.toByte())
        dst.write(bytes, 0, bytes.size)
    }

    override fun encodeEnum(
        enumDescriptor: SerialDescriptor,
        index: Int,
    ) {
        encodeInt(index) // matches decoder: decodeEnum via decodeInt()
    }

    override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder = this

    override fun endStructure(descriptor: SerialDescriptor) { // no-op
    }

    override fun encodeElement(
        descriptor: SerialDescriptor,
        index: Int,
    ): Boolean {
        this.index = index
        return true
    }

    override fun encodeInline(descriptor: SerialDescriptor): Encoder = this
}

internal object PacketEncoderPool {
    private val local = ThreadLocal<ReusablePacketEncoder>()

    fun obtain(): ReusablePacketEncoder = local.get() ?: ReusablePacketEncoder().also { local.set(it) }
}
