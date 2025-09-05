package com.tkblackbelt.conquer4k.shared.protocol.serder

import com.tkblackbelt.conquer4k.shared.protocol.packets.Packet
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.io.Buffer
import kotlinx.io.Source
import kotlinx.io.readDoubleLe
import kotlinx.io.readFloatLe
import kotlinx.io.readIntLe
import kotlinx.io.readLongLe
import kotlinx.io.readShortLe
import kotlinx.io.readString
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.AbstractDecoder
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.modules.SerializersModule

@OptIn(ExperimentalSerializationApi::class)
internal class ReusablePacketDecoder : AbstractDecoder() {
    override lateinit var serializersModule: SerializersModule
        private set
    lateinit var source: Source
        private set
    private var index: Int = 0

    fun reset(
        module: SerializersModule,
        source: Source,
    ) {
        this.serializersModule = module
        this.source = source
        this.index = 0
    }

    override fun decodeNotNullMark(): Boolean = true

    override fun decodeBoolean(): Boolean = (source.readByte().toInt() and 0xFF) != 0

    override fun decodeByte(): Byte = source.readByte()

    override fun decodeShort(): Short = source.readShortLe()

    override fun decodeInt(): Int = source.readIntLe()

    override fun decodeLong(): Long = source.readLongLe()

    override fun decodeFloat(): Float = source.readFloatLe()

    override fun decodeDouble(): Double = source.readDoubleLe()

    override fun decodeChar(): Char = source.readShortLe().toInt().toChar()

    override fun decodeString(): String {
        val len = source.readByte().toInt() and 0xFF
        if (len == 0) return ""
        return source.readString(len.toLong(), Charsets.US_ASCII)
    }

    override fun decodeEnum(descriptor: SerialDescriptor): Int = decodeInt()

    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder = this

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int =
        if (index < descriptor.elementsCount) index++ else CompositeDecoder.DECODE_DONE

    override fun decodeInline(descriptor: SerialDescriptor): Decoder = this
}

internal object PacketDecoderPool {
    private val local = ThreadLocal<ReusablePacketDecoder>()

    fun obtain(): ReusablePacketDecoder = local.get() ?: ReusablePacketDecoder().also { local.set(it) }
}

private val logger = KotlinLogging.logger { }

fun Flow<Buffer>.decodePacket(): Flow<Packet> =
    map { buffer ->
        val d = PacketDecoderPool.obtain()
        d.reset(PacketSerializer.serializersModule, buffer)
        d.decodeSerializableValue(PacketSerializer)
    }
