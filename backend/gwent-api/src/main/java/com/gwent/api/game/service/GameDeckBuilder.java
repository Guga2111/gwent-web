package com.gwent.api.game.service;

import com.gwent.api.catalog.CardCatalogRepository;
import com.gwent.api.catalog.CardEntity;
import com.gwent.api.deck.Deck;
import com.gwent.api.deck.DeckCardEntry;
import com.gwent.api.deck.DeckRepository;
import com.gwent.api.deck.exception.DeckNotFoundException;
import com.gwent.engine.domain.Card;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class GameDeckBuilder {

    private final DeckRepository deckRepository;
    private final CardCatalogRepository cardCatalogRepository;

    public GameDeckBuilder (DeckRepository deckRepository, CardCatalogRepository cardCatalogRepository) {
        this.deckRepository = deckRepository;
        this.cardCatalogRepository = cardCatalogRepository;
    }

    public Card buildLeaderFromDeckId(UUID deckId) {
        Deck deck = deckRepository.findById(deckId)
                .orElseThrow(() -> new DeckNotFoundException(deckId));
        CardEntity leader = cardCatalogRepository.findById(deck.getLeaderId())
                .orElseThrow(() -> new IllegalStateException("Leader card not found: " + deck.getLeaderId()));
        return toEngineCard(leader, leader.getId());
    }

    public List<Card> buildDeckFromDeckId(UUID deckId) {
        Deck deck = deckRepository.findById(deckId)
                .orElseThrow(() -> new DeckNotFoundException(deckId));

        List<String> cardIds = deck.getCards().stream()
                .map(DeckCardEntry::getCardId)
                .toList();
        Map<String, CardEntity> cardMap = cardCatalogRepository.findAllById(cardIds).stream()
                .collect(Collectors.toMap(CardEntity::getId, Function.identity()));

        List<Card> cards = new ArrayList<>();
        for (var entry : deck.getCards()) {
            CardEntity template = cardMap.get(entry.getCardId());
            if (template == null) {
                throw new IllegalStateException("Card not found: " + entry.getCardId());
            }
            for (int i = 1; i <= entry.getQuantity(); i++) {
                String instanceId = entry.getQuantity() > 1 ? entry.getCardId() + "_" + i : entry.getCardId();
                cards.add(toEngineCard(template, instanceId));
            }
        }
        Collections.shuffle(cards);
        return cards;
    }

    public Card toEngineCard(CardEntity e, String id) {
        return new Card(id, e.getName(), e.getFaction(), e.getCardType(),
                e.getAbility(), e.getLeaderAbility(), e.getRowType(), e.getBasePower());
    }
}
