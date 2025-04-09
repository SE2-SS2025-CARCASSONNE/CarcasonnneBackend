package com.carcassonne.backend.security

import com.carcassonne.backend.repository.UserRepository
import com.carcassonne.backend.entity.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(
    private val userRepository: UserRepository //Inject dependency via constructor to access DB
): UserDetailsService {

    //Find and load user data from DB by username
    override fun loadUserByUsername(username: String): UserDetails {
        val user: User? = userRepository.findUserByUsername(username)

        if (user == null) { //Throws exception if user is not found
            throw UsernameNotFoundException("User \"$username\" not found.")
        }

        //Create Spring-specific User object if user is found
        return org.springframework.security.core.userdetails.User(
            user.username,
            user.password,
            ArrayList() //Authorities list to add roles later on
        )
    }
}
