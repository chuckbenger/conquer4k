package com.tkblackbelt.conquer4k.shared.network.transport

fun interface TransportHandler {
    suspend fun handle(transport: ByteTransport)
}
