package com.carcassonne.backend.model

data class Player(
    val id: String,
    var score: Int,
    val remainingMeeple: Int,
    val user_id: Int?
)

