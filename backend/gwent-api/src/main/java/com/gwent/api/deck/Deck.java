package com.gwent.api.deck;

import com.gwent.engine.domain.Faction;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "decks")
@Getter
@Setter
@NoArgsConstructor
public class Deck {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String userId;

    private String name;

    @Enumerated(EnumType.STRING)
    private Faction faction;

    private String leaderId;

    @ElementCollection
    @CollectionTable(name = "deck_cards", joinColumns = @JoinColumn(name = "deck_id"))
    private List<DeckCardEntry> cards = new ArrayList<>();

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
