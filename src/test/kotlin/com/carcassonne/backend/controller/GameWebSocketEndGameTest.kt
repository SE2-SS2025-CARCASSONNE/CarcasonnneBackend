package com.carcassonne.backend.controller

import com.carcassonne.backend.model.*
import com.carcassonne.backend.repository.GameRepository
import com.carcassonne.backend.service.GameManager
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.messaging.simp.SimpMessagingTemplate

class GameWebSocketEndGameTest {

    private val mockGameManager = mock<GameManager>()
    private val mockMessagingTemplate = mock<SimpMessagingTemplate>()
    private val mockGameRepository = mock<GameRepository>()

    private val controller = GameWebSocketController(mockGameManager, mockMessagingTemplate, mockGameRepository)

    @Test
    fun `handle end_game sends game_over message with winner`() {
        val gameId = "game123"
        val player1 = Player(id = "player1", user_id = 1, score = 10, remainingMeeple = 5)
        val player2 = Player(id = "player2", user_id = 2, score = 5, remainingMeeple = 5)
        val players = mutableListOf(player1, player2)
        val gameState = GameState(gameId = gameId, players = players, status = GamePhase.FINISHED)

        whenever(mockGameManager.getOrCreateGame(gameId)).thenReturn(gameState)
        whenever(mockGameManager.endGame(gameId)).thenReturn("player1")

        val message = GameMessage(type = "end_game", gameId = gameId, player = "player1")
        controller.handle(message)

        verify(mockMessagingTemplate).convertAndSend(
            eq("/topic/game/$gameId"),
            argThat<Any> { payload ->
                val map = payload as? Map<*, *> ?: return@argThat false
                map["type"] == "game_over" &&
                        map["winner"] == "player1" &&
                        (map["scores"] as? List<*>)?.any {
                            val entry = it as? Map<*, *> ?: return@any false
                            entry["player"] == "player1" && entry["score"] == 10
                        } == true &&
                        (map["scores"] as? List<*>)?.any {
                            val entry = it as? Map<*, *> ?: return@any false
                            entry["player"] == "player2" && entry["score"] == 5
                        } == true
            }
        )
    }
}
