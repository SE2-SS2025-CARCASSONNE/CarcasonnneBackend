package com.carcassonne.backend.model
data class GameMessage(
    val type: String,        // "join_game", "place_tile"
    val gameId: String,
    val player: String,  // TODO: Spieler ID rename
    val tile: Tile? = null
)
