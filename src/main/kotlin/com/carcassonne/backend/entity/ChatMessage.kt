package com.carcassonne.backend.entity

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "chat_messages")
data class ChatMessage(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0, // Unique message ID

    @ManyToOne
    @JoinColumn(name = "game_id")
    val game: Game, // Game the message belongs to

    @ManyToOne
    @JoinColumn(name = "sender_id")
    val sender: GamePlayer, // Player who sent the message

    val message: String, // Text content of the message

    val timestamp: Instant = Instant.now() // When the message was sent
)