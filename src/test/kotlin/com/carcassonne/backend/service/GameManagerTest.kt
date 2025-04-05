package com.carcassonne.backend.service


import com.carcassonne.backend.model.GameState
import com.carcassonne.backend.model.Tile
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.*

class GameManagerTest {

    private lateinit var gameManager: GameManager

    @BeforeEach
    fun setup() {
        gameManager = GameManager()
    }

    @Test
    fun `should create and return new game if not exists`() {
        val game = gameManager.getOrCreateGame("test-game")
        assertEquals("test-game", game.gameId)

        val sameGame = gameManager.getOrCreateGame("test-game")
        assertSame(game, sameGame, "Should return the same instance for same gameId")
    }

    @Test
    fun `should place tile and rotate turn if correct player`() {
        val gameId = "game-1"
        val game = gameManager.getOrCreateGame(gameId)
        game.players.addAll(listOf("Player1", "Player2"))

        val tile = Tile(x = 0, y = 0, type = "road")
        val updated = gameManager.placeTile(gameId, tile, "Player1")

        assertNotNull(updated)
        assertEquals(tile, updated?.board?.get(Pair(0, 0)))
        assertEquals("Player2", updated?.getCurrentPlayer())
    }

    @Test
    fun `should reject tile placement if not current player`() {
        val gameId = "game-2"
        val game = gameManager.getOrCreateGame(gameId)
        game.players.addAll(listOf("Player1", "Player2"))

        val tile = Tile(x = 1, y = 1, type = "city")
        val result = gameManager.placeTile(gameId, tile, "Player2") // Wrong turn

        assertNull(result, "Move should be invalid because it's not Player2's turn")
        assertFalse(game.board.containsKey(Pair(1, 1)), "Tile should not be placed")
    }
}
