package com.gwent.engine.core;

import com.gwent.engine.domain.*;
import com.gwent.engine.state.BoardRow;
import com.gwent.engine.state.PlayerState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ScoreCalculatorTest {

    private ScoreCalculator calculator;
    private BoardRow meleeRow;

    private Card unit5;
    private Card unit3;
    private Card hero15;
    private Card tightBond4a;
    private Card tightBond4b;
    private Card tightBond4c;
    private Card moraleBoost3;

    @BeforeEach
    void setUp() {
        calculator = new ScoreCalculator();
        meleeRow = new BoardRow(RowType.MELEE);

        unit5 = new Card("unit5", "Soldier", Faction.NEUTRAL, CardType.UNIT,
                null, null, RowType.MELEE, 5);
        unit3 = new Card("unit3", "Scout", Faction.NEUTRAL, CardType.UNIT,
                null, null, RowType.MELEE, 3);
        hero15 = new Card("geralt", "Geralt of Rivia", Faction.NEUTRAL, CardType.HERO,
                null, null, RowType.MELEE, 15);
        tightBond4a = new Card("skirmisher_a", "Dwarven Skirmisher", Faction.NEUTRAL, CardType.UNIT,
                Ability.TIGHT_BOND, null, RowType.MELEE, 4);
        tightBond4b = new Card("skirmisher_b", "Dwarven Skirmisher", Faction.NEUTRAL, CardType.UNIT,
                Ability.TIGHT_BOND, null, RowType.MELEE, 4);
        tightBond4c = new Card("skirmisher_c", "Dwarven Skirmisher", Faction.NEUTRAL, CardType.UNIT,
                Ability.TIGHT_BOND, null, RowType.MELEE, 4);
        moraleBoost3 = new Card("moralecard", "Morale Card", Faction.NEUTRAL, CardType.UNIT,
                Ability.MORALE_BOOST, null, RowType.MELEE, 3);
    }

    // --- Empty / base cases ---

    @Test
    void shouldReturnZeroForEmptyPlayerBoard() {
        Card leader = new Card("foltest", "Foltest", Faction.NORTHERN_REALMS, CardType.LEADER,
                null, LeaderAbility.SIEGE_MASTER, null, null);
        PlayerState player = new PlayerState(leader, List.of());

        assertEquals(0, calculator.calculate(player));
    }

    @Test
    void shouldReturnBasePowerForSingleUnit() {
        meleeRow.addCard(unit5);

        // test via PlayerState to go through calculate(PlayerState)
        Card leader = new Card("foltest", "Foltest", Faction.NORTHERN_REALMS, CardType.LEADER,
                null, LeaderAbility.SIEGE_MASTER, null, null);
        PlayerState player = new PlayerState(leader, List.of());
        player.getMeleeRow().addCard(unit5);

        assertEquals(5, calculator.calculate(player));
    }

    @Test
    void shouldSumAllThreeRowsForPlayerScore() {
        Card rangedUnit = new Card("ranged1", "Archer", Faction.NEUTRAL, CardType.UNIT,
                null, null, RowType.RANGED, 4);
        Card siegeUnit = new Card("siege1", "Catapult", Faction.NEUTRAL, CardType.UNIT,
                null, null, RowType.SIEGE, 6);

        Card leader = new Card("foltest", "Foltest", Faction.NORTHERN_REALMS, CardType.LEADER,
                null, LeaderAbility.SIEGE_MASTER, null, null);
        PlayerState player = new PlayerState(leader, List.of());
        player.getMeleeRow().addCard(unit5);    // 5
        player.getRangedRow().addCard(rangedUnit); // 4
        player.getSiegeRow().addCard(siegeUnit);   // 6

        assertEquals(15, calculator.calculate(player));
    }

    // --- TIGHT_BOND ---

    @Test
    void shouldNotMultiplyTightBondWithOneCopy() {
        // 4 * 1 = 4
        meleeRow.addCard(tightBond4a);

        assertEquals(4, scoreRow(meleeRow));
    }

    @Test
    void shouldDoublePowerForTwoTightBondCopies() {
        // each: 4 * 2 = 8, total = 16
        meleeRow.addCard(tightBond4a);
        meleeRow.addCard(tightBond4b);

        assertEquals(16, scoreRow(meleeRow));
    }

    @Test
    void shouldTriplePowerForThreeTightBondCopies() {
        // each: 4 * 3 = 12, total = 36
        meleeRow.addCard(tightBond4a);
        meleeRow.addCard(tightBond4b);
        meleeRow.addCard(tightBond4c);

        assertEquals(36, scoreRow(meleeRow));
    }

    @Test
    void shouldNotApplyTightBondToDifferentNames() {
        Card otherUnit = new Card("other", "Other Unit", Faction.NEUTRAL, CardType.UNIT,
                Ability.TIGHT_BOND, null, RowType.MELEE, 4);

        meleeRow.addCard(tightBond4a); // count for "Dwarven Skirmisher" = 1 → 4*1=4
        meleeRow.addCard(otherUnit);   // count for "Other Unit" = 1 → 4*1=4

        assertEquals(8, scoreRow(meleeRow));
    }

    // --- Weather ---

    @Test
    void shouldReduceUnitPowerToOneWhenWeatherActive() {
        meleeRow.addCard(unit5);
        meleeRow.setWeatherActive(true);

        assertEquals(1, scoreRow(meleeRow));
    }

    @Test
    void shouldNotAffectHeroWhenWeatherActive() {
        meleeRow.addCard(hero15);
        meleeRow.setWeatherActive(true);

        assertEquals(15, scoreRow(meleeRow));
    }

    @Test
    void shouldOverrideTightBondWithWeather() {
        // tight bond would give 4*2=8 each, but weather resets to 1
        meleeRow.addCard(tightBond4a);
        meleeRow.addCard(tightBond4b);
        meleeRow.setWeatherActive(true);

        assertEquals(2, scoreRow(meleeRow));
    }

    @Test
    void shouldApplyWeatherToUnitsButNotHeroesInSameRow() {
        meleeRow.addCard(unit5);   // weather → 1
        meleeRow.addCard(hero15);  // immune → 15
        meleeRow.setWeatherActive(true);

        assertEquals(16, scoreRow(meleeRow));
    }

    // --- MORALE_BOOST ---

    @Test
    void shouldAddMoraleBoostBonusToOtherCards() {
        // moraleBoost3 = 3 (no self bonus), unit5 = 5+1 = 6 → total = 9
        meleeRow.addCard(unit5);
        meleeRow.addCard(moraleBoost3);

        assertEquals(9, scoreRow(meleeRow));
    }

    @Test
    void shouldNotApplyMoraleBoostToItself() {
        meleeRow.addCard(moraleBoost3);

        assertEquals(3, scoreRow(meleeRow));
    }

    @Test
    void shouldStackMultipleMoraleBoostCards() {
        Card moraleBoost2 = new Card("moralecard2", "Morale Card 2", Faction.NEUTRAL, CardType.UNIT,
                Ability.MORALE_BOOST, null, RowType.MELEE, 2);

        // unit5: 5+2=7 (2 morale boosts)
        // moraleBoost3: 3+1=4 (only the other morale boost counts)
        // moraleBoost2: 2+1=3 (only the other morale boost counts)
        // total = 14
        meleeRow.addCard(unit5);
        meleeRow.addCard(moraleBoost3);
        meleeRow.addCard(moraleBoost2);

        assertEquals(14, scoreRow(meleeRow));
    }

    @Test
    void shouldApplyMoraleBoostAfterWeather() {
        // unit5: weather → 1, then +1 morale = 2
        // moraleBoost3: weather → 1, then no self bonus = 1
        // total = 3
        meleeRow.addCard(unit5);
        meleeRow.addCard(moraleBoost3);
        meleeRow.setWeatherActive(true);

        assertEquals(3, scoreRow(meleeRow));
    }

    // --- COMMANDERS_HORN ---

    @Test
    void shouldDoubleAllUnitPowersWhenHornActive() {
        // unit5: 5*2=10, unit3: 3*2=6 → total = 16
        meleeRow.addCard(unit5);
        meleeRow.addCard(unit3);
        meleeRow.setHornActive(true);

        assertEquals(16, scoreRow(meleeRow));
    }

    @Test
    void shouldApplyMoraleBoostBeforeHorn() {
        // unit5: (5+1)*2=12, moraleBoost3: (3+0)*2=6 → total = 18
        meleeRow.addCard(unit5);
        meleeRow.addCard(moraleBoost3);
        meleeRow.setHornActive(true);

        assertEquals(18, scoreRow(meleeRow));
    }

    @Test
    void shouldApplyWeatherBeforeHorn() {
        // unit5: weather→1, horn: 1*2=2
        meleeRow.addCard(unit5);
        meleeRow.setWeatherActive(true);
        meleeRow.setHornActive(true);

        assertEquals(2, scoreRow(meleeRow));
    }

    @Test
    void shouldApplyTightBondBeforeHorn() {
        // each: 4*2=8 (tight bond), then *2 horn = 16; total = 32
        meleeRow.addCard(tightBond4a);
        meleeRow.addCard(tightBond4b);
        meleeRow.setHornActive(true);

        assertEquals(32, scoreRow(meleeRow));
    }

    // --- Helper ---

    private int scoreRow(BoardRow row) {
        Card leader = new Card("foltest", "Foltest", Faction.NORTHERN_REALMS, CardType.LEADER,
                null, LeaderAbility.SIEGE_MASTER, null, null);
        PlayerState player = new PlayerState(leader, List.of());

        for (Card card : row.getCards()) {
            player.getMeleeRow().addCard(card);
        }
        player.getMeleeRow().setWeatherActive(row.isWeatherActive());
        player.getMeleeRow().setHornActive(row.isHornActive());

        return calculator.calculate(player);
    }
}
