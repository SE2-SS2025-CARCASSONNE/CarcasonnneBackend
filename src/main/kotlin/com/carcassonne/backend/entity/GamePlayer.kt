package com.carcassonne.backend.entity

import jakarta.persistence.*

@Entity
@Table(name = "game_players")
data class GamePlayer(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0, // Unique participation ID

    val score: Int = 0, // Player’s current score in this game
    val remainingMeeples: Int = 7 // Track available meeples during the game
)
