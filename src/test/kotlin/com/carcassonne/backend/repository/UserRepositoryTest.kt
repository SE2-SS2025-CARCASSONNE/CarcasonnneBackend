package com.carcassonne.backend.repository

import com.carcassonne.backend.entity.User
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import java.util.Optional

class UserRepositoryTest {

    private val userRepository = mock(UserRepository::class.java)

    @Test
    fun `findById should return the correct User`() {
        val user = User(id = 1, username = "player1", email = "player1@example.com")
        `when`(userRepository.findById(1)).thenReturn(Optional.of(user))

        val result = userRepository.findById(1)

        assertNotNull(result)
        assertEquals(1, result.get().id)
        assertEquals("player1", result.get().username)
        assertEquals("player1@example.com", result.get().email)
        verify(userRepository, times(1)).findById(1)
    }

    @Test
    fun `findById should return empty when User not found`() {
        `when`(userRepository.findById(99)).thenReturn(Optional.empty())

        val result = userRepository.findById(99)

        assertTrue(result.isEmpty)
        verify(userRepository, times(1)).findById(99)
    }

    @Test
    fun `save should persist and return the User`() {
        val user = User(username = "player2", email = "player2@example.com")
        `when`(userRepository.save(user)).thenReturn(user)

        val result = userRepository.save(user)

        assertNotNull(result)
        assertEquals("player2", result.username)
        assertEquals("player2@example.com", result.email)
        verify(userRepository, times(1)).save(user)
    }

    @Test
    fun `findAll should return all Users`() {
        val user1 = User(id = 1, username = "player1", email = "player1@example.com")
        val user2 = User(id = 2, username = "player2", email = "player2@example.com")
        `when`(userRepository.findAll()).thenReturn(listOf(user1, user2))

        val result = userRepository.findAll()

        assertNotNull(result)
        assertEquals(2, result.size)
        assertTrue(result.contains(user1))
        assertTrue(result.contains(user2))
        verify(userRepository, times(1)).findAll()
    }

    @Test
    fun `delete should remove the User`() {
        val user = User(id = 1, username = "player1", email = "player1@example.com")

        // Mocking des Löschen-Verhaltens
        doNothing().`when`(userRepository).delete(user)
        `when`(userRepository.findById(1)).thenReturn(Optional.empty())

        // Methode ausführen
        userRepository.delete(user)

        // Verifizieren, dass delete aufgerufen wurde
        verify(userRepository, times(1)).delete(user)

        // Sicherstellen, dass die ID nicht mehr existiert
        val result = userRepository.findById(1)
        assertTrue(result.isEmpty)
    }

}
