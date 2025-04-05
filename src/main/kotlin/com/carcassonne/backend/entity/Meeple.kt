package com.carcassonne.backend.entity

import jakarta.persistence.*

@Entity
@Table(name = "meeples")
data class Meeple(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0, // Unique ID for each placed meeple

    val segmentType: String = "",    // E.g., "city", "road", "monastery"
    val segmentPosition: String = "" // E.g., "NORTH", "SOUTH", etc. for position on tile
)
