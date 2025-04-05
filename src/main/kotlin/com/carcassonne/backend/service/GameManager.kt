package com.carcassonne.backend.service

import com.carcassonne.backend.model.GameState
import com.carcassonne.backend.model.Tile
import org.springframework.stereotype.Component

@Component
class GameManager {
    private val games = mutableMapOf<String, GameState>()

    fun getOrCreateGame(gameId: String): GameState =
        games.getOrPut(gameId) { GameState(gameId) }

    fun placeTile(gameId: String, tile: Tile, player: String): GameState? {
        val game = games[gameId] ?: return null
        if (game.getCurrentPlayer() != player) return null

        game.board[Pair(tile.x, tile.y)] = tile
        game.nextPlayer()
        return game
    }
}
