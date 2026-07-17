package com.gwent.engine.core;

import com.gwent.engine.command.PassCommand;
import com.gwent.engine.command.ResolveLeaderCommand;
import com.gwent.engine.command.UseLeaderCommand;
import com.gwent.engine.domain.*;
import com.gwent.engine.exception.command.CardNotInDeckException;
import com.gwent.engine.exception.command.CardNotInGraveyardException;
import com.gwent.engine.exception.command.InvalidRowException;
import com.gwent.engine.state.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LeaderAbilityResolverTest {

    private GwentEngine engine;

    @BeforeEach
    void setUp() {
        engine = new GwentEngine();
    }

    // =========================================================
    // SIEGE_MASTER (Northern Realms)
    // =========================================================

    @Test
    void shouldClearSiegeWeatherFromBothSidesWhenSiegeMasterUsed() {
        PlayerState p1 = playerWithLeader(LeaderAbility.SIEGE_MASTER);
        PlayerState p2 = playerWithLeader(LeaderAbility.SIEGE_MASTER);
        p1.getSiegeRow().setWeatherActive(true);
        p2.getSiegeRow().setWeatherActive(true);
        GameState state = makePlayState(p1, p2);

        engine.execute(state, new UseLeaderCommand());

        assertFalse(p1.getSiegeRow().isWeatherActive());
        assertFalse(p2.getSiegeRow().isWeatherActive());
    }

    @Test
    void shouldNotClearOtherRowsWhenSiegeMasterUsed() {
        PlayerState p1 = playerWithLeader(LeaderAbility.SIEGE_MASTER);
        p1.getMeleeRow().setWeatherActive(true);
        p1.getRangedRow().setWeatherActive(true);
        GameState state = makePlayState(p1, playerWithLeader(LeaderAbility.SIEGE_MASTER));

        engine.execute(state, new UseLeaderCommand());

        assertTrue(p1.getMeleeRow().isWeatherActive());
        assertTrue(p1.getRangedRow().isWeatherActive());
    }

    // =========================================================
    // WHITE_FLAME (Nilfgaard)
    // =========================================================

    @Test
    void shouldMoveWeatherCardFromDeckToHandWhenWhiteFlameUsed() {
        Card weatherCard = makeWeatherCard("frost", Ability.FROST);
        Card normalCard = makeUnit("u1", "Unit", 5, RowType.MELEE);
        PlayerState p1 = new PlayerState(makeLeader(LeaderAbility.WHITE_FLAME), List.of(weatherCard, normalCard));
        GameState state = makePlayState(p1, playerWithLeader(LeaderAbility.WHITE_FLAME));

        engine.execute(state, new UseLeaderCommand());

        assertTrue(p1.getHand().contains(weatherCard));
        assertFalse(p1.getDeck().contains(weatherCard));
    }

    @Test
    void shouldDoNothingWhenNoWeatherCardInDeckForWhiteFlame() {
        Card normalCard = makeUnit("u1", "Unit", 5, RowType.MELEE);
        PlayerState p1 = new PlayerState(makeLeader(LeaderAbility.WHITE_FLAME), List.of(normalCard));
        GameState state = makePlayState(p1, playerWithLeader(LeaderAbility.WHITE_FLAME));

        engine.execute(state, new UseLeaderCommand());

        assertTrue(p1.getHand().isEmpty());
        assertEquals(1, p1.getDeck().size());
    }

    @Test
    void shouldPickFirstWeatherCardWhenMultipleExistForWhiteFlame() {
        Card frost = makeWeatherCard("frost", Ability.FROST);
        Card fog = makeWeatherCard("fog", Ability.FOG);
        PlayerState p1 = new PlayerState(makeLeader(LeaderAbility.WHITE_FLAME), List.of(frost, fog));
        GameState state = makePlayState(p1, playerWithLeader(LeaderAbility.WHITE_FLAME));

        engine.execute(state, new UseLeaderCommand());

        assertEquals(1, p1.getHand().size());
        assertEquals(1, p1.getDeck().size());
    }

    // =========================================================
    // DAISY_OF_THE_VALLEY (Scoia'tael) — same logic as WHITE_FLAME
    // =========================================================

    @Test
    void shouldMoveWeatherCardFromDeckToHandWhenDaisyOfTheValleyUsed() {
        Card weatherCard = makeWeatherCard("rain", Ability.RAIN);
        PlayerState p1 = new PlayerState(makeLeader(LeaderAbility.DAISY_OF_THE_VALLEY), List.of(weatherCard));
        GameState state = makePlayState(p1, playerWithLeader(LeaderAbility.DAISY_OF_THE_VALLEY));

        engine.execute(state, new UseLeaderCommand());

        assertTrue(p1.getHand().contains(weatherCard));
        assertTrue(p1.getDeck().isEmpty());
    }

    // =========================================================
    // BRINGER_OF_DEATH (Monsters) — same logic as WHITE_FLAME
    // =========================================================

    @Test
    void shouldMoveWeatherCardFromDeckToHandWhenBringerOfDeathUsed() {
        Card weatherCard = makeWeatherCard("fog", Ability.FOG);
        PlayerState p1 = new PlayerState(makeLeader(LeaderAbility.BRINGER_OF_DEATH), List.of(weatherCard));
        GameState state = makePlayState(p1, playerWithLeader(LeaderAbility.BRINGER_OF_DEATH));

        engine.execute(state, new UseLeaderCommand());

        assertTrue(p1.getHand().contains(weatherCard));
        assertTrue(p1.getDeck().isEmpty());
    }

    @Test
    void shouldDoNothingWhenNoWeatherCardInDeckForBringerOfDeath() {
        Card normalCard = makeUnit("u1", "Unit", 5, RowType.MELEE);
        PlayerState p1 = new PlayerState(makeLeader(LeaderAbility.BRINGER_OF_DEATH), List.of(normalCard));
        GameState state = makePlayState(p1, playerWithLeader(LeaderAbility.BRINGER_OF_DEATH));

        engine.execute(state, new UseLeaderCommand());

        assertTrue(p1.getHand().isEmpty());
        assertEquals(1, p1.getDeck().size());
    }

    // =========================================================
    // DESTROYER_OF_WORLDS (Monsters)
    // =========================================================

    @Test
    void shouldDiscardTwoCardsAndDrawOneWhenDestroyerOfWorldsUsed() {
        Card discard1 = makeUnit("d1", "Discard1", 3, RowType.MELEE);
        Card discard2 = makeUnit("d2", "Discard2", 4, RowType.MELEE);
        Card keep = makeUnit("keep", "Keep", 5, RowType.MELEE);
        Card fromDeck = makeUnit("deck", "FromDeck", 6, RowType.RANGED);
        PlayerState p1 = new PlayerState(makeLeader(LeaderAbility.DESTROYER_OF_WORLDS), List.of(fromDeck));
        p1.addToHand(discard1);
        p1.addToHand(discard2);
        p1.addToHand(keep);
        GameState state = makePlayState(p1, playerWithLeader(LeaderAbility.DESTROYER_OF_WORLDS));

        engine.execute(state, new UseLeaderCommand());

        assertEquals(2, p1.getGraveyard().size());
        assertEquals(2, p1.getHand().size()); // keep + fromDeck
    }

    @Test
    void shouldDiscardOnlyAvailableCardsWhenHandHasLessThanTwoForDestroyerOfWorlds() {
        Card onlyCard = makeUnit("u1", "Only", 3, RowType.MELEE);
        PlayerState p1 = new PlayerState(makeLeader(LeaderAbility.DESTROYER_OF_WORLDS), List.of());
        p1.addToHand(onlyCard);
        GameState state = makePlayState(p1, playerWithLeader(LeaderAbility.DESTROYER_OF_WORLDS));

        engine.execute(state, new UseLeaderCommand());

        assertEquals(1, p1.getGraveyard().size());
        assertTrue(p1.getHand().isEmpty());
    }

    @Test
    void shouldNotDrawWhenDeckIsEmptyForDestroyerOfWorlds() {
        Card discard1 = makeUnit("d1", "Discard1", 3, RowType.MELEE);
        Card discard2 = makeUnit("d2", "Discard2", 4, RowType.MELEE);
        PlayerState p1 = new PlayerState(makeLeader(LeaderAbility.DESTROYER_OF_WORLDS), List.of());
        p1.addToHand(discard1);
        p1.addToHand(discard2);
        GameState state = makePlayState(p1, playerWithLeader(LeaderAbility.DESTROYER_OF_WORLDS));

        engine.execute(state, new UseLeaderCommand());

        assertEquals(2, p1.getGraveyard().size());
        assertTrue(p1.getHand().isEmpty());
    }

    // =========================================================
    // KING_BRAN (Skellige)
    // =========================================================

    @Test
    void shouldMoveAllGraveyardCardsBackToDeckWhenKingBranUsed() {
        Card g1 = makeUnit("g1", "Ghost1", 3, RowType.MELEE);
        Card g2 = makeUnit("g2", "Ghost2", 4, RowType.RANGED);
        PlayerState p1 = playerWithLeader(LeaderAbility.KING_BRAN);
        p1.addToGraveyard(g1);
        p1.addToGraveyard(g2);
        GameState state = makePlayState(p1, playerWithLeader(LeaderAbility.KING_BRAN));

        engine.execute(state, new UseLeaderCommand());

        assertTrue(p1.getGraveyard().isEmpty());
        assertTrue(p1.getDeck().contains(g1));
        assertTrue(p1.getDeck().contains(g2));
    }

    @Test
    void shouldDoNothingWhenGraveyardIsEmptyForKingBran() {
        PlayerState p1 = playerWithLeader(LeaderAbility.KING_BRAN);
        GameState state = makePlayState(p1, playerWithLeader(LeaderAbility.KING_BRAN));

        assertDoesNotThrow(() -> engine.execute(state, new UseLeaderCommand()));
        assertTrue(p1.getGraveyard().isEmpty());
        assertTrue(p1.getDeck().isEmpty());
    }

    // =========================================================
    // INVADER_OF_THE_NORTH (Nilfgaard)
    // =========================================================

    @Test
    void shouldMarkOpponentLeaderAsUsedWhenInvaderOfTheNorthUsed() {
        PlayerState p1 = playerWithLeader(LeaderAbility.INVADER_OF_THE_NORTH);
        PlayerState p2 = playerWithLeader(LeaderAbility.SIEGE_MASTER);
        GameState state = makePlayState(p1, p2);

        engine.execute(state, new UseLeaderCommand());

        assertTrue(p2.isLeaderUsed());
    }

    @Test
    void shouldDoNothingWhenOpponentLeaderAlreadyUsedForInvaderOfTheNorth() {
        PlayerState p1 = playerWithLeader(LeaderAbility.INVADER_OF_THE_NORTH);
        PlayerState p2 = playerWithLeader(LeaderAbility.SIEGE_MASTER);
        p2.useLeader();
        GameState state = makePlayState(p1, p2);

        assertDoesNotThrow(() -> engine.execute(state, new UseLeaderCommand()));
        assertTrue(p2.isLeaderUsed());
    }

    // =========================================================
    // LORD_COMMANDER (Northern Realms) — destroy strongest in siege row
    // =========================================================

    @Test
    void shouldDestroyStrongestUnitInSiegeRowWhenScoreAtLeast10() {
        PlayerState p1 = playerWithLeader(LeaderAbility.LORD_COMMANDER);
        PlayerState p2 = playerWithLeader(LeaderAbility.LORD_COMMANDER);
        Card strong = makeUnit("s1", "Strong", 7, RowType.SIEGE);
        Card weak = makeUnit("w1", "Weak", 3, RowType.SIEGE);
        p2.getSiegeRow().addCard(strong);
        p2.getSiegeRow().addCard(weak);
        GameState state = makePlayState(p1, p2);

        engine.execute(state, new UseLeaderCommand());

        assertFalse(p2.getSiegeRow().getCards().contains(strong));
        assertTrue(p2.getSiegeRow().getCards().contains(weak));
        assertTrue(p2.getGraveyard().contains(strong));
    }

    @Test
    void shouldNotDestroyWhenSiegeRowScoreBelow10() {
        PlayerState p1 = playerWithLeader(LeaderAbility.LORD_COMMANDER);
        PlayerState p2 = playerWithLeader(LeaderAbility.LORD_COMMANDER);
        Card unit = makeUnit("u1", "Unit", 5, RowType.SIEGE);
        p2.getSiegeRow().addCard(unit);
        GameState state = makePlayState(p1, p2);

        engine.execute(state, new UseLeaderCommand());

        assertTrue(p2.getSiegeRow().getCards().contains(unit));
        assertTrue(p2.getGraveyard().isEmpty());
    }

    @Test
    void shouldNotDestroyHeroCardsInSiegeRow() {
        PlayerState p1 = playerWithLeader(LeaderAbility.LORD_COMMANDER);
        PlayerState p2 = playerWithLeader(LeaderAbility.LORD_COMMANDER);
        Card hero = makeHero("h1", "Hero", 10, RowType.SIEGE);
        p2.getSiegeRow().addCard(hero);
        GameState state = makePlayState(p1, p2);

        engine.execute(state, new UseLeaderCommand());

        assertTrue(p2.getSiegeRow().getCards().contains(hero));
        assertTrue(p2.getGraveyard().isEmpty());
    }

    @Test
    void shouldDestroyFirstStrongestOnTieInSiegeRow() {
        PlayerState p1 = playerWithLeader(LeaderAbility.LORD_COMMANDER);
        PlayerState p2 = playerWithLeader(LeaderAbility.LORD_COMMANDER);
        Card first = makeUnit("f1", "First", 5, RowType.SIEGE);
        Card second = makeUnit("s1", "Second", 5, RowType.SIEGE);
        p2.getSiegeRow().addCard(first);
        p2.getSiegeRow().addCard(second);
        GameState state = makePlayState(p1, p2);

        engine.execute(state, new UseLeaderCommand());

        assertFalse(p2.getSiegeRow().getCards().contains(first));
        assertTrue(p2.getSiegeRow().getCards().contains(second));
        assertTrue(p2.getGraveyard().contains(first));
    }

    // =========================================================
    // QUEEN_OF_DOL_BLATHANNA (Scoia'tael) — destroy strongest in melee row
    // =========================================================

    @Test
    void shouldDestroyStrongestUnitInMeleeRowWhenScoreAtLeast10() {
        PlayerState p1 = playerWithLeader(LeaderAbility.QUEEN_OF_DOL_BLATHANNA);
        PlayerState p2 = playerWithLeader(LeaderAbility.QUEEN_OF_DOL_BLATHANNA);
        Card strong = makeUnit("s1", "Strong", 8, RowType.MELEE);
        Card weak = makeUnit("w1", "Weak", 3, RowType.MELEE);
        p2.getMeleeRow().addCard(strong);
        p2.getMeleeRow().addCard(weak);
        GameState state = makePlayState(p1, p2);

        engine.execute(state, new UseLeaderCommand());

        assertFalse(p2.getMeleeRow().getCards().contains(strong));
        assertTrue(p2.getMeleeRow().getCards().contains(weak));
        assertTrue(p2.getGraveyard().contains(strong));
    }

    @Test
    void shouldNotDestroyWhenMeleeRowScoreBelow10() {
        PlayerState p1 = playerWithLeader(LeaderAbility.QUEEN_OF_DOL_BLATHANNA);
        PlayerState p2 = playerWithLeader(LeaderAbility.QUEEN_OF_DOL_BLATHANNA);
        Card unit = makeUnit("u1", "Unit", 5, RowType.MELEE);
        p2.getMeleeRow().addCard(unit);
        GameState state = makePlayState(p1, p2);

        engine.execute(state, new UseLeaderCommand());

        assertTrue(p2.getMeleeRow().getCards().contains(unit));
    }

    @Test
    void shouldNotDestroyHeroCardsInMeleeRow() {
        PlayerState p1 = playerWithLeader(LeaderAbility.QUEEN_OF_DOL_BLATHANNA);
        PlayerState p2 = playerWithLeader(LeaderAbility.QUEEN_OF_DOL_BLATHANNA);
        Card hero = makeHero("h1", "Hero", 10, RowType.MELEE);
        p2.getMeleeRow().addCard(hero);
        GameState state = makePlayState(p1, p2);

        engine.execute(state, new UseLeaderCommand());

        assertTrue(p2.getMeleeRow().getCards().contains(hero));
    }

    // =========================================================
    // EMPEROR_OF_NILFGAARD (Nilfgaard) — reveal cards
    // =========================================================

    @Test
    void shouldRevealThreeRandomCardsFromOpponentHand() {
        PlayerState p1 = playerWithLeader(LeaderAbility.EMPEROR_OF_NILFGAARD);
        PlayerState p2 = playerWithLeader(LeaderAbility.EMPEROR_OF_NILFGAARD);
        for (int i = 0; i < 5; i++) {
            p2.addToHand(makeUnit("u" + i, "Unit" + i, 3 + i, RowType.MELEE));
        }
        GameState state = makePlayState(p1, p2);

        engine.execute(state, new UseLeaderCommand());

        assertNotNull(state.getRevealedCards());
        assertEquals(3, state.getRevealedCards().size());
        for (Card revealed : state.getRevealedCards()) {
            assertTrue(p2.getHand().contains(revealed));
        }
    }

    @Test
    void shouldRevealAllCardsWhenOpponentHasLessThanThree() {
        PlayerState p1 = playerWithLeader(LeaderAbility.EMPEROR_OF_NILFGAARD);
        PlayerState p2 = playerWithLeader(LeaderAbility.EMPEROR_OF_NILFGAARD);
        Card c1 = makeUnit("u1", "Unit1", 3, RowType.MELEE);
        Card c2 = makeUnit("u2", "Unit2", 4, RowType.MELEE);
        p2.addToHand(c1);
        p2.addToHand(c2);
        GameState state = makePlayState(p1, p2);

        engine.execute(state, new UseLeaderCommand());

        assertNotNull(state.getRevealedCards());
        assertEquals(2, state.getRevealedCards().size());
    }

    @Test
    void shouldSetEmptyRevealedCardsWhenOpponentHandIsEmpty() {
        PlayerState p1 = playerWithLeader(LeaderAbility.EMPEROR_OF_NILFGAARD);
        PlayerState p2 = playerWithLeader(LeaderAbility.EMPEROR_OF_NILFGAARD);
        GameState state = makePlayState(p1, p2);

        engine.execute(state, new UseLeaderCommand());

        assertNotNull(state.getRevealedCards());
        assertTrue(state.getRevealedCards().isEmpty());
    }

    // =========================================================
    // KING_OF_THE_WILD_HUNT (Monsters) — restore 1 unit from graveyard
    // =========================================================

    @Test
    void shouldSetPendingAbilityWhenKingOfTheWildHuntUsedWithUnitsInGraveyard() {
        PlayerState p1 = playerWithLeader(LeaderAbility.KING_OF_THE_WILD_HUNT);
        p1.addToGraveyard(makeUnit("g1", "Ghost", 5, RowType.MELEE));
        GameState state = makePlayState(p1, playerWithLeader(LeaderAbility.KING_OF_THE_WILD_HUNT));

        engine.execute(state, new UseLeaderCommand());

        assertEquals(PendingAbility.LEADER_GRAVEYARD_PICK, state.getPendingAbility());
        assertEquals(LeaderAbility.KING_OF_THE_WILD_HUNT, state.getPendingLeaderAbility());
        assertEquals(Turn.PLAYER_1, state.getCurrentTurn());
    }

    @Test
    void shouldRestoreUnitFromGraveyardWhenKingOfTheWildHuntResolved() {
        PlayerState p1 = playerWithLeader(LeaderAbility.KING_OF_THE_WILD_HUNT);
        Card unit = makeUnit("g1", "Ghost", 5, RowType.MELEE);
        p1.addToGraveyard(unit);
        GameState state = makePlayState(p1, playerWithLeader(LeaderAbility.KING_OF_THE_WILD_HUNT));

        engine.execute(state, new UseLeaderCommand());
        engine.execute(state, new ResolveLeaderCommand(unit));

        assertTrue(p1.getMeleeRow().getCards().contains(unit));
        assertFalse(p1.getGraveyard().contains(unit));
        assertNull(state.getPendingAbility());
    }

    @Test
    void shouldDoNothingWhenGraveyardHasNoUnitsForKingOfTheWildHunt() {
        PlayerState p1 = playerWithLeader(LeaderAbility.KING_OF_THE_WILD_HUNT);
        GameState state = makePlayState(p1, playerWithLeader(LeaderAbility.KING_OF_THE_WILD_HUNT));

        engine.execute(state, new UseLeaderCommand());

        assertNull(state.getPendingAbility());
        assertEquals(Turn.PLAYER_2, state.getCurrentTurn());
    }

    @Test
    void shouldThrowWhenResolvingGraveyardPickWithNonUnitCard() {
        PlayerState p1 = playerWithLeader(LeaderAbility.KING_OF_THE_WILD_HUNT);
        Card hero = makeHero("h1", "Hero", 10, RowType.MELEE);
        Card unit = makeUnit("u1", "Unit", 3, RowType.MELEE);
        p1.addToGraveyard(hero);
        p1.addToGraveyard(unit);
        GameState state = makePlayState(p1, playerWithLeader(LeaderAbility.KING_OF_THE_WILD_HUNT));

        engine.execute(state, new UseLeaderCommand());

        assertThrows(InvalidRowException.class, () ->
                engine.execute(state, new ResolveLeaderCommand(hero)));
    }

    @Test
    void shouldThrowWhenResolvingGraveyardPickWithCardNotInGraveyard() {
        PlayerState p1 = playerWithLeader(LeaderAbility.KING_OF_THE_WILD_HUNT);
        Card inGraveyard = makeUnit("g1", "InGrave", 5, RowType.MELEE);
        Card notInGraveyard = makeUnit("n1", "NotInGrave", 5, RowType.MELEE);
        p1.addToGraveyard(inGraveyard);
        GameState state = makePlayState(p1, playerWithLeader(LeaderAbility.KING_OF_THE_WILD_HUNT));

        engine.execute(state, new UseLeaderCommand());

        assertThrows(CardNotInGraveyardException.class, () ->
                engine.execute(state, new ResolveLeaderCommand(notInGraveyard)));
    }

    // =========================================================
    // RELENTLESS (Nilfgaard) — pick from opponent graveyard
    // =========================================================

    @Test
    void shouldSetPendingAbilityWhenRelentlessUsedWithUnitsInOpponentGraveyard() {
        PlayerState p1 = playerWithLeader(LeaderAbility.RELENTLESS);
        PlayerState p2 = playerWithLeader(LeaderAbility.RELENTLESS);
        p2.addToGraveyard(makeUnit("g1", "Ghost", 5, RowType.MELEE));
        GameState state = makePlayState(p1, p2);

        engine.execute(state, new UseLeaderCommand());

        assertEquals(PendingAbility.LEADER_OPPONENT_GRAVEYARD_PICK, state.getPendingAbility());
        assertEquals(Turn.PLAYER_1, state.getCurrentTurn());
    }

    @Test
    void shouldPlaceOpponentGraveyardUnitOnCurrentPlayerSide() {
        PlayerState p1 = playerWithLeader(LeaderAbility.RELENTLESS);
        PlayerState p2 = playerWithLeader(LeaderAbility.RELENTLESS);
        Card unit = makeUnit("g1", "Ghost", 5, RowType.RANGED);
        p2.addToGraveyard(unit);
        GameState state = makePlayState(p1, p2);

        engine.execute(state, new UseLeaderCommand());
        engine.execute(state, new ResolveLeaderCommand(unit));

        assertTrue(p1.getRangedRow().getCards().contains(unit));
        assertFalse(p2.getGraveyard().contains(unit));
        assertNull(state.getPendingAbility());
    }

    @Test
    void shouldDoNothingWhenOpponentGraveyardHasNoUnitsForRelentless() {
        PlayerState p1 = playerWithLeader(LeaderAbility.RELENTLESS);
        PlayerState p2 = playerWithLeader(LeaderAbility.RELENTLESS);
        GameState state = makePlayState(p1, p2);

        engine.execute(state, new UseLeaderCommand());

        assertNull(state.getPendingAbility());
        assertEquals(Turn.PLAYER_2, state.getCurrentTurn());
    }

    @Test
    void shouldThrowWhenResolvingOpponentGraveyardPickWithCardNotInGraveyard() {
        PlayerState p1 = playerWithLeader(LeaderAbility.RELENTLESS);
        PlayerState p2 = playerWithLeader(LeaderAbility.RELENTLESS);
        Card inGraveyard = makeUnit("g1", "InGrave", 5, RowType.MELEE);
        Card notThere = makeUnit("n1", "NotThere", 5, RowType.MELEE);
        p2.addToGraveyard(inGraveyard);
        GameState state = makePlayState(p1, p2);

        engine.execute(state, new UseLeaderCommand());

        assertThrows(CardNotInGraveyardException.class, () ->
                engine.execute(state, new ResolveLeaderCommand(notThere)));
    }

    // =========================================================
    // KING_OF_TEMERIA (Northern Realms) — pick from deck, play immediately
    // =========================================================

    @Test
    void shouldSetDeckPickPendingWhenKingOfTemeriaUsed() {
        Card deckCard = makeUnit("d1", "DeckUnit", 5, RowType.MELEE);
        PlayerState p1 = new PlayerState(makeLeader(LeaderAbility.KING_OF_TEMERIA), List.of(deckCard));
        GameState state = makePlayState(p1, playerWithLeader(LeaderAbility.KING_OF_TEMERIA));

        engine.execute(state, new UseLeaderCommand());

        assertEquals(PendingAbility.LEADER_DECK_PICK, state.getPendingAbility());
        assertEquals(LeaderAbility.KING_OF_TEMERIA, state.getPendingLeaderAbility());
    }

    @Test
    void shouldPlayCardImmediatelyWhenKingOfTemeriaResolved() {
        Card deckCard = makeUnit("d1", "DeckUnit", 5, RowType.SIEGE);
        PlayerState p1 = new PlayerState(makeLeader(LeaderAbility.KING_OF_TEMERIA), List.of(deckCard));
        GameState state = makePlayState(p1, playerWithLeader(LeaderAbility.KING_OF_TEMERIA));

        engine.execute(state, new UseLeaderCommand());
        engine.execute(state, new ResolveLeaderCommand(deckCard));

        assertTrue(p1.getSiegeRow().getCards().contains(deckCard));
        assertNull(state.getPendingAbility());
    }

    @Test
    void shouldDoNothingWhenDeckIsEmptyForKingOfTemeria() {
        PlayerState p1 = playerWithLeader(LeaderAbility.KING_OF_TEMERIA);
        GameState state = makePlayState(p1, playerWithLeader(LeaderAbility.KING_OF_TEMERIA));

        engine.execute(state, new UseLeaderCommand());

        assertNull(state.getPendingAbility());
        assertEquals(Turn.PLAYER_2, state.getCurrentTurn());
    }

    @Test
    void shouldThrowWhenResolvingDeckPickWithCardNotInDeck() {
        Card deckCard = makeUnit("d1", "DeckUnit", 5, RowType.MELEE);
        Card notInDeck = makeUnit("n1", "NotInDeck", 5, RowType.MELEE);
        PlayerState p1 = new PlayerState(makeLeader(LeaderAbility.KING_OF_TEMERIA), List.of(deckCard));
        GameState state = makePlayState(p1, playerWithLeader(LeaderAbility.KING_OF_TEMERIA));

        engine.execute(state, new UseLeaderCommand());

        assertThrows(CardNotInDeckException.class, () ->
                engine.execute(state, new ResolveLeaderCommand(notInDeck)));
    }

    @Test
    void shouldPlayWeatherCardFromDeckWhenKingOfTemeriaResolved() {
        Card weatherCard = makeWeatherCard("frost", Ability.FROST);
        PlayerState p1 = new PlayerState(makeLeader(LeaderAbility.KING_OF_TEMERIA), List.of(weatherCard));
        PlayerState p2 = playerWithLeader(LeaderAbility.KING_OF_TEMERIA);
        GameState state = makePlayState(p1, p2);

        engine.execute(state, new UseLeaderCommand());
        engine.execute(state, new ResolveLeaderCommand(weatherCard));

        assertTrue(state.getPlayer1().getMeleeRow().isWeatherActive());
        assertTrue(state.getPlayer2().getMeleeRow().isWeatherActive());
    }

    // =========================================================
    // COMMANDER_OF_THE_RED_RIDERS (Monsters) — pick from deck, then discard
    // =========================================================

    @Test
    void shouldSetDeckPickPendingWhenCommanderOfTheRedRidersUsed() {
        Card deckCard = makeUnit("d1", "DeckUnit", 5, RowType.MELEE);
        PlayerState p1 = new PlayerState(makeLeader(LeaderAbility.COMMANDER_OF_THE_RED_RIDERS), List.of(deckCard));
        GameState state = makePlayState(p1, playerWithLeader(LeaderAbility.COMMANDER_OF_THE_RED_RIDERS));

        engine.execute(state, new UseLeaderCommand());

        assertEquals(PendingAbility.LEADER_DECK_PICK, state.getPendingAbility());
        assertEquals(LeaderAbility.COMMANDER_OF_THE_RED_RIDERS, state.getPendingLeaderAbility());
    }

    @Test
    void shouldAddCardToHandAndTransitionToDiscardAfterDeckPick() {
        Card deckCard = makeUnit("d1", "DeckUnit", 5, RowType.MELEE);
        Card handCard = makeUnit("h1", "HandUnit", 3, RowType.MELEE);
        PlayerState p1 = new PlayerState(makeLeader(LeaderAbility.COMMANDER_OF_THE_RED_RIDERS), List.of(deckCard));
        p1.addToHand(handCard);
        GameState state = makePlayState(p1, playerWithLeader(LeaderAbility.COMMANDER_OF_THE_RED_RIDERS));

        engine.execute(state, new UseLeaderCommand());
        engine.execute(state, new ResolveLeaderCommand(deckCard));

        assertTrue(p1.getHand().contains(deckCard));
        assertEquals(PendingAbility.LEADER_HAND_DISCARD, state.getPendingAbility());
    }

    @Test
    void shouldDiscardCardFromHandAndClearPending() {
        Card deckCard = makeUnit("d1", "DeckUnit", 5, RowType.MELEE);
        Card handCard = makeUnit("h1", "HandUnit", 3, RowType.MELEE);
        PlayerState p1 = new PlayerState(makeLeader(LeaderAbility.COMMANDER_OF_THE_RED_RIDERS), List.of(deckCard));
        p1.addToHand(handCard);
        GameState state = makePlayState(p1, playerWithLeader(LeaderAbility.COMMANDER_OF_THE_RED_RIDERS));

        engine.execute(state, new UseLeaderCommand());
        engine.execute(state, new ResolveLeaderCommand(deckCard)); // pick from deck
        engine.execute(state, new ResolveLeaderCommand(handCard)); // discard from hand

        assertFalse(p1.getHand().contains(handCard));
        assertTrue(p1.getGraveyard().contains(handCard));
        assertNull(state.getPendingAbility());
    }

    @Test
    void shouldDoNothingWhenDeckIsEmptyForCommanderOfTheRedRiders() {
        PlayerState p1 = playerWithLeader(LeaderAbility.COMMANDER_OF_THE_RED_RIDERS);
        GameState state = makePlayState(p1, playerWithLeader(LeaderAbility.COMMANDER_OF_THE_RED_RIDERS));

        engine.execute(state, new UseLeaderCommand());

        assertNull(state.getPendingAbility());
    }

    // =========================================================
    // CLAN_AN_CRAITE (Skellige) — restore 2 units from graveyard
    // =========================================================

    @Test
    void shouldSetGraveyardPickWithCount2WhenClanAnCraiteUsed() {
        PlayerState p1 = playerWithLeader(LeaderAbility.CLAN_AN_CRAITE);
        p1.addToGraveyard(makeUnit("g1", "Ghost1", 5, RowType.MELEE));
        p1.addToGraveyard(makeUnit("g2", "Ghost2", 4, RowType.RANGED));
        GameState state = makePlayState(p1, playerWithLeader(LeaderAbility.CLAN_AN_CRAITE));

        engine.execute(state, new UseLeaderCommand());

        assertEquals(PendingAbility.LEADER_GRAVEYARD_PICK, state.getPendingAbility());
        assertEquals(LeaderAbility.CLAN_AN_CRAITE, state.getPendingLeaderAbility());
        assertEquals(2, state.getPendingAbilityCount());
    }

    @Test
    void shouldRestoreTwoUnitsSequentiallyFromGraveyard() {
        PlayerState p1 = playerWithLeader(LeaderAbility.CLAN_AN_CRAITE);
        Card g1 = makeUnit("g1", "Ghost1", 5, RowType.MELEE);
        Card g2 = makeUnit("g2", "Ghost2", 4, RowType.RANGED);
        p1.addToGraveyard(g1);
        p1.addToGraveyard(g2);
        GameState state = makePlayState(p1, playerWithLeader(LeaderAbility.CLAN_AN_CRAITE));

        engine.execute(state, new UseLeaderCommand());

        // first pick
        engine.execute(state, new ResolveLeaderCommand(g1));
        assertTrue(p1.getMeleeRow().getCards().contains(g1));
        assertEquals(PendingAbility.LEADER_GRAVEYARD_PICK, state.getPendingAbility());
        assertEquals(1, state.getPendingAbilityCount());

        // second pick
        engine.execute(state, new ResolveLeaderCommand(g2));
        assertTrue(p1.getRangedRow().getCards().contains(g2));
        assertNull(state.getPendingAbility());
    }

    @Test
    void shouldRestoreOnlyOneUnitWhenGraveyardHasOnlyOneForClanAnCraite() {
        PlayerState p1 = playerWithLeader(LeaderAbility.CLAN_AN_CRAITE);
        Card g1 = makeUnit("g1", "Ghost1", 5, RowType.MELEE);
        p1.addToGraveyard(g1);
        GameState state = makePlayState(p1, playerWithLeader(LeaderAbility.CLAN_AN_CRAITE));

        engine.execute(state, new UseLeaderCommand());

        assertEquals(1, state.getPendingAbilityCount());

        engine.execute(state, new ResolveLeaderCommand(g1));
        assertTrue(p1.getMeleeRow().getCards().contains(g1));
        assertNull(state.getPendingAbility());
    }

    @Test
    void shouldDoNothingWhenGraveyardHasNoUnitsForClanAnCraite() {
        PlayerState p1 = playerWithLeader(LeaderAbility.CLAN_AN_CRAITE);
        GameState state = makePlayState(p1, playerWithLeader(LeaderAbility.CLAN_AN_CRAITE));

        engine.execute(state, new UseLeaderCommand());

        assertNull(state.getPendingAbility());
    }

    // =========================================================
    // NORTH_COMMANDER (Northern Realms) — +1 to siege units
    // =========================================================

    @Test
    void shouldSetLeaderBonusPowerOnSiegeRow() {
        PlayerState p1 = playerWithLeader(LeaderAbility.NORTH_COMMANDER);
        GameState state = makePlayState(p1, playerWithLeader(LeaderAbility.NORTH_COMMANDER));

        engine.execute(state, new UseLeaderCommand());

        assertEquals(1, p1.getSiegeRow().getLeaderBonusPower());
        assertEquals(0, p1.getMeleeRow().getLeaderBonusPower());
        assertEquals(0, p1.getRangedRow().getLeaderBonusPower());
    }

    @Test
    void shouldIncreaseScoreWithLeaderBonusPower() {
        PlayerState p1 = playerWithLeader(LeaderAbility.NORTH_COMMANDER);
        Card unit = makeUnit("u1", "Unit", 5, RowType.SIEGE);
        p1.getSiegeRow().addCard(unit);
        GameState state = makePlayState(p1, playerWithLeader(LeaderAbility.NORTH_COMMANDER));

        engine.execute(state, new UseLeaderCommand());

        assertEquals(6, engine.calculateScore(p1));
    }

    @Test
    void shouldNotApplyLeaderBonusPowerToHeroCards() {
        PlayerState p1 = playerWithLeader(LeaderAbility.NORTH_COMMANDER);
        Card hero = makeHero("h1", "Hero", 10, RowType.SIEGE);
        p1.getSiegeRow().addCard(hero);
        GameState state = makePlayState(p1, playerWithLeader(LeaderAbility.NORTH_COMMANDER));

        engine.execute(state, new UseLeaderCommand());

        assertEquals(10, engine.calculateScore(p1));
    }

    @Test
    void shouldApplyLeaderBonusToEachUnitInSiegeRow() {
        PlayerState p1 = playerWithLeader(LeaderAbility.NORTH_COMMANDER);
        Card u1 = makeUnit("u1", "Unit1", 3, RowType.SIEGE);
        Card u2 = makeUnit("u2", "Unit2", 4, RowType.SIEGE);
        p1.getSiegeRow().addCard(u1);
        p1.getSiegeRow().addCard(u2);
        GameState state = makePlayState(p1, playerWithLeader(LeaderAbility.NORTH_COMMANDER));

        engine.execute(state, new UseLeaderCommand());

        assertEquals(9, engine.calculateScore(p1));
    }

    @Test
    void shouldReduceToOneWhenWeatherActiveEvenWithLeaderBonus() {
        PlayerState p1 = playerWithLeader(LeaderAbility.NORTH_COMMANDER);
        Card unit = makeUnit("u1", "Unit", 5, RowType.SIEGE);
        p1.getSiegeRow().addCard(unit);
        p1.getSiegeRow().setWeatherActive(true);
        GameState state = makePlayState(p1, playerWithLeader(LeaderAbility.NORTH_COMMANDER));

        engine.execute(state, new UseLeaderCommand());

        assertEquals(1, engine.calculateScore(p1));
    }

    @Test
    void shouldResetLeaderBonusPowerBetweenRounds() {
        PlayerState p1 = playerWithLeader(LeaderAbility.NORTH_COMMANDER);
        PlayerState p2 = playerWithLeader(LeaderAbility.NORTH_COMMANDER);
        GameState state = makePlayState(p1, p2);

        engine.execute(state, new UseLeaderCommand());
        assertEquals(1, p1.getSiegeRow().getLeaderBonusPower());

        // both pass → round ends → new round starts → rows cleared
        engine.execute(state, new PassCommand()); // p2 passes
        engine.execute(state, new PassCommand()); // p1 passes (turn switched to p1 after UseLeader)

        assertEquals(0, p1.getSiegeRow().getLeaderBonusPower());
    }

    // =========================================================
    // HOPE_OF_THE_AEN_SEIDHE (Scoia'tael) — +1 to melee and ranged
    // =========================================================

    @Test
    void shouldSetLeaderBonusPowerOnMeleeAndRangedRows() {
        PlayerState p1 = playerWithLeader(LeaderAbility.HOPE_OF_THE_AEN_SEIDHE);
        GameState state = makePlayState(p1, playerWithLeader(LeaderAbility.HOPE_OF_THE_AEN_SEIDHE));

        engine.execute(state, new UseLeaderCommand());

        assertEquals(1, p1.getMeleeRow().getLeaderBonusPower());
        assertEquals(1, p1.getRangedRow().getLeaderBonusPower());
        assertEquals(0, p1.getSiegeRow().getLeaderBonusPower());
    }

    @Test
    void shouldIncreaseScoreInMeleeAndRangedWithLeaderBonus() {
        PlayerState p1 = playerWithLeader(LeaderAbility.HOPE_OF_THE_AEN_SEIDHE);
        Card meleeUnit = makeUnit("m1", "MeleeUnit", 4, RowType.MELEE);
        Card rangedUnit = makeUnit("r1", "RangedUnit", 3, RowType.RANGED);
        Card siegeUnit = makeUnit("s1", "SiegeUnit", 5, RowType.SIEGE);
        p1.getMeleeRow().addCard(meleeUnit);
        p1.getRangedRow().addCard(rangedUnit);
        p1.getSiegeRow().addCard(siegeUnit);
        GameState state = makePlayState(p1, playerWithLeader(LeaderAbility.HOPE_OF_THE_AEN_SEIDHE));

        engine.execute(state, new UseLeaderCommand());

        assertEquals(14, engine.calculateScore(p1));
    }

    // =========================================================
    // PUREBLOOD_ELF — should not throw (no-op)
    // =========================================================

    @Test
    void shouldNotThrowForPurebloodElf() {
        PlayerState p1 = playerWithLeader(LeaderAbility.PUREBLOOD_ELF);
        GameState state = makePlayState(p1, playerWithLeader(LeaderAbility.PUREBLOOD_ELF));

        assertDoesNotThrow(() -> engine.execute(state, new UseLeaderCommand()));
        assertTrue(p1.isLeaderUsed());
    }

    // =========================================================
    // Helpers
    // =========================================================

    private Card makeLeader(LeaderAbility ability) {
        return new Card("leader", "Leader", Faction.NEUTRAL, CardType.LEADER, null, ability, null, null);
    }

    private Card makeUnit(String id, String name, int power, RowType rowType) {
        return new Card(id, name, Faction.NEUTRAL, CardType.UNIT, null, null, rowType, power);
    }

    private Card makeHero(String id, String name, int power, RowType rowType) {
        return new Card(id, name, Faction.NEUTRAL, CardType.HERO, null, null, rowType, power);
    }

    private Card makeWeatherCard(String id, Ability ability) {
        return new Card(id, ability.name(), Faction.NEUTRAL, CardType.WEATHER, ability, null, null, null);
    }

    private PlayerState playerWithLeader(LeaderAbility ability) {
        return new PlayerState(makeLeader(ability), List.of());
    }

    private GameState makePlayState(PlayerState p1, PlayerState p2) {
        GameState state = new GameState(p1, p2);
        state.setCurrentTurn(Turn.PLAYER_1);
        state.setPhase(GamePhase.REDRAW);
        state.setPhase(GamePhase.PLAY);
        return state;
    }
}
