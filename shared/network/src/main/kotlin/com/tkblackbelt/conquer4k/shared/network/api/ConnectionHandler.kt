package com.tkblackbelt.conquer4k.shared.network.api

fun interface ConnectionHandler {
    suspend fun handle(connection: Connection)
}
