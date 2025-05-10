package com.carcassonne.backend.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
//Custom filter running once per HTTP request to extract and validate JWTs
class JwtFilter(
    private val jwtUtil: JwtUtil, //Inject dependency via constructor
    private val userDetailsService: UserDetailsService
): OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authHeader = request.getHeader("Authorization") //Read auth header from HTTP request

        if (authHeader!=null && authHeader.startsWith("Bearer ")) { //Check if authHeader exists and Bearer scheme is used
            val token = authHeader.substring(7) //Remove "Bearer " prefix to extract JWT string

            try {
                val username = jwtUtil.extractUsername(token)

                if (SecurityContextHolder.getContext().authentication == null) { //Validate that no other user is logged in on this request
                    val userDetails: UserDetails = userDetailsService.loadUserByUsername(username) //Load user details from database

                    if (jwtUtil.tokenValid(token, userDetails)) { //Check if JWT is valid for this user
                        val authToken = UsernamePasswordAuthenticationToken( //Create Spring Security auth token
                            userDetails, null, userDetails.authorities)

                        SecurityContextHolder.getContext().authentication = authToken //Store authToken in security context
                    }
                }
            } catch (_: Exception) {
                //Spring Security handles rejection later
            }
        }
        filterChain.doFilter(request, response)
    }
}
