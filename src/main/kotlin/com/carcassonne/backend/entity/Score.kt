package com.carcassonne.backend.entity

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "scores")
data class Score(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne
    @JoinColumn(name = "game_player_id")
    val gamePlayer: GamePlayer,  // Player associated with this score

    val score: Int = 0,  // Player's score at this point

    val timestamp: Instant = Instant.now()  // Time when the score was updated
)