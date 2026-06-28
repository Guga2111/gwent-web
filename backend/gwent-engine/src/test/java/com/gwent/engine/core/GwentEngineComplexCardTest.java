package com.gwent.engine.core;

import com.gwent.engine.command.*;
import com.gwent.engine.domain.*;
import com.gwent.engine.exception.command.*;
import com.gwent.engine.state.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GwentEngineComplexCardTest {

    private GwentEngine engine;

    @BeforeEach
    void setUp() {
        engine = new GwentEngine();
    }

    // =========================================================
    // AGILE
    // =========================================================

    @Test
    void shouldAllowAgileCardOnMeleeRow() {
        Card agile = makeAgile("a1", "Elf Skirmisher", 5);
        GameState state = makePlayState(playerWithHand(agile), makePlayer());

        assertDoesNotThrow(() -> engine.execute(state, new PlayCardCommand(agile, RowType.MELEE)));
        assertTrue(state.getPlayer1().getMeleeRow().getCards().contains(agile));
    }

    @Test
    void shouldAllowAgileCardOnRangedRow() {
        Card agile = makeAgile("a1", "Elf Skirmisher", 5);
        GameState state = makePlayState(playerWithHand(agile), makePlayer());

        assertDoesNotThrow(() -> engine.execute(state, new PlayCardCommand(agile, RowType.RANGED)));
        assertTrue(state.getPlayer1().getRangedRow().getCards().contains(agile));
    }

    @Test
    void shouldThrowWhenAgileCardPlacedOnSiegeRow() {
        Card agile = makeAgile("a1", "Elf Skirmisher", 5);
        GameState state = makePlayState(playerWithHand(agile), makePlayer());

        assertThrows(InvalidRowException.class, () ->
                engine.execute(state, new PlayCardCommand(agile, RowType.SIEGE)));
    }

    @Test
    void shouldSwitchTurnAfterAgileCardPlayed() {
        Card agile = makeAgile("a1", "Elf Skirmisher", 5);
        GameState state = makePlayState(playerWithHand(agile), makePlayer());

        engine.execute(state, new PlayCardCommand(agile, RowType.RANGED));

        assertEquals(Turn.PLAYER_2, state.getCurrentTurn());
    }

    // =========================================================
    // TIGHT_BOND (E2E score via engine.calculateScore)
    // =========================================================

    @Test
    void shouldDoubleScoreWhenTwoTightBondCopiesPlayedInSameRow() {
        Card bond1 = makeTightBond("b1", "Dwarven Skirmisher", 4);
        Card bond2 = makeTightBond("b2", "Dwarven Skirmisher", 4);
        PlayerState p1 = playerWithHand(bond1, bond2);
        PlayerState p2 = makePlayer();
        GameState state = makePlayState(p1, p2);

        engine.execute(state, new PlayCardCommand(bond1, RowType.MELEE)); // turn → p2
        engine.execute(state, new PassCommand());                          // p2 passes → turn → p1
        engine.execute(state, new PlayCardCommand(bond2, RowType.MELEE)); // p2 passed → no switchTurn

        // 4 * 2 = 8 each → total 16
        assertEquals(16, engine.calculateScore(p1));
    }

    @Test
    void shouldTripleScoreWhenThreeTightBondCopiesPlayedInSameRow() {
        Card bond1 = makeTightBond("b1", "Dwarven Skirmisher", 4);
        Card bond2 = makeTightBond("b2", "Dwarven Skirmisher", 4);
        Card bond3 = makeTightBond("b3", "Dwarven Skirmisher", 4);
        PlayerState p1 = playerWithHand(bond1, bond2, bond3);
        PlayerState p2 = makePlayer();
        GameState state = makePlayState(p1, p2);

        engine.execute(state, new PlayCardCommand(bond1, RowType.MELEE)); // turn → p2
        engine.execute(state, new PassCommand());                          // p2 passes → turn → p1
        engine.execute(state, new PlayCardCommand(bond2, RowType.MELEE)); // p2 passed → no switch
        engine.execute(state, new PlayCardCommand(bond3, RowType.MELEE)); // p2 passed → no switch

        // 4 * 3 = 12 each → total 36
        assertEquals(36, engine.calculateScore(p1));
    }

    @Test
    void shouldNotApplyTightBondBonusToCardsWithDifferentNamesInSameRow() {
        Card bond1 = makeTightBond("b1", "Dwarven Skirmisher", 4);
        Card bond2 = makeTightBond("b2", "Blue Stripes Commando", 4); // different name
        PlayerState p1 = playerWithHand(bond1, bond2);
        PlayerState p2 = makePlayer();
        GameState state = makePlayState(p1, p2);

        engine.execute(state, new PlayCardCommand(bond1, RowType.MELEE));
        engine.execute(state, new PassCommand());
        engine.execute(state, new PlayCardCommand(bond2, RowType.MELEE));

        // each has count=1 → 4*1 + 4*1 = 8
        assertEquals(8, engine.calculateScore(p1));
    }

    // =========================================================
    // MORALE_BOOST (E2E score via engine.calculateScore)
    // =========================================================

    @Test
    void shouldAddOneToPowerOfOtherCardsInRowWhenMoraleBoostPlayed() {
        Card unit = makeUnit("u1", "Soldier", 5, RowType.MELEE);
        Card morale = makeMoraleBoost("m1", "Drummer", 3);
        PlayerState p1 = playerWithHand(unit, morale);
        PlayerState p2 = makePlayer();
        GameState state = makePlayState(p1, p2);

        engine.execute(state, new PlayCardCommand(unit, RowType.MELEE));   // turn → p2
        engine.execute(state, new PassCommand());                           // p2 passes → turn → p1
        engine.execute(state, new PlayCardCommand(morale, RowType.MELEE)); // p2 passed → no switch

        // unit: 5+1=6 (morale bonus), morale: 3+0=3 (no self bonus) → total 9
        assertEquals(9, engine.calculateScore(p1));
    }

    @Test
    void shouldNotApplyMoraleBoostToItselfWhenPlayed() {
        Card morale = makeMoraleBoost("m1", "Drummer", 3);
        GameState state = makePlayState(playerWithHand(morale), makePlayer());

        engine.execute(state, new PlayCardCommand(morale, RowType.MELEE));

        assertEquals(3, engine.calculateScore(state.getPlayer1()));
    }

    // =========================================================
    // COMMANDERS_HORN (E2E through engine)
    // =========================================================

    @Test
    void shouldSetHornActiveOnTargetRowWhenCommandersHornPlayed() {
        Card horn = makeCommandersHorn("h1", "Commander's Horn");
        GameState state = makePlayState(playerWithHand(horn), makePlayer());

        engine.execute(state, new PlayCardCommand(horn, RowType.MELEE));

        assertTrue(state.getPlayer1().getMeleeRow().isHornActive());
        assertFalse(state.getPlayer1().getRangedRow().isHornActive());
        assertFalse(state.getPlayer1().getSiegeRow().isHornActive());
    }

    @Test
    void shouldDoubleRowScoreWhenCommandersHornPlayed() {
        Card unit = makeUnit("u1", "Soldier", 5, RowType.MELEE);
        Card horn = makeCommandersHorn("h1", "Commander's Horn");
        PlayerState p1 = playerWithHand(unit, horn);
        PlayerState p2 = makePlayer();
        GameState state = makePlayState(p1, p2);

        engine.execute(state, new PlayCardCommand(unit, RowType.MELEE)); // turn → p2
        engine.execute(state, new PassCommand());                         // p2 passes → turn → p1
        engine.execute(state, new PlayCardCommand(horn, RowType.MELEE)); // p2 passed → no switch

        // unit: 5*2=10, horn: 0*2=0 → total 10
        assertEquals(10, engine.calculateScore(p1));
    }

    @Test
    void shouldOnlyApplyHornToTargetRowNotOthers() {
        Card meleeUnit = makeUnit("u1", "Soldier", 5, RowType.MELEE);
        Card rangedUnit = makeUnit("u2", "Archer", 4, RowType.RANGED);
        Card horn = makeCommandersHorn("h1", "Commander's Horn");
        PlayerState p1 = playerWithHand(meleeUnit, rangedUnit, horn);
        PlayerState p2 = makePlayer();
        GameState state = makePlayState(p1, p2);

        engine.execute(state, new PlayCardCommand(meleeUnit, RowType.MELEE)); // turn → p2
        engine.execute(state, new PassCommand());                              // p2 passes → p1
        engine.execute(state, new PlayCardCommand(rangedUnit, RowType.RANGED));
        engine.execute(state, new PlayCardCommand(horn, RowType.MELEE));       // horn on MELEE only

        // melee: (5+0)*2=10 (horn), ranged: 4 (no horn) → total 14
        assertEquals(14, engine.calculateScore(p1));
    }

    // =========================================================
    // SCORCH (E2E through engine)
    // =========================================================

    @Test
    void shouldDestroyStrongestNonHeroOnOpponentBoardWhenScorchPlayed() {
        Card strong = makeUnit("s1", "Giant", 10, RowType.MELEE);
        Card weak = makeUnit("w1", "Scout", 3, RowType.RANGED);
        Card scorch = makeScorch("sc1", "Scorch");
        PlayerState p1 = playerWithHand(scorch);
        PlayerState p2 = makePlayer();
        p2.getMeleeRow().addCard(strong);
        p2.getRangedRow().addCard(weak);
        GameState state = makePlayState(p1, p2);

        engine.execute(state, new PlayCardCommand(scorch, RowType.MELEE));

        assertFalse(p2.getMeleeRow().getCards().contains(strong)); // destroyed
        assertTrue(p2.getGraveyard().contains(strong));
        assertTrue(p2.getRangedRow().getCards().contains(weak));   // survives
    }

    @Test
    void shouldNotDestroyHeroWithScorchEvenIfStrongest() {
        Card hero = makeHero("h1", "Geralt", 15, RowType.MELEE);
        Card scorch = makeScorch("sc1", "Scorch");
        PlayerState p1 = playerWithHand(scorch);
        PlayerState p2 = makePlayer();
        p2.getMeleeRow().addCard(hero);
        GameState state = makePlayState(p1, p2);

        engine.execute(state, new PlayCardCommand(scorch, RowType.MELEE));

        assertTrue(p2.getMeleeRow().getCards().contains(hero)); // immune
        assertTrue(p2.getGraveyard().isEmpty());
    }

    @Test
    void shouldDestroyMultipleUnitsWithSameMaxPowerWhenScorchPlayed() {
        Card strong1 = makeUnit("s1", "Giant", 8, RowType.MELEE);
        Card strong2 = makeUnit("s2", "Troll", 8, RowType.RANGED);
        Card scorch = makeScorch("sc1", "Scorch");
        PlayerState p1 = playerWithHand(scorch);
        PlayerState p2 = makePlayer();
        p2.getMeleeRow().addCard(strong1);
        p2.getRangedRow().addCard(strong2);
        GameState state = makePlayState(p1, p2);

        engine.execute(state, new PlayCardCommand(scorch, RowType.MELEE));

        assertTrue(p2.getMeleeRow().getCards().isEmpty());
        assertTrue(p2.getRangedRow().getCards().isEmpty());
        assertEquals(2, p2.getGraveyard().size());
    }

    @Test
    void shouldScorchOwnStrongestUnitWhenOnlyOwnUnitsOnBoard() {
        Card myStrong = makeUnit("m1", "Veteran", 8, RowType.MELEE);
        Card scorch = makeScorch("sc1", "Scorch");
        PlayerState p1 = playerWithHand(scorch);
        p1.getMeleeRow().addCard(myStrong); // already on board before playing scorch
        GameState state = makePlayState(p1, makePlayer());

        engine.execute(state, new PlayCardCommand(scorch, RowType.MELEE));

        assertFalse(p1.getMeleeRow().getCards().contains(myStrong));
        assertTrue(p1.getGraveyard().contains(myStrong));
    }

    @Test
    void shouldDoNothingWhenBoardIsEmptyWhenScorchPlayed() {
        Card scorch = makeScorch("sc1", "Scorch");
        GameState state = makePlayState(playerWithHand(scorch), makePlayer());

        assertDoesNotThrow(() -> engine.execute(state, new PlayCardCommand(scorch, RowType.MELEE)));
    }

    // =========================================================
    // MUSTER (E2E through engine)
    // =========================================================

    @Test
    void shouldPullAllCopiesFromHandAndDeckWhenMusterPlayed() {
        Card trigger  = makeMuster("m1", "Blue Stripes Commando", 4);
        Card fromHand = makeMuster("m2", "Blue Stripes Commando", 4);
        Card fromDeck = makeMuster("m3", "Blue Stripes Commando", 4);
        PlayerState p1 = new PlayerState(makeLeader(), List.of(fromDeck));
        p1.addToHand(trigger);
        p1.addToHand(fromHand);
        GameState state = makePlayState(p1, makePlayer());

        engine.execute(state, new PlayCardCommand(trigger, RowType.MELEE));

        assertEquals(3, p1.getMeleeRow().getCards().size()); // trigger + 2 copies pulled
        assertTrue(p1.getHand().isEmpty());
        assertTrue(p1.getDeck().isEmpty());
    }

    @Test
    void shouldNotPullCardsWithDifferentNameWhenMusterPlayed() {
        Card trigger  = makeMuster("m1", "Blue Stripes Commando", 4);
        Card other    = makeMuster("m2", "Commando Neophyte", 4); // different name
        PlayerState p1 = playerWithHand(trigger, other);
        GameState state = makePlayState(p1, makePlayer());

        engine.execute(state, new PlayCardCommand(trigger, RowType.MELEE));

        assertEquals(1, p1.getMeleeRow().getCards().size()); // only trigger
        assertEquals(1, p1.getHand().size());                // other stays in hand
    }

    @Test
    void shouldDoNothingWhenNoCopiesExistForMuster() {
        Card trigger = makeMuster("m1", "Blue Stripes Commando", 4);
        GameState state = makePlayState(playerWithHand(trigger), makePlayer());

        engine.execute(state, new PlayCardCommand(trigger, RowType.MELEE));

        assertEquals(1, p1Of(state).getMeleeRow().getCards().size()); // only trigger
        assertTrue(p1Of(state).getHand().isEmpty());
    }

    // =========================================================
    // Weather + score integration (E2E)
    // =========================================================

    @Test
    void shouldReduceMeleeScoreToOnePerUnitWhenFrostPlayedViaEngine() {
        Card unit = makeUnit("u1", "Knight", 10, RowType.MELEE);
        Card frost = makeFrost("f1");
        PlayerState p1 = makePlayer();
        p1.getMeleeRow().addCard(unit);
        PlayerState p2 = playerWithHand(frost);
        GameState state = makePlayState(p1, p2);
        state.setCurrentTurn(Turn.PLAYER_2); // p2 goes first

        engine.execute(state, new PlayCardCommand(frost, RowType.MELEE));

        assertTrue(p1.getMeleeRow().isWeatherActive());
        assertTrue(p2.getMeleeRow().isWeatherActive());
        assertEquals(1, engine.calculateScore(p1)); // 10 → 1 due to frost
    }

    @Test
    void shouldNotAffectHeroScoreWhenFrostActive() {
        Card hero = makeHero("h1", "Geralt", 15, RowType.MELEE);
        Card frost = makeFrost("f1");
        PlayerState p1 = makePlayer();
        p1.getMeleeRow().addCard(hero);
        PlayerState p2 = playerWithHand(frost);
        GameState state = makePlayState(p1, p2);
        state.setCurrentTurn(Turn.PLAYER_2);

        engine.execute(state, new PlayCardCommand(frost, RowType.MELEE));

        assertEquals(15, engine.calculateScore(p1)); // hero immune to weather
    }

    @Test
    void shouldClearWeatherBetweenRoundsAndRestoreFullScore() {
        Card unit = makeUnit("u1", "Knight", 10, RowType.MELEE);
        Card frost = makeFrost("f1");
        PlayerState p1 = makePlayer();
        PlayerState p2 = playerWithHand(frost);
        p1.getMeleeRow().addCard(unit);
        GameState state = makePlayState(p1, p2);
        state.setCurrentTurn(Turn.PLAYER_2);

        engine.execute(state, new PlayCardCommand(frost, RowType.MELEE)); // frost active, turn → p1
        engine.execute(state, new PassCommand());                          // p1 passes → turn → p2
        engine.execute(state, new PassCommand());                          // p2 passes → round ends

        // New round started: weather cleared, rows cleared
        assertFalse(state.getPlayer1().getMeleeRow().isWeatherActive());
        assertFalse(state.getPlayer2().getMeleeRow().isWeatherActive());
        assertTrue(state.getBoard().getActiveWeatherCards().isEmpty());
        assertEquals(2, state.getCurrentRound());
    }

    // =========================================================
    // Bug regressions: no switchTurn when opponent already passed
    // =========================================================

    @Test
    void shouldNotSwitchTurnWhenLeaderUsedAfterOpponentPassed() {
        PlayerState p1 = new PlayerState(makeLeaderCard(LeaderAbility.SIEGE_MASTER), List.of());
        PlayerState p2 = makePlayer();
        p2.pass();
        GameState state = makePlayState(p1, p2);

        engine.execute(state, new UseLeaderCommand());

        assertEquals(Turn.PLAYER_1, state.getCurrentTurn()); // no switchTurn
        assertTrue(p1.isLeaderUsed());
    }

    @Test
    void shouldNotSwitchTurnWhenMedicResolvedAfterOpponentPassed() {
        Card medic   = makeUnit("medic", "Medic", 5, RowType.MELEE, Ability.MEDIC);
        Card revived = makeUnit("rev", "Revived Unit", 4, RowType.MELEE);
        PlayerState p1 = playerWithHand(medic);
        p1.addToGraveyard(revived);
        PlayerState p2 = makePlayer();
        p2.pass();
        GameState state = makePlayState(p1, p2);

        engine.execute(state, new PlayCardCommand(medic, RowType.MELEE)); // pending → no switch
        engine.execute(state, new ResolveMedicCommand(revived));           // resolved, p2 passed → no switch

        assertEquals(Turn.PLAYER_1, state.getCurrentTurn());
        assertNull(state.getPendingAbility());
        assertTrue(p1.getMeleeRow().getCards().contains(revived));
    }

    @Test
    void shouldNotSwitchTurnWhenPlayingCardAfterOpponentPassed() {
        Card unit = makeUnit("u1", "Soldier", 5, RowType.MELEE);
        PlayerState p1 = playerWithHand(unit);
        PlayerState p2 = makePlayer();
        p2.pass();
        GameState state = makePlayState(p1, p2);

        engine.execute(state, new PlayCardCommand(unit, RowType.MELEE));

        assertEquals(Turn.PLAYER_1, state.getCurrentTurn()); // confirmed: existing behaviour
    }

    // =========================================================
    // Complex multi-ability round scenario
    // =========================================================

    @Test
    void shouldResolveRoundCorrectlyWithFrostAndCommandersHorn() {
        // Setup: p1 has unit(10) + horn. p2 has unit(10) + frost.
        // Turn order: p1 plays unit(10), p2 plays frost (→ both melee = 1),
        //             p1 plays horn (melee score: 1*2=2 despite frost since order is weather then horn),
        //             p2 passes, p1 passes → resolveRound.
        //
        // After ScoreCalculator: weather first (unit→1), then morale (0), then horn (*2):
        //   p1: unit(10) under frost=1, horn active → 1*2=2  +  horn(0*2)=0  → 2
        //   p2: unit(10) under frost=1                                        → 1
        // p1 score (2) > p2 score (1) → p2 loses a life.

        Card p1Unit = makeUnit("u1", "Knight", 10, RowType.MELEE);
        Card p1Horn = makeCommandersHorn("h1", "Commander's Horn");
        Card p2Unit = makeUnit("u2", "Knight", 10, RowType.MELEE);
        Card p2Frost = makeFrost("f1");

        PlayerState p1 = playerWithHand(p1Unit, p1Horn);
        PlayerState p2 = playerWithHand(p2Unit, p2Frost);
        GameState state = makePlayState(p1, p2);

        engine.execute(state, new PlayCardCommand(p1Unit, RowType.MELEE));   // p1 plays, turn → p2
        engine.execute(state, new PlayCardCommand(p2Frost, RowType.MELEE));  // p2 plays frost, turn → p1
        engine.execute(state, new PlayCardCommand(p1Horn, RowType.MELEE));   // p1 plays horn, turn → p2
        engine.execute(state, new PlayCardCommand(p2Unit, RowType.MELEE));   // p2 plays unit, turn → p1
        engine.execute(state, new PassCommand());                             // p1 passes, turn → p2
        engine.execute(state, new PassCommand());                             // p2 passes → resolveRound

        // p1: unit under frost=1, horn→1*2=2; p2: unit under frost=1
        // p1 wins round → p2 loses life
        assertEquals(1, p2.getLives());
        assertEquals(2, p1.getLives());
        assertEquals(2, state.getCurrentRound());
    }

    @Test
    void shouldResolveMusterThenContinueTurn() {
        // p1 plays muster card → all copies pulled, then turn switches (p2 not passed)
        Card trigger  = makeMuster("m1", "Blue Stripes Commando", 4);
        Card fromHand = makeMuster("m2", "Blue Stripes Commando", 4);
        PlayerState p1 = playerWithHand(trigger, fromHand);
        GameState state = makePlayState(p1, makePlayer());

        engine.execute(state, new PlayCardCommand(trigger, RowType.MELEE));

        // muster is not a pending ability — turn should have switched
        assertEquals(Turn.PLAYER_2, state.getCurrentTurn());
        assertEquals(2, p1.getMeleeRow().getCards().size()); // trigger + copy
        assertTrue(p1.getHand().isEmpty());
    }

    // =========================================================
    // Helpers
    // =========================================================

    private Card makeLeader() {
        return new Card("foltest", "Foltest", Faction.NORTHERN_REALMS, CardType.LEADER,
                null, LeaderAbility.SIEGE_MASTER, null, null);
    }

    private Card makeLeaderCard(LeaderAbility ability) {
        return new Card("leader_x", "Leader", Faction.NEUTRAL, CardType.LEADER,
                null, ability, null, null);
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

    private Card makeAgile(String id, String name, int power) {
        return new Card(id, name, Faction.NEUTRAL, CardType.UNIT, Ability.AGILE, null, RowType.MELEE, power);
    }

    private Card makeTightBond(String id, String name, int power) {
        return new Card(id, name, Faction.NEUTRAL, CardType.UNIT, Ability.TIGHT_BOND, null, RowType.MELEE, power);
    }

    private Card makeMoraleBoost(String id, String name, int power) {
        return new Card(id, name, Faction.NEUTRAL, CardType.UNIT, Ability.MORALE_BOOST, null, RowType.MELEE, power);
    }

    private Card makeCommandersHorn(String id, String name) {
        // Treated as a UNIT with 0 power so ScoreCalculator handles it without NPE
        return new Card(id, name, Faction.NEUTRAL, CardType.UNIT, Ability.COMMANDERS_HORN, null, RowType.MELEE, 0);
    }

    private Card makeScorch(String id, String name) {
        return new Card(id, name, Faction.NEUTRAL, CardType.UNIT, Ability.SCORCH, null, RowType.MELEE, 0);
    }

    private Card makeMuster(String id, String name, int power) {
        return new Card(id, name, Faction.NEUTRAL, CardType.UNIT, Ability.MUSTER, null, RowType.MELEE, power);
    }

    private Card makeFrost(String id) {
        return new Card(id, "Biting Frost", Faction.NEUTRAL, CardType.WEATHER, Ability.FROST, null, null, null);
    }

    private PlayerState makePlayer() {
        return new PlayerState(makeLeader(), List.of());
    }

    private PlayerState playerWithHand(Card... cards) {
        PlayerState player = new PlayerState(makeLeader(), List.of());
        for (Card card : cards) player.addToHand(card);
        return player;
    }

    private GameState makePlayState(PlayerState p1, PlayerState p2) {
        GameState state = new GameState(p1, p2);
        state.setCurrentTurn(Turn.PLAYER_1);
        state.setPhase(GamePhase.REDRAW);
        state.setPhase(GamePhase.PLAY);
        return state;
    }

    private PlayerState p1Of(GameState state) {
        return state.getPlayer1();
    }
}
