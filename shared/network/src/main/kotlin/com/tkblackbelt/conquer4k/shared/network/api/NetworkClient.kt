package com.tkblackbelt.conquer4k.shared.network.api

interface NetworkClient {
    suspend fun connect(): Connection
}
