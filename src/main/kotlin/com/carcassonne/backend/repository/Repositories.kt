package com.carcassonne.backend.repository

import com.carcassonne.backend.entity.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.transaction.annotation.Transactional
import org.springframework.stereotype.Repository

// Repository for managing User entities
@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun findUserByUsername(username: String): User?
}

// Repository for managing Game entities (game session info)
@Repository
interface GameRepository : JpaRepository<Game, Long> {

    @Modifying
    @Transactional
    @Query("UPDATE Game g SET g.status = :status WHERE g.gameCode = :gameCode")
    fun updateStatusByGameCode(@Param("gameCode") gameCode: String, @Param("status") status: String)
}

// Repository for tracking which users are in which games
@Repository
interface GamePlayerRepository : JpaRepository<GamePlayer, Long>

// Repository for all placed tiles on the game board
@Repository
interface TileRepository : JpaRepository<Tile, Long>

// Repository for meeples placed on features
@Repository
interface MeepleRepository : JpaRepository<Meeple, Long>

// Repository for tracking each player action (history/log)
@Repository
interface MoveRepository : JpaRepository<Move, Long>

// Repository for accessing and managing player scores during a game
@Repository
interface ScoreRepository : JpaRepository<Score, Long>

// Repository for storing and retrieving in-game chat messages between players
@Repository
interface ChatMessageRepository : JpaRepository<ChatMessage, Long>