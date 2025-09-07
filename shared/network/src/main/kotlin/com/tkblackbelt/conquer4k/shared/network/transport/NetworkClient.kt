package com.tkblackbelt.conquer4k.shared.network.transport

interface NetworkClient {
    suspend fun connect(): ByteTransport
}
