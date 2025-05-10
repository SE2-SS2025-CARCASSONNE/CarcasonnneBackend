package com.carcassonne.backend.service

import com.carcassonne.backend.entity.User
import com.carcassonne.backend.repository.UserRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*

class UserServiceTest {

    private lateinit var userRepository: UserRepository
    private lateinit var userService: UserService

    @BeforeEach
    fun setUp() {
        userRepository = mock(UserRepository::class.java)
        userService = UserService(userRepository)
    }

    @Test
    fun findUserReturnUserIfExistsTest() {
        val mockUser = User(username = "name", password = "pw")
        `when`(userRepository.findUserByUsername("name")).thenReturn(mockUser)
        val result = userService.findUserByUsername("name")

        assertEquals(mockUser, result)
        verify(userRepository).findUserByUsername("name")
    }

    @Test
    fun findUserReturnNullIfNotExistsTest() {
        `when`(userRepository.findUserByUsername("wrongName")).thenReturn(null)
        val result = userService.findUserByUsername("wrongName")

        assertNull(result)
        verify(userRepository).findUserByUsername("wrongName")
    }

    @Test
    fun saveUserCallRepoSaveTest() {
        val newUser = User(username = "name", password = "pw")
        userService.saveUser(newUser)

        verify(userRepository).save(newUser)
    }
}
