package com.gwent.api.deck;

import com.gwent.api.catalog.CardCatalogRepository;
import com.gwent.api.catalog.CardEntity;
import com.gwent.api.deck.dto.DeckCardEntryDto;
import com.gwent.api.deck.dto.DeckDto;
import com.gwent.api.deck.dto.SaveDeckRequest;
import com.gwent.api.deck.exception.DeckNotFoundException;
import com.gwent.engine.domain.CardType;
import com.gwent.engine.domain.Faction;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class DeckService {

    private final DeckRepository deckRepository;
    private final CardCatalogRepository cardCatalogRepository;

    public DeckService(DeckRepository deckRepository, CardCatalogRepository cardCatalogRepository) {
        this.deckRepository = deckRepository;
        this.cardCatalogRepository = cardCatalogRepository;
    }

    public List<DeckDto> getUserDecks(String userId) {
        return deckRepository.findByUserId(userId).stream()
                .map(this::toDto)
                .toList();
    }

    public DeckDto getDeck(UUID deckId, String userId) {
        return deckRepository.findByIdAndUserId(deckId, userId)
                .map(this::toDto)
                .orElseThrow(() -> new DeckNotFoundException(deckId));
    }

    public DeckDto createDeck(String userId, SaveDeckRequest request) {
        validateDeck(request);

        Deck deck = new Deck();
        deck.setUserId(userId);
        deck.setName(request.name());
        deck.setFaction(request.faction());
        deck.setLeaderId(request.leaderId());
        deck.setCards(request.cards().stream()
                .map(e -> new DeckCardEntry(e.cardId(), e.quantity()))
                .toList());

        return toDto(deckRepository.save(deck));
    }

    public DeckDto updateDeck(UUID deckId, String userId, SaveDeckRequest request) {
        Deck deck = deckRepository.findByIdAndUserId(deckId, userId)
                .orElseThrow(() -> new DeckNotFoundException(deckId));

        validateDeck(request);

        deck.setName(request.name());
        deck.setFaction(request.faction());
        deck.setLeaderId(request.leaderId());
        deck.setCards(request.cards().stream()
                .map(e -> new DeckCardEntry(e.cardId(), e.quantity()))
                .toList());

        return toDto(deckRepository.save(deck));
    }

    public void deleteDeck(UUID deckId, String userId) {
        Deck deck = deckRepository.findByIdAndUserId(deckId, userId)
                .orElseThrow(() -> new DeckNotFoundException(deckId));
        deckRepository.delete(deck);
    }

    private void validateDeck(SaveDeckRequest request) {
        if (request.name() == null || request.name().isBlank()) {
            throw new IllegalArgumentException("Deck name is required");
        }

        // Validate card list entries
        if (request.cards() == null || request.cards().isEmpty()) {
            throw new IllegalArgumentException("Deck must contain cards");
        }

        // Batch-fetch all cards (leader + deck cards) in one query
        List<String> allIds = new ArrayList<>();
        allIds.add(request.leaderId());
        request.cards().forEach(e -> allIds.add(e.cardId()));
        Map<String, CardEntity> cardMap = cardCatalogRepository.findAllById(allIds).stream()
                .collect(Collectors.toMap(CardEntity::getId, Function.identity()));

        // Validate leader
        CardEntity leader = cardMap.get(request.leaderId());
        if (leader == null) {
            throw new IllegalArgumentException("Leader card not found: " + request.leaderId());
        }
        if (leader.getCardType() != CardType.LEADER) {
            throw new IllegalArgumentException("Card is not a leader: " + request.leaderId());
        }
        if (leader.getFaction() != request.faction()) {
            throw new IllegalArgumentException("Leader faction does not match deck faction");
        }

        int totalCount = 0;
        for (DeckCardEntryDto entry : request.cards()) {
            CardEntity card = cardMap.get(entry.cardId());
            if (card == null) {
                throw new IllegalArgumentException("Card not found: " + entry.cardId());
            }

            if (card.getCardType() == CardType.LEADER) {
                throw new IllegalArgumentException("Leader cards are not allowed in the card list");
            }

            Faction cardFaction = card.getFaction();
            if (cardFaction != request.faction() && cardFaction != Faction.NEUTRAL) {
                throw new IllegalArgumentException("Card does not belong to deck faction: " + entry.cardId());
            }

            boolean isHero = card.getCardType() == CardType.HERO;
            int maxCopies = isHero ? 1 : 3;
            if (entry.quantity() < 1 || entry.quantity() > maxCopies) {
                throw new IllegalArgumentException(
                        "Invalid quantity " + entry.quantity() + " for card " + entry.cardId() +
                        " (max " + maxCopies + ")");
            }

            totalCount += entry.quantity();
        }

        if (totalCount < 22 || totalCount > 40) {
            throw new IllegalArgumentException(
                    "Deck must contain between 22 and 40 cards, got " + totalCount);
        }
    }

    private DeckDto toDto(Deck deck) {
        List<DeckCardEntryDto> cards = deck.getCards().stream()
                .map(e -> new DeckCardEntryDto(e.getCardId(), e.getQuantity()))
                .toList();
        return new DeckDto(deck.getId(), deck.getName(), deck.getFaction(),
                deck.getLeaderId(), cards, deck.getCreatedAt());
    }
}
