package com.tkblackbelt.conquer4k.shared.network.crypto

import kotlin.collections.get
import kotlin.experimental.and
import kotlin.experimental.xor

class ClientPacketCipher : PacketCipher() {
    override fun rollOne(
        byte: Byte,
        counter: Int,
    ): Byte {
        var rolledByte = byte
        rolledByte = rolledByte xor staticKey1[counter and 0xFF] xor staticKey2[counter ushr 8 and 0xFF]
        rolledByte = (((rolledByte and 0xF.toByte()).toInt() shl 4) or (rolledByte.toInt() ushr 4 and 0xF)).toByte()
        rolledByte = rolledByte xor 0xAB.toByte()
        return rolledByte
    }
}
