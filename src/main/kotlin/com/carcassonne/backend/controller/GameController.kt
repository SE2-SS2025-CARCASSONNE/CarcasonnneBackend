package com.carcassonne.backend.controller

import com.carcassonne.backend.entity.Game
import com.carcassonne.backend.model.Tile
import com.carcassonne.backend.model.Player
import com.carcassonne.backend.repository.GameRepository
import com.carcassonne.backend.service.GameManager
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import java.time.Instant

@RestController
@RequestMapping("/api/game")
@Tag(name = "Game", description = "Game control endpoints")
class GameController(
    private val gameRepository: GameRepository, //Inject dependency via constructor
    private val gameManager: GameManager
) {

    data class CreateGameRequest(val playerCount: Int)
    data class CreateGameResponse(val gameId: String)

    @Operation(summary = "Ping the server")
    @GetMapping("/ping")
    fun ping(): String = "pong"

    @Operation(summary = "Create new game")
    @PostMapping("/create")
    fun createGame(
        @RequestBody request: CreateGameRequest,
        @AuthenticationPrincipal user: UserDetails //Inject authenticated user
    ): CreateGameResponse {
        val code = generateGameId()

        val game = Game(
            gameCode = code,
            status = "WAITING",
            createdAt = Instant.now()
        )

        gameRepository.save(game)
        gameManager.createGameWithHost(code, user.username)

        return CreateGameResponse(gameId = code)
    }
    //Generate ID for Game
    private fun generateGameId(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..6).map { chars.random() }.joinToString("")
    }

    @Operation(summary = "Draw a tile from the deck")
    @PostMapping("/{gameId}/draw")
    fun drawTile(@PathVariable gameId: String): ResponseEntity<Tile> {
        val tile = gameManager.drawTileForPlayer(gameId)
        return if (tile != null) {
            ResponseEntity.ok(tile)
        } else {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(null)
        }
    }
}