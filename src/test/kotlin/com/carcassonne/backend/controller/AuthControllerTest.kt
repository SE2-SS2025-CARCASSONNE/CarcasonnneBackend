package com.carcassonne.backend.controller

import com.carcassonne.backend.model.dto.LoginRequest
import com.carcassonne.backend.model.dto.RegisterRequest
import com.carcassonne.backend.entity.User
import com.carcassonne.backend.security.CustomUserDetailsService
import com.carcassonne.backend.security.JwtUtil
import com.carcassonne.backend.service.UserService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.userdetails.User as SpringUser
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

class AuthControllerTest {

    private val authManager = mock(AuthenticationManager::class.java)
    private val jwtUtil = mock(JwtUtil::class.java)
    private val userDetailsService = mock(CustomUserDetailsService::class.java)
    private val userService = mock(UserService::class.java)
    private val passwordEncoder = mock(BCryptPasswordEncoder::class.java)

    private val authController = AuthController(authManager, jwtUtil, userDetailsService, userService, passwordEncoder)

    @Test
    fun loginReturnJwtAnd200UponSuccessTest() {
        val loginRequest = LoginRequest("name", "pw")
        val springUser = SpringUser("name", "encodedPw", emptyList())

        `when`(userDetailsService.loadUserByUsername("name")).thenReturn(springUser)
        `when`(jwtUtil.createToken("name")).thenReturn("mockJwt")

        val response = authController.login(loginRequest)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals("mockJwt", response.body?.get("token"))
    }

    @Test
    fun loginReturn401UponInvalidCredentialsTest() {
        val loginRequest = LoginRequest("wrongName", "wrongPw")
        `when`(authManager.authenticate(any())).thenThrow(BadCredentialsException("Invalid"))
        val response = authController.login(loginRequest)

        assertEquals(HttpStatus.UNAUTHORIZED, response.statusCode)
    }

    @Test
    fun loginReturn500UponOtherExceptionTest() {
        val loginRequest = LoginRequest("name", "pw")
        `when`(authManager.authenticate(any())).thenThrow(RuntimeException("DB error"))
        val response = authController.login(loginRequest)

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
    }

    @Test
    fun loginReturn400UponBlankFieldsTest() {
        val request = RegisterRequest("", "")
        val response = authController.register(request)

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
    }

    @Test
    fun loginReturn400UponUsernameAlreadyExistsTest() {
        val request = RegisterRequest("name", "pw")
        `when`(userService.findUserByUsername("name")).thenReturn(User(username = "name", password = "hashedPw"))
        val response = authController.register(request)

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
    }

    @Test
    fun registerReturn201AndSaveUserUponSuccessTest() {
        val request = RegisterRequest("name", "pw")
        `when`(userService.findUserByUsername("name")).thenReturn(null)
        `when`(passwordEncoder.encode("pw")).thenReturn("hashedPw")
        val response = authController.register(request)

        assertEquals(HttpStatus.CREATED, response.statusCode)
        verify(userService).saveUser(User(username="name", password = "hashedPw"))
    }

    @Test
    fun registerReturn500UponExceptionTest() {
        val request = RegisterRequest("name", "pw")
        `when`(userService.findUserByUsername("name")).thenReturn(null)
        `when`(passwordEncoder.encode("pw")).thenThrow(RuntimeException("DB error"))
        val response = authController.register(request)

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
    }
}
