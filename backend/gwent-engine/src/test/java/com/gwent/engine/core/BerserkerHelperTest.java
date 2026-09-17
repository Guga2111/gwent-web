package com.gwent.engine.core;

import com.gwent.engine.domain.*;
import com.gwent.engine.state.BoardRow;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BerserkerHelperTest {

    // --- transformBerserkers ---

    @Test
    void shouldTransformVildkaarlIntoBearOnRow() {
        BoardRow row = new BoardRow(RowType.MELEE);
        row.addCard(new Card("vildkaarl_1", "Vildkaarl", Faction.SKELLIGE, CardType.UNIT,
                Ability.BERSERKER, null, RowType.MELEE, 8));

        BerserkerHelper.transformBerserkers(row);

        List<Card> cards = row.getCards();
        assertEquals(1, cards.size());
        Card bear = cards.get(0);
        assertEquals("Transformed Vildkaarl", bear.name());
        assertEquals(Ability.MORALE_BOOST, bear.ability());
        assertEquals(14, bear.basePower());
    }

    @Test
    void shouldTransformBerserkerMarauderOnRow() {
        BoardRow row = new BoardRow(RowType.MELEE);
        row.addCard(new Card("marauder_1", "Berserker Marauder", Faction.SKELLIGE, CardType.UNIT,
                Ability.BERSERKER, null, RowType.MELEE, 4));

        BerserkerHelper.transformBerserkers(row);

        List<Card> cards = row.getCards();
        assertEquals(1, cards.size());
        Card transformed = cards.get(0);
        assertEquals("Transformed Marauder", transformed.name());
        assertNull(transformed.ability());
        assertEquals(8, transformed.basePower());
    }

    @Test
    void shouldTransformMultipleBerserkersOnSameRow() {
        BoardRow row = new BoardRow(RowType.MELEE);
        row.addCard(new Card("vildkaarl_1", "Vildkaarl", Faction.SKELLIGE, CardType.UNIT,
                Ability.BERSERKER, null, RowType.MELEE, 8));
        row.addCard(new Card("marauder_1", "Berserker Marauder", Faction.SKELLIGE, CardType.UNIT,
                Ability.BERSERKER, null, RowType.MELEE, 4));

        BerserkerHelper.transformBerserkers(row);

        List<Card> cards = row.getCards();
        assertEquals(2, cards.size());
        assertTrue(cards.stream().anyMatch(c -> c.name().equals("Transformed Vildkaarl")));
        assertTrue(cards.stream().anyMatch(c -> c.name().equals("Transformed Marauder")));
    }

    @Test
    void shouldNotTransformNonBerserkerCards() {
        BoardRow row = new BoardRow(RowType.MELEE);
        Card regular = new Card("soldier", "Soldier", Faction.NEUTRAL, CardType.UNIT,
                null, null, RowType.MELEE, 5);
        row.addCard(regular);

        BerserkerHelper.transformBerserkers(row);

        assertEquals(1, row.getCards().size());
        assertEquals(regular, row.getCards().get(0));
    }

    @Test
    void shouldDoNothingOnEmptyRow() {
        BoardRow row = new BoardRow(RowType.MELEE);

        assertDoesNotThrow(() -> BerserkerHelper.transformBerserkers(row));
        assertTrue(row.getCards().isEmpty());
    }

    @Test
    void shouldAppendTransformedSuffixToId() {
        BoardRow row = new BoardRow(RowType.MELEE);
        row.addCard(new Card("vildkaarl_1", "Vildkaarl", Faction.SKELLIGE, CardType.UNIT,
                Ability.BERSERKER, null, RowType.MELEE, 8));

        BerserkerHelper.transformBerserkers(row);

        assertTrue(row.getCards().get(0).id().endsWith("_TRANSFORMED"));
    }

    @Test
    void shouldPreserveRowTypeFromOriginal() {
        BoardRow row = new BoardRow(RowType.SIEGE);
        row.addCard(new Card("vildkaarl_1", "Vildkaarl", Faction.SKELLIGE, CardType.UNIT,
                Ability.BERSERKER, null, RowType.SIEGE, 8));

        BerserkerHelper.transformBerserkers(row);

        assertEquals(RowType.SIEGE, row.getCards().get(0).rowType());
    }

    // --- revertIfTransformed ---

    @Test
    void shouldRevertTransformedVildkaarlToOriginal() {
        Card bear = new Card("VILDKAARL_1_TRANSFORMED", "Transformed Vildkaarl",
                Faction.SKELLIGE, CardType.UNIT, Ability.MORALE_BOOST, null, RowType.MELEE, 14);

        Card reverted = BerserkerHelper.revertIfTransformed(bear);

        assertEquals("VILDKAARL_1", reverted.id());
        assertEquals("Vildkaarl", reverted.name());
        assertEquals(Ability.BERSERKER, reverted.ability());
        assertEquals(8, reverted.basePower());
    }

    @Test
    void shouldRevertTransformedMarauderToOriginal() {
        Card transformed = new Card("MARAUDER_1_TRANSFORMED", "Transformed Marauder",
                Faction.SKELLIGE, CardType.UNIT, null, null, RowType.MELEE, 8);

        Card reverted = BerserkerHelper.revertIfTransformed(transformed);

        assertEquals("MARAUDER_1", reverted.id());
        assertEquals("Berserker Marauder", reverted.name());
        assertEquals(Ability.BERSERKER, reverted.ability());
        assertEquals(4, reverted.basePower());
    }

    @Test
    void shouldPreserveRowTypeOnRevert() {
        Card bear = new Card("VILDKAARL_1_TRANSFORMED", "Transformed Vildkaarl",
                Faction.SKELLIGE, CardType.UNIT, Ability.MORALE_BOOST, null, RowType.SIEGE, 14);

        Card reverted = BerserkerHelper.revertIfTransformed(bear);

        assertEquals(RowType.SIEGE, reverted.rowType());
    }

    @Test
    void shouldNotRevertCardWithoutTransformedSuffix() {
        Card regular = new Card("soldier", "Soldier", Faction.NEUTRAL, CardType.UNIT,
                null, null, RowType.MELEE, 5);

        Card result = BerserkerHelper.revertIfTransformed(regular);

        assertSame(regular, result);
    }

    @Test
    void shouldNotRevertCardWithSuffixButUnknownName() {
        Card fake = new Card("FAKE_TRANSFORMED", "Unknown Bear",
                Faction.SKELLIGE, CardType.UNIT, null, null, RowType.MELEE, 10);

        Card result = BerserkerHelper.revertIfTransformed(fake);

        assertSame(fake, result);
    }

    @Test
    void shouldRevertMultipleBearsIndependently() {
        Card bear1 = new Card("VILDKAARL_1_TRANSFORMED", "Transformed Vildkaarl",
                Faction.SKELLIGE, CardType.UNIT, Ability.MORALE_BOOST, null, RowType.MELEE, 14);
        Card bear2 = new Card("VILDKAARL_2_TRANSFORMED", "Transformed Vildkaarl",
                Faction.SKELLIGE, CardType.UNIT, Ability.MORALE_BOOST, null, RowType.MELEE, 14);

        Card reverted1 = BerserkerHelper.revertIfTransformed(bear1);
        Card reverted2 = BerserkerHelper.revertIfTransformed(bear2);

        assertEquals("VILDKAARL_1", reverted1.id());
        assertEquals("VILDKAARL_2", reverted2.id());
        assertEquals(Ability.BERSERKER, reverted1.ability());
        assertEquals(Ability.BERSERKER, reverted2.ability());
    }
}
