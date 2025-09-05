package com.tkblackbelt.conquer4k.shared.protocol.serder

import com.tkblackbelt.conquer4k.shared.protocol.packets.IntPacket
import com.tkblackbelt.conquer4k.shared.protocol.packets.Packet
import com.tkblackbelt.conquer4k.shared.protocol.packets.StringPacket
import kotlinx.io.readShortLe
import kotlinx.io.writeShortLe
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.SerializersModule
import kotlin.jvm.java
import kotlin.jvm.javaClass

object PacketSerializer : KSerializer<Packet> {
    val serializersModule = SerializersModule {}

    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Packet")

    private val subtypeSerializers: Map<Short, KSerializer<out Packet>> =
        mapOf(
            1.toShort() to StringPacket.serializer(),
            2.toShort() to IntPacket.serializer(),
        )

    private val typeByClass: Map<Class<out Packet>, Short> =
        mapOf(
            StringPacket::class.java to 1.toShort(),
            IntPacket::class.java to 2.toShort(),
        )

    override fun deserialize(decoder: Decoder): Packet {
        val packetDecoder =
            decoder as? ReusablePacketDecoder
                ?: throw SerializationException("Deserializer must be PacketDecoder")
        val type = packetDecoder.source.readShortLe()
        val ser =
            subtypeSerializers[type]
                ?: throw SerializationException("Unknown packet type: $type")
        return decoder.decodeSerializableValue(ser)
    }

    override fun serialize(
        encoder: Encoder,
        value: Packet,
    ) {
        val pktEnc =
            encoder as? ReusablePacketEncoder
                ?: throw SerializationException("Serializer must be ReusablePacketEncoder")

        val cls = value.javaClass
        val type =
            typeByClass[cls]
                ?: throw SerializationException("Unknown packet class: ${cls.name}")

        pktEnc.dst.writeShortLe(type)

        @Suppress("UNCHECKED_CAST")
        val ser =
            (subtypeSerializers[type] as? KSerializer<Packet>)
                ?: throw SerializationException("No serializer registered for type: $type")

        encoder.encodeSerializableValue(ser, value)
    }
}
