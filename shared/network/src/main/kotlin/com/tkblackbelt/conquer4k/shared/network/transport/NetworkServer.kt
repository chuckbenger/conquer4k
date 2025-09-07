package com.tkblackbelt.conquer4k.shared.network.transport

import java.io.Closeable

interface NetworkServer : Closeable {
    suspend fun start()
}
