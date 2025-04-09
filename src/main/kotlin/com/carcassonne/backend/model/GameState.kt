package com.carcassonne.backend.model

data class GameState(
    val gameId: String,
    val players: MutableList<String> = mutableListOf(),
    val board: MutableMap<Pair<Int, Int>, Tile> = mutableMapOf(),
    var currentPlayerIndex: Int = 0,
    var status: String = "WAITING"
) {
    fun nextPlayer(): String {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size
        return players[currentPlayerIndex]
    }

    fun getCurrentPlayer(): String = players[currentPlayerIndex]
}