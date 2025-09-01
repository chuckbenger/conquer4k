package com.tkblackbelt.services.auth

/**
 * Entry point for the Auth service.
 * For now, this only boots a placeholder server and logs lifecycle events.
 */
fun main() {
    val service = AuthServer()
    service.start()
    Runtime.getRuntime().addShutdownHook(Thread { service.stop() })
}

class AuthServer {
    fun start() {
        println("[auth] Starting Auth service...")
        // TODO: Wire with shared:network server when ready
    }

    fun stop() {
        println("[auth] Stopping Auth service...")
    }
}
