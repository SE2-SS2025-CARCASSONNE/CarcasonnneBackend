package com.carcassonne.backend.controller

import com.carcassonne.backend.model.dto.LoginRequest
import com.carcassonne.backend.model.dto.RegisterRequest
import com.carcassonne.backend.entity.User
import com.carcassonne.backend.security.JwtUtil
import com.carcassonne.backend.security.CustomUserDetailsService
import com.carcassonne.backend.service.UserService
import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.web.bind.annotation.RequestMapping

@RestController
@RequestMapping("/api/auth") //Base bath
@Tag(name = "Auth", description = "User login and registration endpoints")
class AuthController(
    private val authManager: AuthenticationManager,
    private val jwtUtil: JwtUtil,
    private val userDetailsService: CustomUserDetailsService,
    private val userService: UserService,
    private val pwEncoder: BCryptPasswordEncoder,
) {

    @Operation(summary = "Sign in existing user")
    @PostMapping("/login") //User login endpoint
    fun login(@RequestBody loginRequest: LoginRequest): ResponseEntity<Map<String, String>> {
        return try {
            authManager.authenticate( //Checks credentials provided by user against DB
                UsernamePasswordAuthenticationToken(loginRequest.username, loginRequest.password))

            val userDetails = userDetailsService.loadUserByUsername((loginRequest.username))
            val token = jwtUtil.createToken(userDetails.username) //Creates JWT from user details

            ResponseEntity.ok(mapOf("token" to token)) //Returns HTTP 200 with JWT mapped to "token" key in response body

        } catch (_: BadCredentialsException) { //Handles invalid credentials (incorrect username or password)
            ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("message" to "Invalid username or password. Please try again."))
        } catch (_: Exception) { //Handles all other unexpected errors
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("message" to "Something went wrong. Please try again later."))
        }
    }

    @Operation(summary = "Register new user")
    @PostMapping("/register") //User registration endpoint
    fun register(@RequestBody registerRequest: RegisterRequest): ResponseEntity<Map<String, String>> {
        if (registerRequest.username.isBlank() || registerRequest.password.isBlank()) { //Check if username or password field is blank, return HTTP 400 if yes
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(mapOf("message" to "Please enter a username and password."))
        }
        if (userService.findUserByUsername(registerRequest.username) != null) { //Check if username already exists, return HTTP 400 if yes
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(mapOf("message" to "Username is not available. Please try again."))
        }

        return try {
            val hashedPw = pwEncoder.encode(registerRequest.password) //Hashes password using BCrypt
            val newUser = User(username = registerRequest.username, password = hashedPw) //Creates new user with provided credentials
            userService.saveUser(newUser) //Save new user to DB

            ResponseEntity.status(HttpStatus.CREATED) //Not really needed, easier to handle directly in frontend
                .body(mapOf("message" to "Account registered successfully. Please log in."))

        } catch (_: Exception) { //Handles all other unexpected errors
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("message" to "Something went wrong. Please try again later."))
        }
    }
}
