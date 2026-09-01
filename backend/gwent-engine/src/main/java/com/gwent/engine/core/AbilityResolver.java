package com.gwent.engine.core;

import com.gwent.engine.domain.Card;
import com.gwent.engine.domain.CardType;
import com.gwent.engine.domain.PendingAbility;
import com.gwent.engine.domain.RowType;
import com.gwent.engine.state.BoardRow;
import com.gwent.engine.state.GameState;
import com.gwent.engine.state.PlayerState;

import java.util.ArrayList;
import java.util.List;

class AbilityResolver {
    
    private final ScoreCalculator scoreCalculator;
    
    public AbilityResolver (ScoreCalculator scoreCalculator) {
        this.scoreCalculator = scoreCalculator;
    }

    void resolve (GameState state, Card card, RowType targetRow) {
        if (card.ability() == null) return;

        switch (card.ability()) {
            case SPY -> handleResolveSpy(state);
            case MEDIC          -> handleMedic(state);
            case MUSTER         -> handleMuster(state, card, targetRow);
            case SCORCH         -> handleScorch(state);
            case COMMANDERS_HORN -> handleCommandersHorn(state, targetRow);
            default -> {}
        }
    }

    private void handleResolveSpy(GameState state) {
        PlayerState current = state.getCurrentPlayer();
        if (!current.isDeckEmpty()) current.drawCard();
        if (!current.isDeckEmpty()) current.drawCard();
    }

    private void handleMedic(GameState state) {
        boolean hasRevivableCards = state.getCurrentPlayer().getGraveyard().stream()
                .anyMatch(c -> c.cardType() == CardType.UNIT);
        if (hasRevivableCards) {
            state.setPendingAbility(PendingAbility.MEDIC_CHOICE);
        }
    }

    private void handleMuster(GameState state, Card card, RowType targetRow) {
        PlayerState current = state.getCurrentPlayer();
        BoardRow row = current.getRow(targetRow);

        List<Card> fromHand = new ArrayList<>(current.getHand()).stream()
                .filter(c -> c.name().equals(card.name()))
                .toList();
        for (Card c : fromHand) {
            current.removeFromHand(c);
            row.addCard(c);
        }

        List<Card> fromDeck = current.getDeck().stream()
                .filter(c -> c.name().equals(card.name()))
                .toList();
        for (Card c : fromDeck) {
            current.removeFromDeck(c);
            row.addCard(c);
        }
    }

    private void handleScorch(GameState state) {
        List<PlayerState> players = List.of(state.getPlayer1(), state.getPlayer2());
        List<RowType> rowTypes = List.of(RowType.values());

        int maxPower = 0;
        for (PlayerState player : players) {
            for (RowType rowType : rowTypes) {
                BoardRow row = player.getRow(rowType);
                for (Card c : row.getCards()) {
                    if (c.cardType() == CardType.HERO) continue;
                    int power = scoreCalculator.calculateCardPower(c, row);
                    if (power > maxPower) maxPower = power;
                }
            }
        }

        final int finalMaxPower = maxPower;
        for (PlayerState player : players) {
            for (RowType rowType : rowTypes) {
                BoardRow row = player.getRow(rowType);
                List<Card> toScorch = row.getCards().stream()
                        .filter(c -> c.cardType() != CardType.HERO)
                        .filter(c -> scoreCalculator.calculateCardPower(c, row) == finalMaxPower)
                        .toList();
                for (Card c : toScorch) {
                    row.removeCard(c);
                    player.addToGraveyard(c);
                }
            }
        }
    }

    private void handleCommandersHorn(GameState state, RowType targetRow) {
        PlayerState current = state.getCurrentPlayer();
        BoardRow row = current.getRow(targetRow);
        row.setHornActive(true);
    }
}
