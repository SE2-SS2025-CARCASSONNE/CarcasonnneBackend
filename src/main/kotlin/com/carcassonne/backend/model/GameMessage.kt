package com.carcassonne.backend.model
data class GameMessage(
    val type: String,        // "join_game", "place_tile"
    val gameId: String,
    val player: String,
    val tile: Tile? = null
)
