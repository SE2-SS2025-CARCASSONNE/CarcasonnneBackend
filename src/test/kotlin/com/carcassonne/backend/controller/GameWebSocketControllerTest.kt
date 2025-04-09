package com.carcassonne.backend.controller

import com.carcassonne.backend.model.GameMessage
import com.carcassonne.backend.model.GameState
import com.carcassonne.backend.repository.GameRepository
import com.carcassonne.backend.service.GameManager
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.springframework.messaging.simp.SimpMessagingTemplate

class GameWebSocketControllerTest {

    private val mockGameManager = mock(GameManager::class.java)
    private val mockMessagingTemplate = mock(SimpMessagingTemplate::class.java)
    private val mockGameRepository = mock(GameRepository::class.java)

    private val controller = GameWebSocketController(mockGameManager, mockMessagingTemplate, mockGameRepository)

    @Test
    fun `handle join_game sends player_joined message`() {
        // Arrange
        val gameId = "testgame"
        val player = "player"
        val playersList = mutableListOf<String>()

        // Create a mock GameState
        val mockGameState = mock(GameState::class.java)

        `when`(mockGameState.players).thenReturn(playersList)
        `when`(mockGameState.getCurrentPlayer()).thenReturn(player)
        `when`(mockGameManager.getOrCreateGame(gameId)).thenReturn(mockGameState)

        val message = GameMessage(
            type = "join_game",
            gameId = gameId,
            player = player
        )

        // Act
        controller.handle(message)

        // Assert
        verify(mockMessagingTemplate).convertAndSend(
            eq("/topic/game/$gameId"),
            argThat<Any> { payload ->
                val map = payload as Map<*, *>
                map["type"] == "player_joined" &&
                        map["player"] == player &&
                        (map["players"] as List<*>).contains(player)
            }
        )
    }
}

