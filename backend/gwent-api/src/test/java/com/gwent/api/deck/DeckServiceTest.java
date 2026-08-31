package com.gwent.api.deck;

import com.gwent.api.catalog.CardCatalogRepository;
import com.gwent.api.catalog.CardEntity;
import com.gwent.api.deck.dto.DeckCardEntryDto;
import com.gwent.api.deck.dto.DeckDto;
import com.gwent.api.deck.dto.SaveDeckRequest;
import com.gwent.api.deck.exception.DeckNotFoundException;
import com.gwent.engine.domain.CardType;
import com.gwent.engine.domain.Faction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.gwent.api.shared.TestDataFactory.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeckServiceTest {

    @Mock
    private DeckRepository deckRepository;

    @Mock
    private CardCatalogRepository cardCatalogRepository;

    @InjectMocks
    private DeckService deckService;

    // ── getUserDecks ──

    @Test
    void shouldReturnDeckDtoList_whenDecksExist() {
        Deck deck = makeDeck("user1", Faction.NORTHERN_REALMS);
        when(deckRepository.findByUserId("user1")).thenReturn(List.of(deck));

        List<DeckDto> result = deckService.getUserDecks("user1");

        assertEquals(1, result.size());
        assertEquals("Test Deck", result.getFirst().name());
    }

    @Test
    void shouldReturnEmptyList_whenNoDecks() {
        when(deckRepository.findByUserId("user1")).thenReturn(List.of());

        List<DeckDto> result = deckService.getUserDecks("user1");

        assertTrue(result.isEmpty());
    }

    // ── getDeck ──

    @Test
    void shouldReturnDeckDto_whenFound() {
        Deck deck = makeDeck("user1", Faction.NORTHERN_REALMS);
        UUID deckId = deck.getId();
        when(deckRepository.findByIdAndUserId(deckId, "user1")).thenReturn(Optional.of(deck));

        DeckDto result = deckService.getDeck(deckId, "user1");

        assertEquals(deckId, result.id());
    }

    @Test
    void shouldThrowDeckNotFoundException_whenNotFound() {
        UUID deckId = UUID.randomUUID();
        when(deckRepository.findByIdAndUserId(deckId, "user1")).thenReturn(Optional.empty());

        assertThrows(DeckNotFoundException.class, () -> deckService.getDeck(deckId, "user1"));
    }

    // ── createDeck ──

    @Test
    void shouldCreateAndReturnDeckDto_whenValid() {
        SaveDeckRequest request = makeValidSaveDeckRequest(Faction.NORTHERN_REALMS);
        stubValidDeckCards(request);
        when(deckRepository.save(any(Deck.class))).thenAnswer(inv -> {
            Deck d = inv.getArgument(0);
            d.setId(UUID.randomUUID());
            return d;
        });

        DeckDto result = deckService.createDeck("user1", request);

        assertNotNull(result.id());
        assertEquals("Test Deck", result.name());
        assertEquals(Faction.NORTHERN_REALMS, result.faction());
        verify(deckRepository).save(any(Deck.class));
    }

    @Test
    void shouldThrow_whenNameIsNull() {
        SaveDeckRequest request = new SaveDeckRequest(null, Faction.NORTHERN_REALMS, "leader_nr", List.of());

        assertThrows(IllegalArgumentException.class, () -> deckService.createDeck("user1", request));
    }

    @Test
    void shouldThrow_whenNameIsBlank() {
        SaveDeckRequest request = new SaveDeckRequest("  ", Faction.NORTHERN_REALMS, "leader_nr", List.of());

        assertThrows(IllegalArgumentException.class, () -> deckService.createDeck("user1", request));
    }

    @Test
    void shouldThrow_whenLeaderNotFound() {
        SaveDeckRequest request = new SaveDeckRequest("Deck", Faction.NORTHERN_REALMS, "missing_leader", List.of(new DeckCardEntryDto("c1", 3)));
        when(cardCatalogRepository.findById("missing_leader")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> deckService.createDeck("user1", request));
    }

    @Test
    void shouldThrow_whenLeaderCardIsNotLeaderType() {
        SaveDeckRequest request = new SaveDeckRequest("Deck", Faction.NORTHERN_REALMS, "not_leader", List.of(new DeckCardEntryDto("c1", 3)));
        CardEntity unitCard = makeUnitCardEntity("not_leader", Faction.NORTHERN_REALMS);
        when(cardCatalogRepository.findById("not_leader")).thenReturn(Optional.of(unitCard));

        assertThrows(IllegalArgumentException.class, () -> deckService.createDeck("user1", request));
    }

    @Test
    void shouldThrow_whenLeaderFactionDoesNotMatchDeckFaction() {
        SaveDeckRequest request = new SaveDeckRequest("Deck", Faction.NORTHERN_REALMS, "ng_leader",
                List.of(new DeckCardEntryDto("c1", 3)));
        CardEntity ngLeader = makeLeaderCardEntity("ng_leader", Faction.NILFGAARD, com.gwent.engine.domain.LeaderAbility.EMPEROR_OF_NILFGAARD);
        when(cardCatalogRepository.findById("ng_leader")).thenReturn(Optional.of(ngLeader));

        assertThrows(IllegalArgumentException.class, () -> deckService.createDeck("user1", request));
    }

    @Test
    void shouldThrow_whenCardListIsNull() {
        SaveDeckRequest request = new SaveDeckRequest("Deck", Faction.NORTHERN_REALMS, "leader_nr", null);
        stubLeader(Faction.NORTHERN_REALMS);

        assertThrows(IllegalArgumentException.class, () -> deckService.createDeck("user1", request));
    }

    @Test
    void shouldThrow_whenCardListIsEmpty() {
        SaveDeckRequest request = new SaveDeckRequest("Deck", Faction.NORTHERN_REALMS, "leader_nr", List.of());
        stubLeader(Faction.NORTHERN_REALMS);

        assertThrows(IllegalArgumentException.class, () -> deckService.createDeck("user1", request));
    }

    @Test
    void shouldThrow_whenCardNotFoundInCatalog() {
        List<DeckCardEntryDto> cards = buildMinimalCards();
        SaveDeckRequest request = new SaveDeckRequest("Deck", Faction.NORTHERN_REALMS, "leader_nr", cards);
        stubLeader(Faction.NORTHERN_REALMS);
        when(cardCatalogRepository.findById("card_1")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> deckService.createDeck("user1", request));
    }

    @Test
    void shouldThrow_whenLeaderCardInCardList() {
        List<DeckCardEntryDto> cards = List.of(new DeckCardEntryDto("card_leader_in_list", 1));
        SaveDeckRequest request = new SaveDeckRequest("Deck", Faction.NORTHERN_REALMS, "leader_nr", cards);
        stubLeader(Faction.NORTHERN_REALMS);
        CardEntity leaderInList = makeLeaderCardEntity("card_leader_in_list", Faction.NORTHERN_REALMS,
                com.gwent.engine.domain.LeaderAbility.SIEGE_MASTER);
        when(cardCatalogRepository.findById("card_leader_in_list")).thenReturn(Optional.of(leaderInList));

        assertThrows(IllegalArgumentException.class, () -> deckService.createDeck("user1", request));
    }

    @Test
    void shouldThrow_whenCardFactionDoesNotMatch() {
        List<DeckCardEntryDto> cards = List.of(new DeckCardEntryDto("ng_unit", 3));
        SaveDeckRequest request = new SaveDeckRequest("Deck", Faction.NORTHERN_REALMS, "leader_nr", cards);
        stubLeader(Faction.NORTHERN_REALMS);
        CardEntity ngUnit = makeUnitCardEntity("ng_unit", Faction.NILFGAARD);
        when(cardCatalogRepository.findById("ng_unit")).thenReturn(Optional.of(ngUnit));

        assertThrows(IllegalArgumentException.class, () -> deckService.createDeck("user1", request));
    }

    @Test
    void shouldAllow_whenNeutralCardInAnyFactionDeck() {
        List<DeckCardEntryDto> cards = buildMinimalCardsWithNeutral();
        SaveDeckRequest request = new SaveDeckRequest("Deck", Faction.NORTHERN_REALMS, "leader_nr", cards);
        stubLeader(Faction.NORTHERN_REALMS);
        stubCardsForMinimalWithNeutral();
        when(deckRepository.save(any(Deck.class))).thenAnswer(inv -> {
            Deck d = inv.getArgument(0);
            d.setId(UUID.randomUUID());
            return d;
        });

        DeckDto result = deckService.createDeck("user1", request);

        assertNotNull(result);
    }

    @Test
    void shouldThrow_whenHeroQuantityExceedsOne() {
        List<DeckCardEntryDto> cards = List.of(new DeckCardEntryDto("hero_1", 2));
        SaveDeckRequest request = new SaveDeckRequest("Deck", Faction.NORTHERN_REALMS, "leader_nr", cards);
        stubLeader(Faction.NORTHERN_REALMS);
        CardEntity hero = makeHeroCardEntity("hero_1", Faction.NORTHERN_REALMS);
        when(cardCatalogRepository.findById("hero_1")).thenReturn(Optional.of(hero));

        assertThrows(IllegalArgumentException.class, () -> deckService.createDeck("user1", request));
    }

    @Test
    void shouldThrow_whenTotalCountBelow22() {
        List<DeckCardEntryDto> cards = List.of(new DeckCardEntryDto("card_1", 3));
        SaveDeckRequest request = new SaveDeckRequest("Deck", Faction.NORTHERN_REALMS, "leader_nr", cards);
        stubLeader(Faction.NORTHERN_REALMS);
        stubUnitCard("card_1", Faction.NORTHERN_REALMS);

        assertThrows(IllegalArgumentException.class, () -> deckService.createDeck("user1", request));
    }

    @Test
    void shouldThrow_whenTotalCountAbove40() {
        List<DeckCardEntryDto> cards = new ArrayList<>();
        for (int i = 1; i <= 14; i++) {
            cards.add(new DeckCardEntryDto("card_" + i, 3));
        }
        SaveDeckRequest request = new SaveDeckRequest("Deck", Faction.NORTHERN_REALMS, "leader_nr", cards);
        stubLeader(Faction.NORTHERN_REALMS);
        for (int i = 1; i <= 14; i++) {
            stubUnitCard("card_" + i, Faction.NORTHERN_REALMS);
        }

        assertThrows(IllegalArgumentException.class, () -> deckService.createDeck("user1", request));
    }

    @Test
    void shouldAccept_whenTotalCountIs22() {
        SaveDeckRequest request = makeValidSaveDeckRequest(Faction.NORTHERN_REALMS);
        // 8 cards * 3 = 24, adjust to 22
        List<DeckCardEntryDto> cards = new ArrayList<>();
        for (int i = 1; i <= 7; i++) {
            cards.add(new DeckCardEntryDto("card_" + i, 3));
        }
        cards.add(new DeckCardEntryDto("card_8", 1));
        SaveDeckRequest req22 = new SaveDeckRequest("Deck", Faction.NORTHERN_REALMS, "leader_nr", cards);
        stubLeader(Faction.NORTHERN_REALMS);
        for (int i = 1; i <= 8; i++) {
            stubUnitCard("card_" + i, Faction.NORTHERN_REALMS);
        }
        when(deckRepository.save(any(Deck.class))).thenAnswer(inv -> {
            Deck d = inv.getArgument(0);
            d.setId(UUID.randomUUID());
            return d;
        });

        DeckDto result = deckService.createDeck("user1", req22);

        assertNotNull(result);
    }

    // ── updateDeck ──

    @Test
    void shouldUpdateAndReturnDeckDto_whenValid() {
        Deck deck = makeDeck("user1", Faction.NORTHERN_REALMS);
        UUID deckId = deck.getId();
        SaveDeckRequest request = makeValidSaveDeckRequest(Faction.NORTHERN_REALMS);
        when(deckRepository.findByIdAndUserId(deckId, "user1")).thenReturn(Optional.of(deck));
        stubValidDeckCards(request);
        when(deckRepository.save(any(Deck.class))).thenAnswer(inv -> inv.getArgument(0));

        DeckDto result = deckService.updateDeck(deckId, "user1", request);

        assertEquals(deckId, result.id());
        verify(deckRepository).save(any(Deck.class));
    }

    @Test
    void shouldThrowDeckNotFoundException_whenUpdateNotFound() {
        UUID deckId = UUID.randomUUID();
        SaveDeckRequest request = makeValidSaveDeckRequest(Faction.NORTHERN_REALMS);
        when(deckRepository.findByIdAndUserId(deckId, "user1")).thenReturn(Optional.empty());

        assertThrows(DeckNotFoundException.class, () -> deckService.updateDeck(deckId, "user1", request));
    }

    // ── deleteDeck ──

    @Test
    void shouldDeleteDeck_whenFound() {
        Deck deck = makeDeck("user1", Faction.NORTHERN_REALMS);
        UUID deckId = deck.getId();
        when(deckRepository.findByIdAndUserId(deckId, "user1")).thenReturn(Optional.of(deck));

        deckService.deleteDeck(deckId, "user1");

        verify(deckRepository).delete(deck);
    }

    @Test
    void shouldThrowDeckNotFoundException_whenDeleteNotFound() {
        UUID deckId = UUID.randomUUID();
        when(deckRepository.findByIdAndUserId(deckId, "user1")).thenReturn(Optional.empty());

        assertThrows(DeckNotFoundException.class, () -> deckService.deleteDeck(deckId, "user1"));
    }

    // ── Helpers ──

    private void stubLeader(Faction faction) {
        CardEntity leader = makeLeaderCardEntity("leader_nr", faction, com.gwent.engine.domain.LeaderAbility.SIEGE_MASTER);
        when(cardCatalogRepository.findById("leader_nr")).thenReturn(Optional.of(leader));
    }

    private void stubUnitCard(String id, Faction faction) {
        CardEntity unit = makeUnitCardEntity(id, faction);
        when(cardCatalogRepository.findById(id)).thenReturn(Optional.of(unit));
    }

    private void stubValidDeckCards(SaveDeckRequest request) {
        stubLeader(request.faction());
        for (DeckCardEntryDto entry : request.cards()) {
            stubUnitCard(entry.cardId(), request.faction());
        }
    }

    private List<DeckCardEntryDto> buildMinimalCards() {
        List<DeckCardEntryDto> cards = new ArrayList<>();
        for (int i = 1; i <= 8; i++) {
            cards.add(new DeckCardEntryDto("card_" + i, 3));
        }
        return cards;
    }

    private List<DeckCardEntryDto> buildMinimalCardsWithNeutral() {
        List<DeckCardEntryDto> cards = new ArrayList<>();
        for (int i = 1; i <= 7; i++) {
            cards.add(new DeckCardEntryDto("card_" + i, 3));
        }
        cards.add(new DeckCardEntryDto("neutral_card", 3));
        return cards;
    }

    private void stubCardsForMinimalWithNeutral() {
        for (int i = 1; i <= 7; i++) {
            stubUnitCard("card_" + i, Faction.NORTHERN_REALMS);
        }
        CardEntity neutralCard = makeUnitCardEntity("neutral_card", Faction.NEUTRAL);
        when(cardCatalogRepository.findById("neutral_card")).thenReturn(Optional.of(neutralCard));
    }
}
