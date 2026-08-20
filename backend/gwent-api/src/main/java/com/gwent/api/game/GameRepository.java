package com.gwent.api.game;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface GameRepository extends JpaRepository<Game, UUID> {

    @Query("SELECT g FROM Game g WHERE g.status = :status AND (g.player1Id = :userId OR g.player2Id = :userId)")
    Optional<Game> findActiveGameForPlayer(@Param("status") GameStatus status, @Param("userId") String userId);
}
