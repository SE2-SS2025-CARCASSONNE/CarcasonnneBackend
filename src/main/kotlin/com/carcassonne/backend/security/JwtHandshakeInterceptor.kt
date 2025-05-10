package com.carcassonne.backend.security

import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.server.HandshakeInterceptor
import org.springframework.security.core.userdetails.UserDetailsService

@Component
//Custom interceptor that runs once during initial WebSocket handshake (like JwtFilter but for WebSockets)
class JwtHandshakeInterceptor(
    private val jwtUtil: JwtUtil,
    private val userDetailsService: UserDetailsService
) : HandshakeInterceptor {

    override fun beforeHandshake(
        request: ServerHttpRequest,
        response: ServerHttpResponse,
        wsHandler: WebSocketHandler,
        attributes: MutableMap<String, Any>
    ): Boolean {
        println(">>> [Handshake] Incoming handshake request...")
        println(">>> [Handshake] Headers: ${request.headers}")

        val authHeader = request.headers.getFirst("Authorization") //Read auth header from HTTP request
        println(">>> [Handshake] Authorization header: $authHeader") // NEW LINE

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            val token = authHeader.substring(7)

            try {
                val username = jwtUtil.extractUsername(token)
                println(">>> [Handshake] Extracted username from token: $username")
                val userDetails = userDetailsService.loadUserByUsername(username)

                return if (jwtUtil.tokenValid(token, userDetails)) {
                    attributes["username"] = username
                    println(">>> [Handshake] Token is valid, user connected as $username")
                    true
                } else {
                    println(">>> [Handshake] Token is INVALID")
                    false
                }
            } catch (e: Exception) {
                println(">>> [Handshake] Exception during token validation: ${e.message}")
                return false
            }
        }

        println(">>> [Handshake] Authorization header missing or malformed")
        return false
    }

    override fun afterHandshake(
        request: ServerHttpRequest,
        response: ServerHttpResponse,
        wsHandler: WebSocketHandler,
        ex: Exception?
    ) {
        //Nothing to do after handshake
    }
}