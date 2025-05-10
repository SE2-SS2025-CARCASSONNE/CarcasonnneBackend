package com.carcassonne.backend.config

import com.carcassonne.backend.security.JwtFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
class SecurityConfig(
    private val jwtFilter: JwtFilter //Inject dependency via constructor
) {

    @Bean
    //Tells Spring how to secure the API endpoints
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() } //CSRF protection not needed for JWTs

            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers(//Swagger endpoints should be restricted in production
                        "/api/game/**", //Just for demo!!!
                        "/ws/**", //Just for demo!!!
                        "/api/auth/**",
                        "/api/game/ping",
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "swagger-resources/**",
                        "/webjars/**")
                    .permitAll() //Auth endpoints open to everybody (to login/register)
                    .anyRequest().authenticated() //Other endpoints only open to authenticated users
            }

            .sessionManagement { session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS) //Disable sessions, use JWTs instead
            }

            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter::class.java) //Add custom JwtFilter to check if JWT is valid before checking username/password

        return http.build()
    }

    @Bean
    //Default Spring authentication manager
    fun authenticationManager(config: AuthenticationConfiguration): AuthenticationManager {
        return config.authenticationManager
    }

    @Bean
    //Used to hash passwords for secure storage and verification
    fun passwordEncoder(): BCryptPasswordEncoder {
        return BCryptPasswordEncoder()
    }
}
