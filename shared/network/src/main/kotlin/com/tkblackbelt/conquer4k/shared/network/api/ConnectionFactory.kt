package com.tkblackbelt.conquer4k.shared.network.api

import io.ktor.network.sockets.Socket
import kotlinx.coroutines.CoroutineScope

fun interface ConnectionFactory {
    fun create(
        scope: CoroutineScope,
        socket: Socket,
    ): Connection
}
