package com.carcassonne.backend.model

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class GameStateTest {

    @Test
    fun `test adding players and getting current player`() {
        val gameState = GameState(gameId = "test-game")
        gameState.players.addAll(listOf("Player1", "Player2"))

        assertEquals("Player1", gameState.getCurrentPlayer())
    }

    @Test
    fun `test nextPlayer rotation`() {
        val gameState = GameState(gameId = "test-rotate")
        gameState.players.addAll(listOf("Player1", "Player2", "Player3"))

        assertEquals("Player1", gameState.getCurrentPlayer())
        gameState.nextPlayer()
        assertEquals("Player2", gameState.getCurrentPlayer())
        gameState.nextPlayer()
        assertEquals("Player3", gameState.getCurrentPlayer())
        gameState.nextPlayer()
        assertEquals("Player1", gameState.getCurrentPlayer()) // should wrap around
    }

    @Test
    fun `test placing tile on board`() {
        val gameState = GameState(gameId = "test-board")
        val tile = Tile(x = 1, y = 2, type = "road")

        gameState.board[Pair(tile.x, tile.y)] = tile

        val placedTile = gameState.board[Pair(1, 2)]
        assertNotNull(placedTile)
        assertEquals("road", placedTile?.type)
        assertEquals(1, placedTile?.x)
        assertEquals(2, placedTile?.y)
    }

    @Test
    fun `test nextPlayer with one player`() {
        val gameState = GameState(gameId = "solo-game")
        gameState.players.add("PlayerSolo")

        assertEquals("PlayerSolo", gameState.getCurrentPlayer())
        gameState.nextPlayer()
        assertEquals("PlayerSolo", gameState.getCurrentPlayer())
    }
}
