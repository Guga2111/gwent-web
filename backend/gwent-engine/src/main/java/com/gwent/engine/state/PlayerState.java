package com.gwent.engine.state;

import com.gwent.engine.domain.Card;
import com.gwent.engine.domain.RowType;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

public class PlayerState {
    private List<Card> hand = new ArrayList<>();
    private Deque<Card> deck;
    private List<Card> graveyard = new ArrayList<>();
    private BoardRow meleeRow = new BoardRow(RowType.MELEE);
    private BoardRow rangedRow = new BoardRow(RowType.RANGED);
    private BoardRow siegeRow = new BoardRow(RowType.SIEGE);
    private final Card leader;
    private boolean leaderUsed;
    private int lives;
    private boolean passed;

    public PlayerState (Card leader, List<Card> deck) {
        this.leader = leader;
        this.lives = 2;
        this.deck = new ArrayDeque<>(deck);
    }

    // if no elements throw NoSuchElementException so before calling it use isDeckEmpty() for check
    public void drawCard () {
        hand.add(deck.pop());
    }

    public boolean isDeckEmpty () {
        return deck.isEmpty();
    }

    public List<Card> getHand () {
        return Collections.unmodifiableList(hand);
    }

    public List<Card> getGraveyard () {
        return Collections.unmodifiableList(graveyard);
    }

    public Deque<Card> getDeck () {
        return new ArrayDeque<>(deck);
    }

    public void addToHand (Card card) {
        hand.add(card);
    }

    public void removeFromHand (Card card) {
        hand.remove(card);
    }

    public void addToGraveyard (Card card) {
        graveyard.add(card);
    }

    public void removeFromGraveyard (Card card) {
        graveyard.remove(card);
    }

    public void loseLife () {

        if (lives <= 0) throw new IllegalStateException("The players life is already 0");

        lives = lives - 1;
    }

    public int getLives () {
        return lives;
    }

    public boolean isEliminated () {
        return lives <= 0;
    }

    public void pass () {
        passed = true;
    }

    public boolean isPassed () {
        return passed;
    }

    public void resetPassed () {
        passed = false;
    }

    public Card getLeader () {
        return leader;
    }

    public void useLeader () {
        if (leaderUsed) throw new IllegalStateException("Leader ability has already been used");
        leaderUsed = true;
    }

    public boolean isLeaderUsed () {
        return leaderUsed;
    }

    public BoardRow getMeleeRow () {
        return meleeRow;
    }

    public BoardRow getRangedRow () {
        return rangedRow;
    }

    public BoardRow getSiegeRow () {
        return siegeRow;
    }

    public void clearRows () {
        graveyard.addAll(meleeRow.getCards());
        graveyard.addAll(rangedRow.getCards());
        graveyard.addAll(siegeRow.getCards());
        meleeRow.clear();
        rangedRow.clear();
        siegeRow.clear();
    }
}
