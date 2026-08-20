package com.gwent.engine.core;

import com.gwent.engine.domain.*;
import com.gwent.engine.state.GameState;
import com.gwent.engine.state.PlayerState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AbilityResolverTest {

    private AbilityResolver resolver;
    private GameState state;
    private PlayerState player1;
    private PlayerState player2;

    @BeforeEach
    void setUp() {
        resolver = new AbilityResolver(new ScoreCalculator());
        player1 = new PlayerState(makeLeader(), List.of());
        player2 = new PlayerState(makeLeader(), List.of());
        state = makeState(player1, player2);
    }

    // --- SPY ---

    @Test
    void shouldDrawTwoCardsWhenSpyIsPlayed() {
        PlayerState p1 = new PlayerState(makeLeader(), List.of(
                makeUnit("u1", "Soldier 1", 3, RowType.MELEE),
                makeUnit("u2", "Soldier 2", 3, RowType.MELEE),
                makeUnit("u3", "Soldier 3", 3, RowType.MELEE)
        ));
        GameState gs = makeState(p1, new PlayerState(makeLeader(), List.of()));

        resolver.resolve(gs, makeUnit("spy", "Spy", 3, RowType.MELEE, Ability.SPY), RowType.MELEE);

        assertEquals(2, p1.getHand().size());
    }

    @Test
    void shouldDrawOneCardWhenDeckHasOnlyOne() {
        PlayerState p1 = new PlayerState(makeLeader(), List.of(
                makeUnit("u1", "Soldier 1", 3, RowType.MELEE)
        ));
        GameState gs = makeState(p1, new PlayerState(makeLeader(), List.of()));

        resolver.resolve(gs, makeUnit("spy", "Spy", 3, RowType.MELEE, Ability.SPY), RowType.MELEE);

        assertEquals(1, p1.getHand().size());
    }

    @Test
    void shouldDrawNothingWhenDeckIsEmpty() {
        PlayerState p1 = new PlayerState(makeLeader(), List.of());
        GameState gs = makeState(p1, new PlayerState(makeLeader(), List.of()));

        resolver.resolve(gs, makeUnit("spy", "Spy", 3, RowType.MELEE, Ability.SPY), RowType.MELEE);

        assertTrue(p1.getHand().isEmpty());
    }

    // --- MEDIC ---

    @Test
    void shouldSetPendingAbilityWhenMedicIsPlayed() {
        resolver.resolve(state, makeUnit("medic", "Medic", 5, RowType.MELEE, Ability.MEDIC), RowType.MELEE);

        assertEquals(PendingAbility.MEDIC_CHOICE, state.getPendingAbility());
    }

    // --- MUSTER ---

    @Test
    void shouldPullCopiesFromHandWhenMusterIsPlayed() {
        Card copy1 = makeUnit("blue_b", "Blue Stripes Commando", 4, RowType.MELEE, Ability.MUSTER);
        Card copy2 = makeUnit("blue_c", "Blue Stripes Commando", 4, RowType.MELEE, Ability.MUSTER);
        PlayerState p1 = new PlayerState(makeLeader(), List.of());
        p1.addToHand(copy1);
        p1.addToHand(copy2);
        GameState gs = makeState(p1, new PlayerState(makeLeader(), List.of()));

        resolver.resolve(gs, makeUnit("blue_a", "Blue Stripes Commando", 4, RowType.MELEE, Ability.MUSTER), RowType.MELEE);

        assertTrue(p1.getHand().isEmpty());
        assertEquals(2, p1.getMeleeRow().getCards().size());
    }

    @Test
    void shouldPullCopiesFromDeckWhenMusterIsPlayed() {
        Card copy1 = makeUnit("blue_b", "Blue Stripes Commando", 4, RowType.MELEE, Ability.MUSTER);
        Card copy2 = makeUnit("blue_c", "Blue Stripes Commando", 4, RowType.MELEE, Ability.MUSTER);
        PlayerState p1 = new PlayerState(makeLeader(), List.of(copy1, copy2));
        GameState gs = makeState(p1, new PlayerState(makeLeader(), List.of()));

        resolver.resolve(gs, makeUnit("blue_a", "Blue Stripes Commando", 4, RowType.MELEE, Ability.MUSTER), RowType.MELEE);

        assertTrue(p1.getDeck().isEmpty());
        assertEquals(2, p1.getMeleeRow().getCards().size());
    }

    @Test
    void shouldPullCopiesFromBothHandAndDeckWhenMusterIsPlayed() {
        Card fromHand = makeUnit("blue_b", "Blue Stripes Commando", 4, RowType.MELEE, Ability.MUSTER);
        Card fromDeck = makeUnit("blue_c", "Blue Stripes Commando", 4, RowType.MELEE, Ability.MUSTER);
        PlayerState p1 = new PlayerState(makeLeader(), List.of(fromDeck));
        p1.addToHand(fromHand);
        GameState gs = makeState(p1, new PlayerState(makeLeader(), List.of()));

        resolver.resolve(gs, makeUnit("blue_a", "Blue Stripes Commando", 4, RowType.MELEE, Ability.MUSTER), RowType.MELEE);

        assertTrue(p1.getHand().isEmpty());
        assertTrue(p1.getDeck().isEmpty());
        assertEquals(2, p1.getMeleeRow().getCards().size());
    }

    @Test
    void shouldDoNothingWhenNoCopiesExistForMuster() {
        PlayerState p1 = new PlayerState(makeLeader(), List.of());
        GameState gs = makeState(p1, new PlayerState(makeLeader(), List.of()));

        resolver.resolve(gs, makeUnit("blue_a", "Blue Stripes Commando", 4, RowType.MELEE, Ability.MUSTER), RowType.MELEE);

        assertTrue(p1.getHand().isEmpty());
        assertTrue(p1.getDeck().isEmpty());
        assertTrue(p1.getMeleeRow().getCards().isEmpty());
    }

    @Test
    void shouldNotPullCardsWithDifferentNameForMuster() {
        Card different = makeUnit("other", "Other Card", 4, RowType.MELEE, Ability.MUSTER);
        PlayerState p1 = new PlayerState(makeLeader(), List.of());
        p1.addToHand(different);
        GameState gs = makeState(p1, new PlayerState(makeLeader(), List.of()));

        resolver.resolve(gs, makeUnit("blue_a", "Blue Stripes Commando", 4, RowType.MELEE, Ability.MUSTER), RowType.MELEE);

        assertEquals(1, p1.getHand().size());
        assertTrue(p1.getMeleeRow().getCards().isEmpty());
    }

    // --- SCORCH ---

    @Test
    void shouldDestroyStrongestUnit() {
        Card strong = makeUnit("strong", "Strong Unit", 10, RowType.MELEE);
        Card weak = makeUnit("weak", "Weak Unit", 3, RowType.MELEE);
        PlayerState p1 = new PlayerState(makeLeader(), List.of());
        p1.getMeleeRow().addCard(strong);
        p1.getMeleeRow().addCard(weak);
        GameState gs = makeState(p1, new PlayerState(makeLeader(), List.of()));

        resolver.resolve(gs, makeUnit("scorch_card", "Scorch", 2, RowType.MELEE, Ability.SCORCH), RowType.MELEE);

        assertFalse(p1.getMeleeRow().getCards().contains(strong));
        assertTrue(p1.getMeleeRow().getCards().contains(weak));
        assertEquals(1, p1.getGraveyard().size());
    }

    @Test
    void shouldDestroyMultipleUnitsWithSameMaxPower() {
        PlayerState p1 = new PlayerState(makeLeader(), List.of());
        p1.getMeleeRow().addCard(makeUnit("u1", "Unit 1", 8, RowType.MELEE));
        p1.getMeleeRow().addCard(makeUnit("u2", "Unit 2", 8, RowType.MELEE));
        p1.getMeleeRow().addCard(makeUnit("weak", "Weak", 3, RowType.MELEE));
        GameState gs = makeState(p1, new PlayerState(makeLeader(), List.of()));

        resolver.resolve(gs, makeUnit("scorch_card", "Scorch", 2, RowType.MELEE, Ability.SCORCH), RowType.MELEE);

        assertEquals(1, p1.getMeleeRow().getCards().size());
        assertEquals(2, p1.getGraveyard().size());
    }

    @Test
    void shouldNotDestroyHeroesWithScorch() {
        Card hero = makeHero("geralt", "Geralt", 15, RowType.MELEE);
        Card weak = makeUnit("weak", "Weak", 3, RowType.MELEE);
        PlayerState p1 = new PlayerState(makeLeader(), List.of());
        p1.getMeleeRow().addCard(hero);
        p1.getMeleeRow().addCard(weak);
        GameState gs = makeState(p1, new PlayerState(makeLeader(), List.of()));

        resolver.resolve(gs, makeUnit("scorch_card", "Scorch", 2, RowType.MELEE, Ability.SCORCH), RowType.MELEE);

        assertTrue(p1.getMeleeRow().getCards().contains(hero));
        assertFalse(p1.getMeleeRow().getCards().contains(weak));
    }

    @Test
    void shouldScorchStrongestUnitFromOpponentSide() {
        Card opponentStrong = makeUnit("op_strong", "Opponent Strong", 10, RowType.MELEE);
        Card myWeak = makeUnit("my_weak", "My Weak", 3, RowType.MELEE);
        PlayerState p1 = new PlayerState(makeLeader(), List.of());
        p1.getMeleeRow().addCard(myWeak);
        PlayerState p2 = new PlayerState(makeLeader(), List.of());
        p2.getMeleeRow().addCard(opponentStrong);
        GameState gs = makeState(p1, p2);

        resolver.resolve(gs, makeUnit("scorch_card", "Scorch", 2, RowType.MELEE, Ability.SCORCH), RowType.MELEE);

        assertTrue(p2.getMeleeRow().getCards().isEmpty());
        assertEquals(1, p2.getGraveyard().size());
        assertFalse(p1.getMeleeRow().getCards().isEmpty());
    }

    @Test
    void shouldScorchAcrossAllRows() {
        Card meleeUnit = makeUnit("m1", "Melee", 5, RowType.MELEE);
        Card rangedUnit = makeUnit("r1", "Ranged", 10, RowType.RANGED);
        Card siegeUnit = makeUnit("s1", "Siege", 3, RowType.SIEGE);
        PlayerState p1 = new PlayerState(makeLeader(), List.of());
        p1.getMeleeRow().addCard(meleeUnit);
        p1.getRangedRow().addCard(rangedUnit);
        p1.getSiegeRow().addCard(siegeUnit);
        GameState gs = makeState(p1, new PlayerState(makeLeader(), List.of()));

        resolver.resolve(gs, makeUnit("scorch_card", "Scorch", 2, RowType.MELEE, Ability.SCORCH), RowType.MELEE);

        assertFalse(p1.getMeleeRow().getCards().isEmpty());
        assertTrue(p1.getRangedRow().getCards().isEmpty()); // strongest destroyed
        assertFalse(p1.getSiegeRow().getCards().isEmpty());
    }

    @Test
    void shouldDoNothingWhenBoardIsEmptyForScorch() {
        GameState gs = makeState(
                new PlayerState(makeLeader(), List.of()),
                new PlayerState(makeLeader(), List.of())
        );

        assertDoesNotThrow(() ->
                resolver.resolve(gs, makeUnit("scorch_card", "Scorch", 2, RowType.MELEE, Ability.SCORCH), RowType.MELEE));
    }

    @Test
    void shouldDoNothingWhenOnlyHeroesOnBoardForScorch() {
        Card hero = makeHero("geralt", "Geralt", 15, RowType.MELEE);
        PlayerState p1 = new PlayerState(makeLeader(), List.of());
        p1.getMeleeRow().addCard(hero);
        GameState gs = makeState(p1, new PlayerState(makeLeader(), List.of()));

        resolver.resolve(gs, makeUnit("scorch_card", "Scorch", 2, RowType.MELEE, Ability.SCORCH), RowType.MELEE);

        assertTrue(p1.getMeleeRow().getCards().contains(hero));
        assertTrue(p1.getGraveyard().isEmpty());
    }

    // --- COMMANDERS_HORN ---

    @Test
    void shouldActivateHornOnTargetRow() {
        resolver.resolve(state, makeUnit("horn", "Horn Card", 0, RowType.MELEE, Ability.COMMANDERS_HORN), RowType.MELEE);

        assertTrue(player1.getMeleeRow().isHornActive());
        assertFalse(player1.getRangedRow().isHornActive());
        assertFalse(player1.getSiegeRow().isHornActive());
    }

    @Test
    void shouldActivateHornOnlyOnChosenRow() {
        resolver.resolve(state, makeUnit("horn", "Horn Card", 0, RowType.RANGED, Ability.COMMANDERS_HORN), RowType.RANGED);

        assertFalse(player1.getMeleeRow().isHornActive());
        assertTrue(player1.getRangedRow().isHornActive());
        assertFalse(player1.getSiegeRow().isHornActive());
    }

    // --- No-op abilities ---

    @Test
    void shouldDoNothingForNullAbility() {
        assertDoesNotThrow(() -> resolver.resolve(state, makeUnit("plain", "Plain", 5, RowType.MELEE), RowType.MELEE));
        assertNull(state.getPendingAbility());
    }

    @Test
    void shouldDoNothingForTightBond() {
        assertDoesNotThrow(() -> resolver.resolve(state, makeUnit("bond", "Bond", 4, RowType.MELEE, Ability.TIGHT_BOND), RowType.MELEE));
        assertNull(state.getPendingAbility());
    }

    @Test
    void shouldDoNothingForMoraleBoost() {
        assertDoesNotThrow(() -> resolver.resolve(state, makeUnit("morale", "Morale", 3, RowType.MELEE, Ability.MORALE_BOOST), RowType.MELEE));
        assertNull(state.getPendingAbility());
    }

    // --- Helpers ---

    private Card makeLeader() {
        return new Card("foltest", "Foltest", Faction.NORTHERN_REALMS, CardType.LEADER,
                null, LeaderAbility.SIEGE_MASTER, null, null);
    }

    private Card makeUnit(String id, String name, int power, RowType rowType) {
        return new Card(id, name, Faction.NEUTRAL, CardType.UNIT, null, null, rowType, power);
    }

    private Card makeUnit(String id, String name, int power, RowType rowType, Ability ability) {
        return new Card(id, name, Faction.NEUTRAL, CardType.UNIT, ability, null, rowType, power);
    }

    private Card makeHero(String id, String name, int power, RowType rowType) {
        return new Card(id, name, Faction.NEUTRAL, CardType.HERO, null, null, rowType, power);
    }

    private GameState makeState(PlayerState p1, PlayerState p2) {
        GameState gs = new GameState(p1, p2);
        gs.setCurrentTurn(Turn.PLAYER_1);
        return gs;
    }
}