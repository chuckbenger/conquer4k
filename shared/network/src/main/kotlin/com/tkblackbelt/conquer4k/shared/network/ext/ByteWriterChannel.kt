package com.tkblackbelt.conquer4k.shared.network.ext

import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.writeByte

suspend inline fun ByteWriteChannel.writeShortLe(v: Int) {
    writeByte((v and 0xFF).toByte())
    writeByte(((v ushr 8) and 0xFF).toByte())
}
