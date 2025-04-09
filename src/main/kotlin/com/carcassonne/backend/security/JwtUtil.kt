package com.carcassonne.backend.security

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import java.security.Key
import java.util.*
import javax.crypto.spec.SecretKeySpec

@Component
class JwtUtil(
    //Placeholder to inject jwt.secret from .properties
    @Value("\${jwt.secret}")
    private val secret: String
) {

    //Convert String to SecretKey (subtype of Key) object
    private val key: Key = SecretKeySpec(secret.toByteArray(), SignatureAlgorithm.HS256.jcaName)

    //Create JWT from username and sign it with secret key
    fun createToken(username: String): String {
        val currentTime = Date()
        val expiryTime = Date(currentTime.time + 1000*60*60*24) //JWT expires in 24h
        return Jwts.builder()
            .setSubject(username)
            .setIssuedAt(currentTime)
            .setExpiration(expiryTime)
            .signWith(key).compact()
    }

    //Parse JWT and extract username
    fun extractUsername(token: String): String {
        return Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .body.subject //Username
    }

    //Check if JWT is valid (correct username & not expired)
    fun tokenValid(token: String, userDetails: UserDetails): Boolean {
        val username = extractUsername(token)
        return (username == userDetails.username && !tokenExpired(token))
    }

    //Parse JWT and extract expiration time (helper function)
    fun extractExpiration(token: String): Date {
        return Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .body.expiration //Expiration time
    }

    //Check if JWT is expired (helper function)
    fun tokenExpired(token: String): Boolean {
        val currentTime = Date()
        return extractExpiration(token).before(currentTime)
    }
}
