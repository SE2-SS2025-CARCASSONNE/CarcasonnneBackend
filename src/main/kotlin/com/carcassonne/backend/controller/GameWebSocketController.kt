package com.carcassonne.backend.controller

import com.carcassonne.backend.model.GameMessage
import com.carcassonne.backend.repository.GameRepository
import com.carcassonne.backend.service.GameManager
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Controller

@Controller
class GameWebSocketController(
    private val gameManager: GameManager,
    private val messagingTemplate: SimpMessagingTemplate,
    private val gameRepository: GameRepository
) {

    @MessageMapping("/game/send") // from client to /app/game/send
    fun handle(@Payload msg: GameMessage) {
        when (msg.type) {
            "join_game" -> {
                val game = gameManager.getOrCreateGame(msg.gameId)
                if (!game.players.contains(msg.player)) {
                    game.players.add(msg.player)
                }

                val payload = mapOf(
                    "type" to "player_joined",
                    "player" to msg.player,
                    "players" to game.players,
                    "currentPlayer" to game.getCurrentPlayer()
                )

                messagingTemplate.convertAndSend("/topic/game/${msg.gameId}", payload)
            }

            "place_tile" -> {
                val game = gameManager.placeTile(msg.gameId, msg.tile!!, msg.player)
                if (game != null) {
                    val payload = mapOf(
                        "type" to "board_update",
                        "tile" to msg.tile,
                        "player" to msg.player,
                        "nextPlayer" to game.getCurrentPlayer()
                    )
                    messagingTemplate.convertAndSend("/topic/game/${msg.gameId}", payload)
                } else {
                    val error = mapOf("type" to "error", "message" to "Invalid move or not your turn")
                    messagingTemplate.convertAndSend("/topic/game/${msg.gameId}", error)
                }
            }
            "start_game" -> {
                println(">>> [Backend] Received start_game for ${msg.gameId}")

                val game = gameManager.getOrCreateGame(msg.gameId)
                game.status = "STARTED"

                // Update DB
                try {
                    gameRepository.updateStatusByGameCode(msg.gameId, "STARTED")
                    println(">>> [Backend] Game status updated in DB")
                } catch (e: Exception) {
                    println(">>> [Backend] ERROR updating DB: ${e.message}")
                }

                // Notify clients
                val payload = mapOf(
                    "type" to "game_started",
                    "gameId" to msg.gameId
                )
                println(">>> [Backend] Sending game_started to /topic/game/${msg.gameId} with $payload")
                messagingTemplate.convertAndSend("/topic/game/${msg.gameId}", payload)
            }
        }
    }
}