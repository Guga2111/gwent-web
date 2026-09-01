package com.gwent.api.deck;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeckRepository extends JpaRepository<Deck, UUID> {

    List<Deck> findByUserId(String userId);

    Optional<Deck> findByIdAndUserId(UUID id, String userId);
}
