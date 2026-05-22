package com.gwent.engine.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CardTest {

    @Test
    void shouldCreateValidUnitCard() {
        Card card = new Card("geralt", "Geralt of Rivia", Faction.NEUTRAL, CardType.UNIT,
                Ability.MORALE_BOOST, null, RowType.MELEE, 10);

        assertEquals("GERALT", card.id());
        assertEquals("Geralt of Rivia", card.name());
        assertEquals(10, card.basePower());
    }

    @Test
    void shouldCreateValidHeroCard() {
        Card card = new Card("ciri", "Ciri", Faction.NEUTRAL, CardType.HERO,
                null, null, RowType.MELEE, 15);

        assertEquals("CIRI", card.id());
        assertEquals(15, card.basePower());
    }

    @Test
    void shouldCreateValidLeaderCard() {
        Card card = new Card("foltest", "Foltest", Faction.NORTHERN_REALMS, CardType.LEADER,
                null, LeaderAbility.SIEGE_MASTER, null, null);

        assertEquals("FOLTEST", card.id());
        assertNull(card.rowType());
        assertNull(card.basePower());
    }

    @Test
    void shouldCreateValidSpecialCard() {
        Card card = new Card("decoy", "Decoy", Faction.NEUTRAL, CardType.SPECIAL,
                null, null, null, null);

        assertEquals("DECOY", card.id());
    }

    @Test
    void shouldCreateValidWeatherCard() {
        Card card = new Card("biting frost", "Biting Frost", Faction.NEUTRAL, CardType.WEATHER,
                null, null, null, null);

        assertEquals("BITING_FROST", card.id());
    }

    @Test
    void shouldNormalizeIdWithSpacesAndCasing() {
        Card card = new Card("  geralt of rivia  ", "Geralt", Faction.NEUTRAL, CardType.UNIT,
                null, null, RowType.MELEE, 10);

        assertEquals("GERALT_OF_RIVIA", card.id());
    }

    @Test
    void shouldThrowWhenIdIsNull() {
        assertThrows(NullPointerException.class, () ->
                new Card(null, "Geralt", Faction.NEUTRAL, CardType.UNIT,
                        null, null, RowType.MELEE, 10));
    }

    @Test
    void shouldThrowWhenNameIsNull() {
        assertThrows(NullPointerException.class, () ->
                new Card("geralt", null, Faction.NEUTRAL, CardType.UNIT,
                        null, null, RowType.MELEE, 10));
    }

    @Test
    void shouldThrowWhenLeaderHasNoLeaderAbility() {
        assertThrows(IllegalArgumentException.class, () ->
                new Card("foltest", "Foltest", Faction.NORTHERN_REALMS, CardType.LEADER,
                        null, null, null, null));
    }

    @Test
    void shouldThrowWhenLeaderHasUnitAbility() {
        assertThrows(IllegalArgumentException.class, () ->
                new Card("foltest", "Foltest", Faction.NORTHERN_REALMS, CardType.LEADER,
                        Ability.SPY, LeaderAbility.SIEGE_MASTER, null, null));
    }

    @Test
    void shouldThrowWhenNonLeaderHasLeaderAbility() {
        assertThrows(IllegalArgumentException.class, () ->
                new Card("geralt", "Geralt", Faction.NEUTRAL, CardType.UNIT,
                        null, LeaderAbility.SIEGE_MASTER, RowType.MELEE, 10));
    }

    @Test
    void shouldThrowWhenUnitHasNoRowType() {
        assertThrows(IllegalArgumentException.class, () ->
                new Card("geralt", "Geralt", Faction.NEUTRAL, CardType.UNIT,
                        null, null, null, 10));
    }

    @Test
    void shouldThrowWhenUnitHasNoBasePower() {
        assertThrows(IllegalArgumentException.class, () ->
                new Card("geralt", "Geralt", Faction.NEUTRAL, CardType.UNIT,
                        null, null, RowType.MELEE, null));
    }

    @Test
    void shouldThrowWhenHeroHasNoRowType() {
        assertThrows(IllegalArgumentException.class, () ->
                new Card("ciri", "Ciri", Faction.NEUTRAL, CardType.HERO,
                        null, null, null, 15));
    }

    @Test
    void shouldThrowWhenSpecialHasRowType() {
        assertThrows(IllegalArgumentException.class, () ->
                new Card("decoy", "Decoy", Faction.NEUTRAL, CardType.SPECIAL,
                        null, null, RowType.MELEE, null));
    }

    @Test
    void shouldThrowWhenWeatherHasRowType() {
        assertThrows(IllegalArgumentException.class, () ->
                new Card("frost", "Biting Frost", Faction.NEUTRAL, CardType.WEATHER,
                        null, null, RowType.MELEE, null));
    }
}
