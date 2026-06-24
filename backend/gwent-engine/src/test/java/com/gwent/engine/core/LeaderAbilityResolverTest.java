package com.gwent.engine.core;

import com.gwent.engine.command.UseLeaderCommand;
import com.gwent.engine.domain.*;
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
    // Unimplemented abilities — should not throw
    // =========================================================

    @Test
    void shouldMarkLeaderAsUsedEvenForUnimplementedAbilities() {
        PlayerState p1 = playerWithLeader(LeaderAbility.NORTH_COMMANDER);
        GameState state = makePlayState(p1, playerWithLeader(LeaderAbility.NORTH_COMMANDER));

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
