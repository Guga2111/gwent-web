package com.gwent.engine.state;

import com.gwent.engine.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BoardRowTest {

    private BoardRow meleeRow;
    private Card meleeUnit;

    @BeforeEach
    void setUp() {
        meleeRow = new BoardRow(RowType.MELEE);
        meleeUnit = new Card("geralt", "Geralt of Rivia", Faction.NEUTRAL, CardType.HERO,
                null, null, RowType.MELEE, 15);
    }

    @Test
    void shouldStartEmpty() {
        assertTrue(meleeRow.getCards().isEmpty());
        assertFalse(meleeRow.isHornActive());
        assertFalse(meleeRow.isWeatherActive());
    }

    @Test
    void shouldAddCardToRow() {
        meleeRow.addCard(meleeUnit);

        assertEquals(1, meleeRow.getCards().size());
        assertEquals(meleeUnit, meleeRow.getCards().get(0));
    }

    @Test
    void shouldThrowWhenCardRowDoesNotMatch() {
        Card rangedUnit = new Card("archer", "Archer", Faction.NORTHERN_REALMS, CardType.UNIT,
                null, null, RowType.RANGED, 5);

        assertThrows(IllegalStateException.class, () -> meleeRow.addCard(rangedUnit));
    }

    @Test
    void shouldRemoveCard() {
        meleeRow.addCard(meleeUnit);
        meleeRow.removeCard(meleeUnit);

        assertTrue(meleeRow.getCards().isEmpty());
    }

    @Test
    void shouldClearAllCards() {
        Card anotherMelee = new Card("vesemir", "Vesemir", Faction.NEUTRAL, CardType.UNIT,
                null, null, RowType.MELEE, 6);

        meleeRow.addCard(meleeUnit);
        meleeRow.addCard(anotherMelee);
        meleeRow.clear();

        assertTrue(meleeRow.getCards().isEmpty());
    }

    @Test
    void shouldToggleHornActive() {
        meleeRow.setHornActive(true);
        assertTrue(meleeRow.isHornActive());

        meleeRow.setHornActive(false);
        assertFalse(meleeRow.isHornActive());
    }

    @Test
    void shouldToggleWeatherActive() {
        meleeRow.setWeatherActive(true);
        assertTrue(meleeRow.isWeatherActive());

        meleeRow.setWeatherActive(false);
        assertFalse(meleeRow.isWeatherActive());
    }

    @Test
    void shouldReturnUnmodifiableList() {
        meleeRow.addCard(meleeUnit);

        assertThrows(UnsupportedOperationException.class, () ->
                meleeRow.getCards().add(meleeUnit));
    }

    @Test
    void shouldDoNothingWhenRemovingCardNotInRow() {
        meleeRow.addCard(meleeUnit);
        Card other = new Card("vesemir", "Vesemir", Faction.NEUTRAL, CardType.UNIT,
                null, null, RowType.MELEE, 6);

        meleeRow.removeCard(other);

        assertEquals(1, meleeRow.getCards().size());
    }

    @Test
    void shouldReturnCorrectRowType() {
        assertEquals(RowType.MELEE, meleeRow.getRowType());
    }
}