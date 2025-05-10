package com.carcassonne.backend.repository

import com.carcassonne.backend.entity.Game
import com.carcassonne.backend.entity.User
import com.carcassonne.backend.model.GamePhase
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

// User repository (still needed)
@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun findUserByUsername(username: String): User?
}

// Game repository (still needed + FIXED update method)
@Repository
interface GameRepository : JpaRepository<Game, Long> {

    @Modifying
    @Transactional
    @Query("UPDATE Game g SET g.status = :status WHERE g.gameCode = :gameCode")
    fun updateStatusByGameCode(
        @Param("gameCode") gameCode: String,
        @Param("status") status: GamePhase
    )
    fun findByGameCode(gameCode: String): Game?
}
