package com.carcassonne.backend.controller

import com.carcassonne.backend.model.GameState
import com.carcassonne.backend.service.GameManager
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.http.ResponseEntity

class GameRestControllerTest {

    private lateinit var gameManager: GameManager
    private lateinit var gameRestController: GameRestController
    private lateinit var userDetails: UserDetails

    @BeforeEach
    fun setUp() {
        //Mock GameManager dependency to avoid using real service
        gameManager = mock(GameManager::class.java)
        //Pass mocked dependency to GameRestController
        gameRestController = GameRestController(gameManager)
        //Simulate authenticated user
        userDetails = User("max", "muster123", emptyList())
    }

    @Test
    //getGame should return HTTP 200 and GameState corresponding to gameId
    fun getGameTest() {
        val exampleId = "A5KF01"
        val gameState = GameState(gameId = exampleId)

        `when`(gameManager.getOrCreateGame(exampleId)).thenReturn(gameState)
        val response: ResponseEntity<GameState> = gameRestController.getGame(exampleId, userDetails)

        assertEquals(200, response.statusCode.value())
        assertEquals(gameState, response.body)
        verify(gameManager, times(1)).getOrCreateGame(exampleId)
    }

    @Test
    //getGame should throw an exception if GameManager fails
    fun getGameThrowExceptionTest() {
        val exampleId = "Error001"

        `when`(gameManager.getOrCreateGame(exampleId)).thenThrow(RuntimeException("Critical error"))

        val exception = assertThrows(RuntimeException::class.java) {
            gameRestController.getGame(exampleId, userDetails)
        }

        assertEquals("Critical error", exception.message)
    }
}