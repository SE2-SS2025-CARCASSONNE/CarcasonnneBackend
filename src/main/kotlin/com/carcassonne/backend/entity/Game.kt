package com.carcassonne.backend.entity

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "games")
data class Game(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(unique = true)
    val gameCode: String, // Random 6-char ID, e.g. "XY9D2A"

    val status: String = "WAITING",

    val createdAt: Instant = Instant.now()
)
