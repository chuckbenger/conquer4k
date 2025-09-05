package com.tkblackbelt.conquer4k.shared.network.api

import java.io.Closeable

interface NetworkServer : Closeable {
    suspend fun start()
}