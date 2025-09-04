package com.tkblackbelt.conquer4k.services.auth

import com.tkblackbelt.conquer4k.shared.network.server.TcpServer
import com.tkblackbelt.conquer4k.shared.network.server.TcpServerConfig
import kotlinx.coroutines.runBlocking

/**
 * Entry point for the Auth service.
 * For now, this only boots a placeholder server and logs lifecycle events.
 */
fun main() {
    val tcpServer = TcpServer(TcpServerConfig("0.0.0.0", 8921))
    runBlocking {
        tcpServer.start()
        while (true) {
            Thread.sleep(1000)
        }
    }
}
