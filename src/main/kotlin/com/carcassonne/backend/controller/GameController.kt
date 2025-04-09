package com.carcassonne.backend.controller

import com.carcassonne.backend.entity.Game
import com.carcassonne.backend.repository.GameRepository
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*
import java.time.Instant

@RestController
@RequestMapping("/api/game")
@Tag(name = "Game", description = "Game control endpoints")
class GameController(
    private val gameRepository: GameRepository //Inject dependency via constructor
) {

    data class CreateGameRequest(val playerCount: Int)
    data class CreateGameResponse(val gameId: String)

    @Operation(summary = "Ping the server")
    @GetMapping("/ping")
    fun ping(): String = "pong"

    @Operation(summary = "Create new game")
    @PostMapping("/create")
    fun createGame(@RequestBody request: CreateGameRequest): CreateGameResponse {
        val code = generateGameId()

        val game = Game(
            gameCode = code,
            status = "WAITING",
            createdAt = Instant.now()
        )

        gameRepository.save(game)

        return CreateGameResponse(gameId = code)
    }
    //Generate ID for Game
    private fun generateGameId(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..6).map { chars.random() }.joinToString("")
    }
}