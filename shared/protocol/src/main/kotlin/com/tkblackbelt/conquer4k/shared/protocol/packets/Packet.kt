package com.tkblackbelt.conquer4k.shared.protocol.packets

import com.tkblackbelt.conquer4k.shared.protocol.serder.PacketSerializer
import kotlinx.serialization.Serializable

@Serializable(with = PacketSerializer::class)
sealed class Packet

@Serializable
data class StringPacket(
    val message: String,
) : Packet()

@Serializable
data class IntPacket(
    val value: Int,
) : Packet()
