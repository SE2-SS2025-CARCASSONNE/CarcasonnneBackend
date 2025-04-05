package com.carcassonne.backend.entity

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "moves")
data class Move(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0, // Unique ID for move log

    val turnNumber: Int = 0,      // To track round order
    val action: String = "",      // E.g., "place_tile", "place_meeple", "skip"
    val timestamp: Instant = Instant.now() // For analysis / history
)
