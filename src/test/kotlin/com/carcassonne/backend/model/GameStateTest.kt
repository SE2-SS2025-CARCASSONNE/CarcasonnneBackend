package com.carcassonne.backend.model

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GameStateTest {
    private lateinit var gameState: GameState

    @BeforeEach
    fun setup() {
        gameState = GameState(gameId = "test-game")
    }

    @Test
    fun `test adding players`() {
        gameState.addPlayer("player1")
        gameState.addPlayer("player2")
        assertEquals(2, gameState.players.size)
        assertEquals("player1", gameState.players[0].id)
    }

    @Test
    fun `test starting the game`() {
        gameState.addPlayer("player1")
        gameState.addPlayer("player2")
        gameState.startGame()
        assertEquals(GamePhase.TILE_PLACEMENT, gameState.status)
    }

    @Test
    fun `test switching players`() {
        gameState.addPlayer("player1")
        gameState.addPlayer("player2")
        gameState.startGame()
        val current = gameState.getCurrentPlayer()
        val next = gameState.nextPlayer()
        assertNotEquals(current, next)
    }

    @Test
    fun `test placing a tile`() {
        gameState.addPlayer("player1")
        gameState.addPlayer("player2")
        gameState.startGame()
        val tile = Tile(
            id = "tile1",
            terrainNorth = TerrainType.ROAD,
            terrainEast = TerrainType.FIELD,
            terrainSouth = TerrainType.CITY,
            terrainWest = TerrainType.ROAD,
            tileRotation = TileRotation.NORTH
        )
        val position = Position(0, 0)
        gameState.placeTile(tile, position)
        assertEquals(tile, gameState.board[position])
        assertEquals(GamePhase.MEEPLE_PLACEMENT, gameState.status)
    }

    @Test
    fun `test drawing a tile`() {
        val tile = Tile(
            id = "tile1",
            terrainNorth = TerrainType.ROAD,
            terrainEast = TerrainType.FIELD,
            terrainSouth = TerrainType.CITY,
            terrainWest = TerrainType.ROAD,
            tileRotation = TileRotation.NORTH
        )
        gameState.tileDeck.add(tile)
        val drawn = gameState.drawTile()
        assertEquals(tile, drawn)
        assertTrue(gameState.tileDeck.isEmpty())
    }

    @Test
    fun `test finding a player`() {
        gameState.addPlayer("player1")
        val player = gameState.findPlayerById("player1")
        assertNotNull(player)
        assertEquals("player1", player?.id)
    }

    @Test
    fun `test finishing game`() {
        gameState.finishGame()
        assertEquals(GamePhase.FINISHED, gameState.status)
    }
}
