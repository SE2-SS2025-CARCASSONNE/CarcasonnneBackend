package com.carcassonne.backend.entity

import jakarta.persistence.*

@Entity
@Table(name = "tiles")
data class Tile(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0, // Unique DB ID for placed tile

    val tileId: String = "", // Refers to tile template (e.g., "T05")
    val x: Int = 0,          // X-position on board grid
    val y: Int = 0,          // Y-position on board grid
    val rotation: Int = 0,   // Orientation in degrees (0/90/180/270)

    val type: String = ""    // Main feature type for filtering ("city", "monastery", etc.)
)
