package com.carcassonne.backend.model
data class Tile(
    val x: Int,
    val y: Int,
    val type: String // e.g. "city", "road", "monastery"
)
