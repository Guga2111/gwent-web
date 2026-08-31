package com.gwent.api.shared;

import com.gwent.api.catalog.CardEntity;
import com.gwent.api.deck.Deck;
import com.gwent.api.deck.DeckCardEntry;
import com.gwent.api.deck.dto.DeckCardEntryDto;
import com.gwent.api.deck.dto.SaveDeckRequest;
import com.gwent.api.game.SessionContext;
import com.gwent.api.game.dto.*;
import com.gwent.api.user.User;
import com.gwent.engine.domain.*;
import com.gwent.engine.state.BoardRow;
import com.gwent.engine.state.GameState;
import com.gwent.engine.state.PlayerState;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class TestDataFactory {

    private TestDataFactory() {}

    // --- User ---

    public static User makeUser(String email, String username) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail(email);
        user.setUsername(username);
        user.setPassword("encoded-password");
        user.setHasSeenTutorial(false);
        return user;
    }

    // --- CardEntity ---

    public static CardEntity makeLeaderCardEntity(String id, Faction faction, LeaderAbility leaderAbility) {
        CardEntity card = new CardEntity();
        card.setId(id);
        card.setName("Leader " + id);
        card.setFaction(faction);
        card.setCardType(CardType.LEADER);
        card.setLeaderAbility(leaderAbility);
        card.setDeckCopies(1);
        return card;
    }

    public static CardEntity makeUnitCardEntity(String id, Faction faction) {
        CardEntity card = new CardEntity();
        card.setId(id);
        card.setName("Unit " + id);
        card.setFaction(faction);
        card.setCardType(CardType.UNIT);
        card.setRowType(RowType.MELEE);
        card.setBasePower(5);
        card.setDeckCopies(3);
        return card;
    }

    public static CardEntity makeHeroCardEntity(String id, Faction faction) {
        CardEntity card = new CardEntity();
        card.setId(id);
        card.setName("Hero " + id);
        card.setFaction(faction);
        card.setCardType(CardType.HERO);
        card.setRowType(RowType.MELEE);
        card.setBasePower(10);
        card.setDeckCopies(1);
        return card;
    }

    // --- Engine Card ---

    public static Card makeCard(String id, Faction faction) {
        return new Card(id, "Unit " + id, faction, CardType.UNIT, null, null, RowType.MELEE, 5);
    }

    public static Card makeLeaderCard(String id, Faction faction, LeaderAbility leaderAbility) {
        return new Card(id, "Leader " + id, faction, CardType.LEADER, null, leaderAbility, null, null);
    }

    // --- Deck ---

    public static Deck makeDeck(String userId, Faction faction) {
        Deck deck = new Deck();
        deck.setId(UUID.randomUUID());
        deck.setUserId(userId);
        deck.setName("Test Deck");
        deck.setFaction(faction);
        deck.setLeaderId("leader_nr");
        List<DeckCardEntry> cards = new ArrayList<>();
        for (int i = 1; i <= 8; i++) {
            cards.add(new DeckCardEntry("card_" + i, 3));
        }
        deck.setCards(cards);
        return deck;
    }

    public static SaveDeckRequest makeValidSaveDeckRequest(Faction faction) {
        List<DeckCardEntryDto> cards = new ArrayList<>();
        for (int i = 1; i <= 8; i++) {
            cards.add(new DeckCardEntryDto("card_" + i, 3));
        }
        return new SaveDeckRequest("Test Deck", faction, "leader_nr", cards);
    }

    // --- Game DTOs ---

    public static GameStateDto makeGameStateDto(UUID gameId) {
        List<CardDto> emptyCards = List.of();
        BoardRowDto emptyRow = new BoardRowDto(emptyCards, false, false);
        CardDto leader = new CardDto("leader", "Leader", null, null, "LEADER", null, null, "NORTHERN_REALMS", "SIEGE_MASTER");

        PlayerStateDto me = new PlayerStateDto("player1@test.com", 2, 0, false, false,
                leader, 2, false, emptyCards, 15, emptyRow, emptyRow, emptyRow, emptyCards);
        OpponentStateDto opponent = new OpponentStateDto("player2@test.com", 2, 0, false, false,
                leader, 10, 15, emptyRow, emptyRow, emptyRow, emptyCards);

        return new GameStateDto(gameId, "PLAY", "PLAYER_1", "PLAYER_1", null, 1,
                emptyCards, me, opponent, null, null, null, null, null, null, false);
    }

    public static CreateGameDto makeCreateGameDto(UUID gameId, String playerId) {
        return new CreateGameDto(gameId, playerId);
    }

    public static ActiveGameDto makeActiveGameDto(UUID gameId) {
        return new ActiveGameDto(gameId);
    }

    // --- SessionContext helpers ---

    public static SessionContext makeSessionContext(String player1Id, String player2Id) {
        Card leader1 = makeLeaderCard("leader_p1", Faction.NORTHERN_REALMS, LeaderAbility.SIEGE_MASTER);
        Card leader2 = makeLeaderCard("leader_p2", Faction.NILFGAARD, LeaderAbility.EMPEROR_OF_NILFGAARD);

        List<Card> deck1 = new ArrayList<>();
        List<Card> deck2 = new ArrayList<>();
        for (int i = 1; i <= 22; i++) {
            deck1.add(makeCard("nr_card_" + i, Faction.NORTHERN_REALMS));
            deck2.add(makeCard("ng_card_" + i, Faction.NILFGAARD));
        }

        PlayerState p1 = new PlayerState(leader1, deck1);
        PlayerState p2 = new PlayerState(leader2, deck2);
        GameState gameState = new GameState(p1, p2);
        gameState.setCurrentTurn(Turn.PLAYER_1);

        return new SessionContext(gameState, player1Id, player2Id);
    }
}
