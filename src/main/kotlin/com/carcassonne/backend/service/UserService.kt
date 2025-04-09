package com.carcassonne.backend.service

import com.carcassonne.backend.entity.User
import com.carcassonne.backend.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository
) {

    fun findUserByUsername(username: String): User? {
        return userRepository.findUserByUsername(username)
    }

    fun saveUser(user: User) {
        userRepository.save(user)
    }
}