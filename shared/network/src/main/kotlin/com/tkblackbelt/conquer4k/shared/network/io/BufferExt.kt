package com.tkblackbelt.conquer4k.shared.network.io

import kotlinx.io.Buffer
import kotlinx.io.readByteArray
import java.nio.charset.Charset

@OptIn(ExperimentalStdlibApi::class)
fun Buffer.toDebugString(
    charset: Charset = Charset.defaultCharset(),
    consume: Boolean = false,
): String =
    buildString {
        val bytes =
            if (consume) {
                readByteArray()
            } else {
                peek().readByteArray()
            }
        append("[${bytes.joinToString()}]")
        append(" ${bytes.toHexString()}")
        append(" \"${bytes.toString(charset)}\"")
    }

