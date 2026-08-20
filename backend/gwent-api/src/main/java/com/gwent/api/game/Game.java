package com.gwent.api.game;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "games")
@Getter
@Setter
@NoArgsConstructor
public class Game {

    @Id
    private UUID id;

    private String player1Id;

    private String player2Id;

    private UUID player1DeckId;

    private UUID player2DeckId;

    @Column(columnDefinition = "TEXT")
    private String stateJson;

    @Enumerated(EnumType.STRING)
    private GameStatus status;

    @Setter(lombok.AccessLevel.NONE)
    private LocalDateTime createdAt;

    @Setter(lombok.AccessLevel.NONE)
    private LocalDateTime updatedAt;

    @PrePersist
    private void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    private void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
