package com.gwent.engine.core;

import com.gwent.engine.command.*;
import com.gwent.engine.domain.*;
import com.gwent.engine.state.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FactionPassiveResolverTest {

    private GwentEngine engine;

    @BeforeEach
    void setUp() {
        engine = new GwentEngine();
    }

    // =========================================================
    // Nilfgaard — wins ties
    // =========================================================

    @Test
    void shouldMakeOnlyOpponentLoseLifeOnTieWhenNilfgaard() {
        PlayerState p1 = new PlayerState(makeLeader(Faction.NILFGAARD), List.of());
        PlayerState p2 = new PlayerState(makeLeader(Faction.NORTHERN_REALMS), List.of());
        GameState state = makePlayState(p1, p2);

        engine.execute(state, new PassCommand());
        engine.execute(state, new PassCommand());

        assertEquals(2, p1.getLives());
        assertEquals(1, p2.getLives());
    }

    @Test
    void shouldMakeBothLoseLifeOnTieWhenBothNilfgaard() {
        PlayerState p1 = new PlayerState(makeLeader(Faction.NILFGAARD), List.of());
        PlayerState p2 = new PlayerState(makeLeader(Faction.NILFGAARD), List.of());
        GameState state = makePlayState(p1, p2);

        engine.execute(state, new PassCommand());
        engine.execute(state, new PassCommand());

        assertEquals(1, p1.getLives());
        assertEquals(1, p2.getLives());
    }

    @Test
    void shouldNotActivateNilfgaardPassiveWhenNotATie() {
        Card strong = makeUnit("strong", "Strong", 10, RowType.MELEE);
        PlayerState p1 = new PlayerState(makeLeader(Faction.NILFGAARD), List.of());
        p1.addToHand(strong);
        PlayerState p2 = new PlayerState(makeLeader(Faction.NORTHERN_REALMS), List.of());
        GameState state = makePlayState(p1, p2);

        engine.execute(state, new PlayCardCommand(strong, RowType.MELEE));
        engine.execute(state, new PassCommand());
        engine.execute(state, new PassCommand());

        assertEquals(2, p1.getLives());
        assertEquals(1, p2.getLives());
    }

    // =========================================================
    // Northern Realms — draws +1 card when winning
    // =========================================================

    @Test
    void shouldDrawExtraCardWhenNorthernRealmsWinsRound() {
        Card strong = makeUnit("strong", "Strong", 10, RowType.MELEE);
        Card deckCard = makeUnit("deck", "Deck", 3, RowType.MELEE);
        PlayerState p1 = new PlayerState(makeLeader(Faction.NORTHERN_REALMS), List.of(deckCard));
        p1.addToHand(strong);
        PlayerState p2 = new PlayerState(makeLeader(Faction.NEUTRAL), List.of());
        GameState state = makePlayState(p1, p2);

        engine.execute(state, new PlayCardCommand(strong, RowType.MELEE));
        engine.execute(state, new PassCommand());
        engine.execute(state, new PassCommand());

        assertTrue(p1.getHand().contains(deckCard));
    }

    @Test
    void shouldNotDrawWhenNorthernRealmsLosesRound() {
        Card strong = makeUnit("strong", "Strong", 10, RowType.MELEE);
        Card deckCard = makeUnit("deck", "Deck", 3, RowType.MELEE);
        PlayerState p1 = new PlayerState(makeLeader(Faction.NORTHERN_REALMS), List.of(deckCard));
        PlayerState p2 = new PlayerState(makeLeader(Faction.NEUTRAL), List.of());
        p2.addToHand(strong);
        GameState state = makePlayState(p1, p2);

        engine.execute(state, new PassCommand());
        engine.execute(state, new PlayCardCommand(strong, RowType.MELEE));
        engine.execute(state, new PassCommand());

        assertFalse(p1.getHand().contains(deckCard));
    }

    @Test
    void shouldNotDrawWhenDeckIsEmpty() {
        Card strong = makeUnit("strong", "Strong", 10, RowType.MELEE);
        PlayerState p1 = new PlayerState(makeLeader(Faction.NORTHERN_REALMS), List.of());
        p1.addToHand(strong);
        PlayerState p2 = new PlayerState(makeLeader(Faction.NEUTRAL), List.of());
        GameState state = makePlayState(p1, p2);

        engine.execute(state, new PlayCardCommand(strong, RowType.MELEE));
        engine.execute(state, new PassCommand());
        engine.execute(state, new PassCommand());

        assertTrue(p1.getHand().isEmpty());
    }

    // =========================================================
    // Monsters — keeps 1 random unit on board
    // =========================================================

    @Test
    void shouldKeepOneUnitOnBoardAfterRoundEnd() {
        Card unit1 = makeUnit("u1", "Unit1", 5, RowType.MELEE);
        Card unit2 = makeUnit("u2", "Unit2", 3, RowType.MELEE);
        PlayerState p1 = new PlayerState(makeLeader(Faction.MONSTER), List.of());
        p1.addToHand(unit1);
        p1.addToHand(unit2);
        PlayerState p2 = new PlayerState(makeLeader(Faction.NEUTRAL), List.of());
        GameState state = makePlayState(p1, p2);

        engine.execute(state, new PlayCardCommand(unit1, RowType.MELEE));
        engine.execute(state, new PassCommand()); // p2 passes, turn→p1
        engine.execute(state, new PlayCardCommand(unit2, RowType.MELEE)); // p1 plays, p2 passed → no switch
        engine.execute(state, new PassCommand()); // p1 passes → both passed → resolveRound

        List<Card> meleeCards = p1.getMeleeRow().getCards();
        assertEquals(1, meleeCards.size());
        assertTrue(meleeCards.contains(unit1) || meleeCards.contains(unit2));
    }

    @Test
    void shouldSendRemainingCardsToGraveyard() {
        Card unit1 = makeUnit("u1", "Unit1", 5, RowType.MELEE);
        Card unit2 = makeUnit("u2", "Unit2", 3, RowType.MELEE);
        PlayerState p1 = new PlayerState(makeLeader(Faction.MONSTER), List.of());
        p1.addToHand(unit1);
        p1.addToHand(unit2);
        PlayerState p2 = new PlayerState(makeLeader(Faction.NEUTRAL), List.of());
        GameState state = makePlayState(p1, p2);

        engine.execute(state, new PlayCardCommand(unit1, RowType.MELEE));
        engine.execute(state, new PassCommand());
        engine.execute(state, new PlayCardCommand(unit2, RowType.MELEE));
        engine.execute(state, new PassCommand());

        assertEquals(1, p1.getMeleeRow().getCards().size());
        assertEquals(1, p1.getGraveyard().size());
    }

    @Test
    void shouldNotKeepCardWhenNoUnitsOnBoard() {
        PlayerState p1 = new PlayerState(makeLeader(Faction.MONSTER), List.of());
        PlayerState p2 = new PlayerState(makeLeader(Faction.NEUTRAL), List.of());
        GameState state = makePlayState(p1, p2);

        engine.execute(state, new PassCommand());
        engine.execute(state, new PassCommand());

        assertTrue(p1.getMeleeRow().getCards().isEmpty());
        assertTrue(p1.getRangedRow().getCards().isEmpty());
        assertTrue(p1.getSiegeRow().getCards().isEmpty());
    }

    // =========================================================
    // Scoia'tael — chooses who goes first
    // =========================================================

    @Test
    void shouldSetPendingAbilityOnCoinFlipWhenScoiatael() {
        PlayerState p1 = new PlayerState(makeLeader(Faction.SCOIATAEL), List.of());
        PlayerState p2 = new PlayerState(makeLeader(Faction.NEUTRAL), List.of());
        GameState state = new GameState(p1, p2);

        engine.resolveCoinFlip(state, Turn.PLAYER_2);

        assertEquals(PendingAbility.SCOIATAEL_FIRST_PLAYER_CHOICE, state.getPendingAbility());
        assertEquals(Turn.PLAYER_1, state.getCurrentTurn());
        assertEquals(GamePhase.COIN_FLIP, state.getPhase());
    }

    @Test
    void shouldSetChosenPlayerAsFirstAfterResolve() {
        PlayerState p1 = new PlayerState(makeLeader(Faction.SCOIATAEL), List.of());
        PlayerState p2 = new PlayerState(makeLeader(Faction.NEUTRAL), List.of());
        GameState state = new GameState(p1, p2);

        engine.resolveCoinFlip(state, Turn.PLAYER_2);
        engine.execute(state, new ResolveScoiataelCommand(Turn.PLAYER_2));

        assertEquals(Turn.PLAYER_2, state.getCurrentTurn());
        assertEquals(GamePhase.REDRAW, state.getPhase());
        assertNull(state.getPendingAbility());
    }

    @Test
    void shouldProceedNormallyWhenBothScoiatael() {
        PlayerState p1 = new PlayerState(makeLeader(Faction.SCOIATAEL), List.of());
        PlayerState p2 = new PlayerState(makeLeader(Faction.SCOIATAEL), List.of());
        GameState state = new GameState(p1, p2);

        engine.resolveCoinFlip(state, Turn.PLAYER_1);

        assertEquals(Turn.PLAYER_1, state.getCurrentTurn());
        assertEquals(GamePhase.REDRAW, state.getPhase());
        assertNull(state.getPendingAbility());
    }

    // =========================================================
    // Skellige — 2 random graveyard units at round 3
    // =========================================================

    @Test
    void shouldPlace2GraveyardUnitsOnBoardAtRound3() {
        Card p1Unit1 = makeUnit("p1u1", "P1Unit1", 10, RowType.MELEE);
        Card p1Unit2 = makeUnit("p1u2", "P1Unit2", 5, RowType.MELEE);
        Card p2Strong = makeUnit("p2s", "P2Strong", 20, RowType.MELEE);
        PlayerState p1 = new PlayerState(makeLeader(Faction.SKELLIGE), List.of());
        p1.addToHand(p1Unit1);
        p1.addToHand(p1Unit2);
        PlayerState p2 = new PlayerState(makeLeader(Faction.NEUTRAL), List.of());
        p2.addToHand(p2Strong);
        GameState state = makePlayState(p1, p2);

        // Round 1: P1 plays both units, wins (15 > 0), P2 loses 1 life
        engine.execute(state, new PlayCardCommand(p1Unit1, RowType.MELEE));
        engine.execute(state, new PassCommand()); // P2 passes, turn→P1
        engine.execute(state, new PlayCardCommand(p1Unit2, RowType.MELEE)); // P1 plays, P2 passed → no switch
        engine.execute(state, new PassCommand()); // P1 passes → resolveRound → P1 wins

        // Round 2: P2 (loser) goes first, plays strong, wins (20 > 0), P1 loses 1 life
        engine.execute(state, new PlayCardCommand(p2Strong, RowType.MELEE)); // P2 plays, turn→P1
        engine.execute(state, new PassCommand()); // P1 passes, turn→P2
        engine.execute(state, new PassCommand()); // P2 passes → resolveRound → P2 wins

        // Round 3: Skellige places 2 units from graveyard
        assertEquals(3, state.getCurrentRound());
        assertEquals(2, p1.getMeleeRow().getCards().size());
        assertTrue(p1.getMeleeRow().getCards().contains(p1Unit1));
        assertTrue(p1.getMeleeRow().getCards().contains(p1Unit2));
        assertTrue(p1.getGraveyard().isEmpty());
    }

    @Test
    void shouldPlaceFewerIfGraveyardHasLessThan2Units() {
        Card p1Unit = makeUnit("p1u", "P1Unit", 10, RowType.MELEE);
        Card p1Hero = new Card("hero", "Hero", Faction.NEUTRAL, CardType.HERO, null, null, RowType.MELEE, 15);
        Card p2Strong = makeUnit("p2s", "P2Strong", 30, RowType.MELEE);
        PlayerState p1 = new PlayerState(makeLeader(Faction.SKELLIGE), List.of());
        p1.addToHand(p1Unit);
        p1.addToHand(p1Hero);
        PlayerState p2 = new PlayerState(makeLeader(Faction.NEUTRAL), List.of());
        p2.addToHand(p2Strong);
        GameState state = makePlayState(p1, p2);

        // Round 1: P1 plays unit + hero, wins (25 > 0)
        engine.execute(state, new PlayCardCommand(p1Unit, RowType.MELEE));
        engine.execute(state, new PassCommand());
        engine.execute(state, new PlayCardCommand(p1Hero, RowType.MELEE));
        engine.execute(state, new PassCommand());

        // Round 2: P2 wins with strong
        engine.execute(state, new PlayCardCommand(p2Strong, RowType.MELEE));
        engine.execute(state, new PassCommand());
        engine.execute(state, new PassCommand());

        // Round 3: graveyard has p1Unit (UNIT) and p1Hero (HERO)
        // Skellige only places UNIT cards, so just p1Unit
        assertEquals(3, state.getCurrentRound());
        assertEquals(1, p1.getMeleeRow().getCards().size());
        assertTrue(p1.getMeleeRow().getCards().contains(p1Unit));
    }

    @Test
    void shouldNotTriggerSkelligeBeforeRound3() {
        Card strong = makeUnit("s1", "Strong", 10, RowType.MELEE);
        PlayerState p1 = new PlayerState(makeLeader(Faction.SKELLIGE), List.of());
        p1.addToHand(strong);
        p1.addToGraveyard(makeUnit("g1", "Grave1", 3, RowType.MELEE));
        p1.addToGraveyard(makeUnit("g2", "Grave2", 4, RowType.MELEE));
        PlayerState p2 = new PlayerState(makeLeader(Faction.NEUTRAL), List.of());
        GameState state = makePlayState(p1, p2);

        // Round 1: P1 wins
        engine.execute(state, new PlayCardCommand(strong, RowType.MELEE));
        engine.execute(state, new PassCommand());
        engine.execute(state, new PassCommand());

        // Round 2: graveyard units should NOT have been placed
        assertEquals(2, state.getCurrentRound());
        assertTrue(p1.getMeleeRow().getCards().isEmpty());
        assertEquals(3, p1.getGraveyard().size()); // strong + 2 originals
    }

    // =========================================================
    // Helpers
    // =========================================================

    private Card makeLeader(Faction faction) {
        return new Card("leader_" + faction.name().toLowerCase(), "Leader", faction, CardType.LEADER,
                null, LeaderAbility.SIEGE_MASTER, null, null);
    }

    private Card makeUnit(String id, String name, int power, RowType rowType) {
        return new Card(id, name, Faction.NEUTRAL, CardType.UNIT, null, null, rowType, power);
    }

    private GameState makePlayState(PlayerState p1, PlayerState p2) {
        GameState state = new GameState(p1, p2);
        state.setCurrentTurn(Turn.PLAYER_1);
        state.setPhase(GamePhase.REDRAW);
        state.setPhase(GamePhase.PLAY);
        return state;
    }
}
