package com.gwent.engine.state;

import com.gwent.engine.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class PlayerStateTest {

    private PlayerState player;
    private Card leader;
    private Card meleeUnit;
    private Card rangedUnit;
    private Card siegeUnit;

    @BeforeEach
    void setUp() {
        leader = new Card("foltest", "Foltest", Faction.NORTHERN_REALMS, CardType.LEADER,
                null, LeaderAbility.SIEGE_MASTER, null, null);
        meleeUnit = new Card("geralt", "Geralt of Rivia", Faction.NEUTRAL, CardType.HERO,
                null, null, RowType.MELEE, 15);
        rangedUnit = new Card("archer", "Archer", Faction.NORTHERN_REALMS, CardType.UNIT,
                null, null, RowType.RANGED, 5);
        siegeUnit = new Card("trebuchet", "Trebuchet", Faction.NORTHERN_REALMS, CardType.UNIT,
                null, null, RowType.SIEGE, 6);
        player = new PlayerState(leader, List.of(meleeUnit, rangedUnit, siegeUnit));
    }

    // --- Initial state ---

    @Test
    void shouldStartWithCorrectDefaults() {
        assertEquals(2, player.getLives());
        assertFalse(player.isPassed());
        assertFalse(player.isLeaderUsed());
        assertFalse(player.isEliminated());
        assertTrue(player.getHand().isEmpty());
        assertTrue(player.getGraveyard().isEmpty());
    }

    @Test
    void shouldHaveLeaderSet() {
        assertEquals(leader, player.getLeader());
    }

    @Test
    void shouldHaveDeckPopulated() {
        assertFalse(player.isDeckEmpty());
        assertEquals(3, player.getDeck().size());
    }

    // --- Draw card ---

    @Test
    void shouldDrawCardFromDeckToHand() {
        player.drawCard();

        assertEquals(1, player.getHand().size());
        assertEquals(meleeUnit, player.getHand().get(0));
        assertEquals(2, player.getDeck().size());
    }

    @Test
    void shouldDrawMultipleCards() {
        player.drawCard();
        player.drawCard();
        player.drawCard();

        assertEquals(3, player.getHand().size());
        assertTrue(player.isDeckEmpty());
    }

    @Test
    void shouldThrowWhenDrawingFromEmptyDeck() {
        player.drawCard();
        player.drawCard();
        player.drawCard();

        assertThrows(NoSuchElementException.class, () -> player.drawCard());
    }

    // --- Hand ---

    @Test
    void shouldAddCardToHand() {
        player.addToHand(meleeUnit);

        assertEquals(1, player.getHand().size());
        assertEquals(meleeUnit, player.getHand().get(0));
    }

    @Test
    void shouldRemoveCardFromHand() {
        player.addToHand(meleeUnit);
        player.removeFromHand(meleeUnit);

        assertTrue(player.getHand().isEmpty());
    }

    @Test
    void shouldDoNothingWhenRemovingCardNotInHand() {
        player.addToHand(meleeUnit);
        player.removeFromHand(rangedUnit);

        assertEquals(1, player.getHand().size());
    }

    @Test
    void shouldReturnUnmodifiableHand() {
        assertThrows(UnsupportedOperationException.class, () ->
                player.getHand().add(meleeUnit));
    }

    // --- Graveyard ---

    @Test
    void shouldAddCardToGraveyard() {
        player.addToGraveyard(meleeUnit);

        assertEquals(1, player.getGraveyard().size());
        assertEquals(meleeUnit, player.getGraveyard().get(0));
    }

    @Test
    void shouldRemoveCardFromGraveyard() {
        player.addToGraveyard(meleeUnit);
        player.removeFromGraveyard(meleeUnit);

        assertTrue(player.getGraveyard().isEmpty());
    }

    @Test
    void shouldDoNothingWhenRemovingCardNotInGraveyard() {
        player.addToGraveyard(meleeUnit);
        player.removeFromGraveyard(rangedUnit);

        assertEquals(1, player.getGraveyard().size());
    }

    @Test
    void shouldReturnUnmodifiableGraveyard() {
        assertThrows(UnsupportedOperationException.class, () ->
                player.getGraveyard().add(meleeUnit));
    }

    // --- Deck ---

    @Test
    void shouldReturnDefensiveCopyOfDeck() {
        var deckCopy = player.getDeck();
        deckCopy.push(meleeUnit);

        assertEquals(3, player.getDeck().size());
    }

    @Test
    void shouldReportDeckEmptyCorrectly() {
        var emptyPlayer = new PlayerState(leader, List.of());

        assertTrue(emptyPlayer.isDeckEmpty());
    }

    // --- Lives ---

    @Test
    void shouldLoseLife() {
        player.loseLife();

        assertEquals(1, player.getLives());
        assertFalse(player.isEliminated());
    }

    @Test
    void shouldBeEliminatedAtZeroLives() {
        player.loseLife();
        player.loseLife();

        assertEquals(0, player.getLives());
        assertTrue(player.isEliminated());
    }

    @Test
    void shouldThrowWhenLosingLifeAtZero() {
        player.loseLife();
        player.loseLife();

        assertThrows(IllegalStateException.class, () -> player.loseLife());
    }

    // --- Pass ---

    @Test
    void shouldPass() {
        player.pass();

        assertTrue(player.isPassed());
    }

    @Test
    void shouldResetPassed() {
        player.pass();
        player.resetPassed();

        assertFalse(player.isPassed());
    }

    // --- Leader ---

    @Test
    void shouldUseLeader() {
        player.useLeader();

        assertTrue(player.isLeaderUsed());
    }

    @Test
    void shouldThrowWhenUsingLeaderTwice() {
        player.useLeader();

        assertThrows(IllegalStateException.class, () -> player.useLeader());
    }

    @Test
    void shouldNotBeUsedInitially() {
        assertFalse(player.isLeaderUsed());
    }

    // --- Rows ---

    @Test
    void shouldReturnCorrectRows() {
        assertEquals(RowType.MELEE, player.getMeleeRow().getRowType());
        assertEquals(RowType.RANGED, player.getRangedRow().getRowType());
        assertEquals(RowType.SIEGE, player.getSiegeRow().getRowType());
    }

    // --- Clear rows ---

    @Test
    void shouldClearAllRowsAndMoveCardsToGraveyard() {
        player.getMeleeRow().addCard(meleeUnit);
        player.getRangedRow().addCard(rangedUnit);
        player.getSiegeRow().addCard(siegeUnit);

        player.clearRows();

        assertTrue(player.getMeleeRow().getCards().isEmpty());
        assertTrue(player.getRangedRow().getCards().isEmpty());
        assertTrue(player.getSiegeRow().getCards().isEmpty());
        assertEquals(3, player.getGraveyard().size());
    }

    @Test
    void shouldClearRowsWhenAlreadyEmpty() {
        player.clearRows();

        assertTrue(player.getGraveyard().isEmpty());
    }
}
