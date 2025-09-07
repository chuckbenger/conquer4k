package com.tkblackbelt.conquer4k.shared.protocol.crypto

import kotlin.experimental.and
import kotlin.experimental.xor

class ServerPacketCipher : PacketCipher() {
    override fun rollOne(
        byte: Byte,
        counter: Int,
    ): Byte {
        var rolledByte = byte
        rolledByte = byte xor 0xAB.toByte()
        rolledByte = (((rolledByte and 0xF.toByte()).toInt() shl 4) or (rolledByte.toInt() shr 4 and 0xF)).toByte()
        rolledByte = rolledByte xor staticKey2[counter shr 8 and 0xFF] xor staticKey1[counter and 0xFF]
        return rolledByte
    }
}
