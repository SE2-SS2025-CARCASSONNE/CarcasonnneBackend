package com.carcassonne.backend.entity

import jakarta.persistence.*

@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0, // Unique user ID (primary key)
    val username: String = "", // Display name for UI and identification
    val password: String = "",  // Hashed password for user login
    val highScore: Int? = 0 // New attribute to track the user’s highest score
)
