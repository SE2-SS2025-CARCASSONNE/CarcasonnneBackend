package com.carcassonne.backend.service

import com.carcassonne.backend.model.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
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
    fun `should generate tile deck with correct count and valid terrains`() {
        val deck = gameManager.createShuffledTileDeck(seed = 99L)
        val expectedCount = TerrainType.values().size * 5
        assertEquals(expectedCount, deck.size, "Expected $expectedCount tiles in deck")

        deck.forEach { tile ->
            assertTrue(tile.terrainNorth in TerrainType.values())
            assertTrue(tile.terrainEast in TerrainType.values())
            assertTrue(tile.terrainSouth in TerrainType.values())
            assertTrue(tile.terrainWest in TerrainType.values())
        }
    }

    @Test
    fun `tile deck should be consistent with same seed`() {
        val seed = 42L
        val deck1 = gameManager.createShuffledTileDeck(seed)
        val deck2 = gameManager.createShuffledTileDeck(seed)
        assertEquals(deck1, deck2, "Decks with same seed should be identical")
    }

    @Test
    fun `tile deck should differ with different seeds`() {
        val deck1 = gameManager.createShuffledTileDeck(seed = 1L)
        val deck2 = gameManager.createShuffledTileDeck(seed = 2L)
        assertNotEquals(deck1, deck2, "Decks with different seeds should be different")
    }

    @Test
    fun `tile IDs should be unique in the generated deck`() {
        val deck = gameManager.createShuffledTileDeck(seed = 123L)
        val ids = deck.map { it.id }
        val uniqueIds = ids.toSet()
        assertEquals(deck.size, uniqueIds.size, "All tile IDs should be unique")
    }


    @Test
    fun `drawTileForPlayer should return tile if deck has tiles`() {
        val game = gameManager.getOrCreateGame("game-draw")
        val tile = Tile(
            "${TerrainType.CITY}_${this}",
            TerrainType.CITY, TerrainType.CITY,
            TerrainType.CITY, TerrainType.ROAD,
            TileRotation.NORTH,
            Position(0,0)
        )
        game.tileDeck.add(tile)

        val drawn = gameManager.drawTileForPlayer("game-draw")
        assertNotNull(drawn)
    }

    @Test
    fun `drawTileForPlayer should return null if deck is empty`() {
        val game = gameManager.getOrCreateGame("empty-draw")
        game.tileDeck.clear()

        val result = gameManager.drawTileForPlayer("empty-draw")
        assertNull(result)
    }

    @Test
    fun `placeTile should place tile if first tile`() {
        val gameId = "game-1"
        val game = gameManager.getOrCreateGame(gameId)
        game.addPlayer("Player1")
        game.addPlayer("Player2")

        game.startGame()

        val tile = Tile(
            "${TerrainType.CITY}_${this}",
            TerrainType.CITY, TerrainType.CITY,
            TerrainType.CITY, TerrainType.ROAD,
            TileRotation.NORTH,
            Position(0,0)
        )
        val updated = gameManager.placeTile(
            gameId, tile, "Player1"
        )

        assertNotNull(updated)
        assertEquals(tile, updated?.board?.get(Position(0,0)))
    }

    @Test
    fun `placeTile should place tile if valid`() {
        val gameId = "game-1"
        val game = gameManager.getOrCreateGame(gameId)
        game.addPlayer("Player1")
        game.addPlayer("Player2")
        game.startGame()

        val tile1 = Tile(
            "${TerrainType.CITY}_${this}",
            TerrainType.ROAD, TerrainType.CITY,
            TerrainType.CITY, TerrainType.CITY,
            TileRotation.NORTH,
            Position(0,0)
        )
        val tile2 = Tile(
            "${TerrainType.CITY}_${this}",
            TerrainType.FIELD, TerrainType.FIELD,
            TerrainType.ROAD, TerrainType.FIELD,
            TileRotation.NORTH,
            Position(0,1),
            true
        )
        // simulate first round
        gameManager.placeTile(
            gameId, tile1, "Player1"
        )
        game.nextPlayer()
        game.status = GamePhase.TILE_PLACEMENT

        // second round
        val updated = gameManager.placeTile(
            gameId, tile2, "Player2"
        )

        assertNotNull(updated)
        assertEquals(tile1, updated?.board?.get(Position(0,0)))
        assertEquals(tile2, updated?.board?.get(Position(0,1)))
    }

    @Test
    fun `should reject tile placement if not current player`() {
        val gameId = "wrong-turn"
        val game = gameManager.getOrCreateGame(gameId)
        game.addPlayer("Player1")
        game.addPlayer("Player2")
        game.startGame()

        val tile = Tile(
            "${TerrainType.CITY}_${this}",
            TerrainType.CITY, TerrainType.ROAD,
            TerrainType.CITY, TerrainType.ROAD,
            TileRotation.NORTH,
            Position(1,0)
        )
        val result: () -> Unit = { gameManager.placeTile(gameId, tile, "Player2") } // Wrong turn

        assertFailsWith<IllegalStateException>("Not player's turn", block = result)
        assertFalse(game.board.containsKey(Position(1,0)), "Tile should not be placed")
    }

    @Test
    fun `should reject tile placement if not in tile placement phase`() {
        val gameId = "wrong-phase"
        val game = gameManager.getOrCreateGame(gameId)
        game.addPlayer("Player1")
        game.addPlayer("Player2")
        game.status = GamePhase.WAITING

        val tile = Tile(
            "${TerrainType.ROAD}_${this}",
            TerrainType.ROAD, TerrainType.ROAD,
            TerrainType.ROAD, TerrainType.ROAD,
            TileRotation.NORTH,
            Position(0,0)
        )
        val result: () -> Unit = { gameManager.placeTile(gameId, tile, "Player1") } // Wrong phase/game status

        assertFailsWith<IllegalStateException>("Game is not in tile placement phase", block = result)
        assertFalse(game.board.containsKey(Position(0,0)), "Tile should not be placed")
    }

    @Test
    fun `should reject tile placement if invalid position`() {
        val gameId = "invalid-position"
        val game = gameManager.getOrCreateGame(gameId)
        game.addPlayer("Player1")
        game.addPlayer("Player2")
        game.startGame()

        val tile = Tile(
            "${TerrainType.ROAD}_${this}",
            TerrainType.ROAD, TerrainType.ROAD,
            TerrainType.ROAD, TerrainType.ROAD,
            TileRotation.NORTH,
            Position(5,5)
        )
        val result: () -> Unit = { gameManager.placeTile(gameId, tile, "Player1") } // Wrong phase/game status

        assertFailsWith<IllegalArgumentException>("Position is invalid", block = result)
        assertFalse(game.board.containsKey(Position(5,5)), "Tile should not be placed")
    }

    @Test
    fun `should place meeple on valid city feature`() {
        val gameId = "meeple-test"
        val game = gameManager.getOrCreateGame(gameId)
        game.addPlayer("Player1")
        game.addPlayer("Player2")
        game.startGame()

        val tile = Tile(
            id = "tile1",
            terrainNorth = TerrainType.CITY, terrainEast = TerrainType.CITY,
            terrainSouth = TerrainType.CITY, terrainWest = TerrainType.ROAD,
            tileRotation = TileRotation.NORTH,
            position = Position(0, 0)
        )
        val tile2 = Tile(
            id = "tile2",
            terrainNorth = TerrainType.CITY, terrainEast = TerrainType.CITY,
            terrainSouth = TerrainType.CITY, terrainWest = TerrainType.ROAD,
            tileRotation = TileRotation.NORTH,
            position = Position(0, 1) // Direkt nördlich von tile1
        )
        gameManager.placeTile(gameId, tile, "Player1")
        game.nextPlayer()
        game.status = GamePhase.TILE_PLACEMENT

        gameManager.placeTile(gameId, tile2, "Player2")

        val meeple = Meeple(id = "meeple1", playerId = "Player2", tileId = tile2.id)

        val updatedGameState = gameManager.placeMeeple(gameId, "Player2", meeple, MeeplePosition.N)

        // Sicherstellen, dass der Meeple erfolgreich platziert wurde
        assertNotNull(updatedGameState, "Meeple should be placed successfully")

        // Optional: Überprüfen, ob der Meeple tatsächlich in der GameState-Liste ist
        assertTrue(updatedGameState!!.meeplesOnBoard.contains(meeple))
    }

    @Test
    fun `should NOT place meeple on invalid field feature`() {
        val gameId = "meeple-negative-test"
        val game = gameManager.getOrCreateGame(gameId)
        game.addPlayer("Player1")
        game.addPlayer("Player2")
        game.startGame()

        val tile = Tile(
            id = "tile-invalid",
            terrainNorth = TerrainType.FIELD, terrainEast = TerrainType.CITY,
            terrainSouth = TerrainType.FIELD, terrainWest = TerrainType.FIELD,
            tileRotation = TileRotation.NORTH,
            position = Position(0, 0)
        )
        gameManager.placeTile(gameId, tile, "Player1")

        val meeple = Meeple(id = "meeple-invalid", playerId = "Player1", tileId = tile.id)

        assertThrows<IllegalStateException>("Invalid meeple position") {
            gameManager.placeMeeple(gameId, "Player1", meeple, MeeplePosition.N)
        }
    }

    @Test
    fun `other player should NOT place a second meeple on the same feature`() {
        val gameId = "meeple-duplicate-test"
        val game = gameManager.getOrCreateGame(gameId)
        game.addPlayer("Player1")
        game.addPlayer("Player2")
        game.startGame()

        // Beispiel-Tile: Stadt im Osten, Feld in den anderen Bereichen
        val tile = Tile(
            id = "tile-conflict",
            terrainNorth = TerrainType.FIELD, terrainEast = TerrainType.CITY,
            terrainSouth = TerrainType.FIELD, terrainWest = TerrainType.ROAD,
            tileRotation = TileRotation.NORTH,
            position = Position(0, 0)
        )
        gameManager.placeTile(gameId, tile, "Player1")

        // Erster Meeple wird korrekt gesetzt
        val meeple1 = Meeple(id = "meeple1", playerId = "Player1", tileId = tile.id)
        val gameStateAfterFirstMeeple = gameManager.placeMeeple(gameId, "Player1", meeple1, MeeplePosition.E)

        assertNotNull(gameStateAfterFirstMeeple, "First meeple should be placed successfully")
        assertTrue(gameStateAfterFirstMeeple!!.meeplesOnBoard.contains(meeple1))

        // Zweiter Spieler versucht, einen Meeple auf dieselbe Position zu setzen
        val meeple2 = Meeple(id = "meeple2", playerId = "Player2", tileId = tile.id)

        assertThrows<IllegalStateException>("Meeple position already occupied") {
            gameManager.placeMeeple(gameId, "Player2", meeple2, MeeplePosition.E)
        }
    }

    @Test
    fun `should NOT allow same player to place two meeples on the same tile`() {
        val gameId = "meeple-double-test"
        val game = gameManager.getOrCreateGame(gameId)
        game.addPlayer("Player1")
        game.addPlayer("Player2")
        game.startGame()

        // Tile mit Stadt im Osten, Road im Westen
        val tile = Tile(
            id = "tile-double",
            terrainNorth = TerrainType.FIELD, terrainEast = TerrainType.CITY,
            terrainSouth = TerrainType.FIELD, terrainWest = TerrainType.ROAD,
            tileRotation = TileRotation.NORTH,
            position = Position(0, 0)
        )
        gameManager.placeTile(gameId, tile, "Player1")

        // Erster Meeple wird erfolgreich auf Osten (CITY) gesetzt
        val meeple1 = Meeple(id = "meeple1", playerId = "Player1", tileId = tile.id)
        val gameStateAfterFirstMeeple = gameManager.placeMeeple(gameId, "Player1", meeple1, MeeplePosition.E)

        assertNotNull(gameStateAfterFirstMeeple, "First meeple should be placed successfully")
        assertTrue(gameStateAfterFirstMeeple!!.meeplesOnBoard.contains(meeple1))

        // Derselbe Spieler versucht, einen zweiten Meeple auf Westen (ROAD) zu setzen
        val meeple2 = Meeple(id = "meeple2", playerId = "Player1", tileId = tile.id)

        assertThrows<IllegalStateException>("Player should NOT be allowed to place two meeples on the same tile") {
            gameManager.placeMeeple(gameId, "Player1", meeple2, MeeplePosition.W)
        }
    }

    @Test
    fun `should correctly place meeple on rotated tile`() {
        val gameId = "meeple-rotation-test"
        val game = gameManager.getOrCreateGame(gameId)
        game.addPlayer("Player1")
        game.addPlayer("Player2")
        game.startGame()

        // Tile mit Stadt ursprünglich im Süden, aber gedreht nach Norden
        val tileRotated = Tile(
            id = "tile-rotated",
            terrainNorth = TerrainType.FIELD, terrainEast = TerrainType.FIELD,
            terrainSouth = TerrainType.CITY, terrainWest = TerrainType.FIELD,
            tileRotation = TileRotation.SOUTH, // 🔄 Tile wird gedreht!
            position = Position(0, 0)
        )
        gameManager.placeTile(gameId, tileRotated, "Player1")

        // Statt Süden müssen wir jetzt Norden wählen, weil das ursprüngliche TerrainSouth nach Norden rotiert wurde
        val meeple = Meeple(id = "meeple-rotated", playerId = "Player1", tileId = tileRotated.id)

        val updatedGameState = gameManager.placeMeeple(gameId, "Player1", meeple, MeeplePosition.N)

        // Erwartung: Meeple sollte erfolgreich platziert werden
        assertNotNull(updatedGameState, "Meeple should be placed successfully on rotated tile")
        assertTrue(updatedGameState!!.meeplesOnBoard.contains(meeple))
    }

    @Test
    fun `should not allow meeple placement when player has no meeples left`() {
        val gameId = "meeple-no-meeple-test"
        val game = gameManager.getOrCreateGame(gameId)

        // Füge zwei Spieler hinzu, damit das Spiel korrekt läuft
        game.addPlayer("Player1")
        game.addPlayer("Player2")
        game.startGame()

        // Spieler1 hat keine Meeples mehr
        val player = game.players.first { it.id == "Player1" }
        val playerWithoutMeeples = player.copy(remainingMeeple = 0)

        // Aktualisiere die Spieler-Liste im Spiel
        game.players.find { it.id == playerWithoutMeeples.id }?.let { existingPlayer ->
            val index = game.players.indexOf(existingPlayer)
            game.players[index] = playerWithoutMeeples
        }

        val tile = Tile(
            id = "tile-test",
            terrainNorth = TerrainType.CITY, terrainEast = TerrainType.FIELD,
            terrainSouth = TerrainType.ROAD, terrainWest = TerrainType.FIELD,
            tileRotation = TileRotation.NORTH,
            position = Position(0, 0)
        )
        gameManager.placeTile(gameId, tile, "Player1")

        val meeple = Meeple(id = "meeple-test", playerId = "Player1", tileId = tile.id)

        // Versuch, einen Meeple zu platzieren → sollte fehlschlagen!
        val exception = assertThrows<IllegalStateException> {
            gameManager.placeMeeple(gameId, "Player1", meeple, MeeplePosition.N)
        }
        assertEquals("No Meeples remaining for placement!", exception.message)
    }

    @Test
    fun `should not allow meeple placement on ROAD if another meeple is on connected road segment`() {
        val gameId = "road-meeple-test"
        val game = gameManager.getOrCreateGame(gameId)

        // Zwei Spieler hinzufügen
        game.addPlayer("Player1")
        game.addPlayer("Player2")
        game.startGame()

        // Erstes Tile mit Straße, auf das bereits ein Meeple gesetzt wird
        val tileWithMeeple = Tile(
            id = "tile-road-1",
            terrainNorth = TerrainType.FIELD, terrainEast = TerrainType.ROAD,
            terrainSouth = TerrainType.FIELD, terrainWest = TerrainType.ROAD,
            tileRotation = TileRotation.NORTH,
            position = Position(0, 0)
        )
        gameManager.placeTile(gameId, tileWithMeeple, "Player1")

        val existingMeeple = Meeple(id = "meeple-existing", playerId = "Player1", tileId = tileWithMeeple.id, position = MeeplePosition.W)
        game.meeplesOnBoard.add(existingMeeple) // Simuliere bereits gesetzten Meeple

        // Zweites Tile mit einer angrenzenden Straße
        val adjacentTile = Tile(
            id = "tile-road-2",
            terrainNorth = TerrainType.FIELD, terrainEast = TerrainType.ROAD,
            terrainSouth = TerrainType.FIELD, terrainWest = TerrainType.ROAD,
            tileRotation = TileRotation.NORTH,
            position = Position(1, 0) // Direkt rechts daneben
        )
        game.nextPlayer()
        game.status = GamePhase.TILE_PLACEMENT
        gameManager.placeTile(gameId, adjacentTile, "Player2")

        // Spieler2 versucht jetzt, einen Meeple auf die Straße zu setzen → sollte blockiert werden!
        val newMeeple = Meeple(id = "meeple-new", playerId = "Player2", tileId = adjacentTile.id)

        val exception = assertThrows<IllegalStateException> {
            gameManager.placeMeeple(gameId, "Player2", newMeeple, MeeplePosition.W)
        }
        assertEquals("Another Meeple is already present on this feature!", exception.message)
    }
    @Test
    fun `endGame returns winner with highest score`() {
        val gameId = "endgame-test"
        val game = gameManager.getOrCreateGame(gameId)

        game.players.add(Player(id = "Player1", user_id = 1, score = 10, remainingMeeple = 5))
        game.players.add(Player(id = "Player2", user_id = 2, score = 15, remainingMeeple = 5))
        game.status = GamePhase.FINISHED
        val winner = gameManager.endGame(gameId)

        assertEquals("Player2", winner, "Expected Player2 to be the winner with highest score")
    }

    @Test
    fun `endGame throws exception if game is not finished`() {
        val gameId = "not-finished"
        val game = gameManager.getOrCreateGame(gameId)
        game.players.add(Player(id = "Player1", user_id = 1, score = 5, remainingMeeple = 5))
        game.status = GamePhase.TILE_PLACEMENT

        val exception = assertFailsWith<IllegalStateException> {
            gameManager.endGame(gameId)
        }
        assertEquals("Game is not in FINISHED phase", exception.message)
    }

    @Test
    fun `endGame throws exception if game has no players`() {
        val gameId = "no-players"
        val game = gameManager.getOrCreateGame(gameId)
        game.status = GamePhase.FINISHED

        val exception = assertFailsWith<IllegalStateException> {
            gameManager.endGame(gameId)
        }
        assertEquals("No players to determine winner", exception.message)
    }
}