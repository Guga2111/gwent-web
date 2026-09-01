package com.gwent.api.game.service;

import com.gwent.api.catalog.CardCatalogCache;
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

@Service
public class GameDeckBuilder {

    private final DeckRepository deckRepository;
    private final CardCatalogCache cardCatalogCache;

    public GameDeckBuilder (DeckRepository deckRepository, CardCatalogCache cardCatalogCache) {
        this.deckRepository = deckRepository;
        this.cardCatalogCache = cardCatalogCache;
    }

    public Card buildLeaderFromDeckId(UUID deckId) {
        Deck deck = deckRepository.findById(deckId)
                .orElseThrow(() -> new DeckNotFoundException(deckId));
        CardEntity leader = cardCatalogCache.getById(deck.getLeaderId());
        return toEngineCard(leader, leader.getId());
    }

    public List<Card> buildDeckFromDeckId(UUID deckId) {
        Deck deck = deckRepository.findById(deckId)
                .orElseThrow(() -> new DeckNotFoundException(deckId));

        List<String> cardIds = deck.getCards().stream()
                .map(DeckCardEntry::getCardId)
                .toList();
        Map<String, CardEntity> cardMap = cardCatalogCache.getAllById(cardIds);

        List<Card> cards = new ArrayList<>();
        for (var entry : deck.getCards()) {
            CardEntity template = cardMap.get(entry.getCardId());
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
