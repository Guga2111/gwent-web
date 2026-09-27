package com.gwent.engine.core;

import com.gwent.engine.domain.*;
import com.gwent.engine.state.BoardRow;
import com.gwent.engine.state.GameState;
import com.gwent.engine.state.PlayerState;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class AbilityResolver {

    private static final Map<String, String> MUSTER_TARGET_OVERRIDE = Map.of(
            "Cerys an Craite", "Shield Maiden"
    );
    
    private final ScoreCalculator scoreCalculator;
    
    public AbilityResolver (ScoreCalculator scoreCalculator) {
        this.scoreCalculator = scoreCalculator;
    }

    void resolve (GameState state, Card card, RowType targetRow) {
        resolveAbility(state, card, card.ability(), targetRow);
        resolveAbility(state, card, card.secondAbility(), targetRow);
    }

    private void resolveAbility(GameState state, Card card, Ability ability, RowType targetRow) {
        if (ability == null) return;

        switch (ability) {
            case SPY -> handleResolveSpy(state);
            case MEDIC          -> handleMedic(state);
            case MUSTER         -> handleMuster(state, card, targetRow);
            case SCORCH         -> handleScorch(state);
            case COMMANDERS_HORN -> handleCommandersHorn(state, targetRow);
            case DUMMY           -> handleDummy(state);
            case MARDROEME      -> handleMardroeme(state, targetRow);
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
        String targetName = MUSTER_TARGET_OVERRIDE.getOrDefault(card.name(), card.name());

        List<Card> fromHand = new ArrayList<>(current.getHand()).stream()
                .filter(c -> c.name().equals(targetName))
                .toList();
        for (Card c : fromHand) {
            current.removeFromHand(c);
            row.addCard(c);
        }

        List<Card> fromDeck = current.getDeck().stream()
                .filter(c -> c.name().equals(targetName))
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
            boolean kingBran = player.isKingBranActive();
            for (RowType rowType : rowTypes) {
                BoardRow row = player.getRow(rowType);
                for (Card c : row.getCards()) {
                    if (c.cardType() == CardType.HERO) continue;
                    int power = scoreCalculator.calculateCardPower(c, row, kingBran);
                    if (power > maxPower) maxPower = power;
                }
            }
        }

        final int finalMaxPower = maxPower;
        for (PlayerState player : players) {
            boolean kingBran = player.isKingBranActive();
            for (RowType rowType : rowTypes) {
                BoardRow row = player.getRow(rowType);
                List<Card> toScorch = row.getCards().stream()
                        .filter(c -> c.cardType() != CardType.HERO)
                        .filter(c -> scoreCalculator.calculateCardPower(c, row, kingBran) == finalMaxPower)
                        .toList();
                for (Card c : toScorch) {
                    row.removeCard(c);
                    player.addToGraveyard(c);
                    if (c.hasAbility(Ability.KAMBI)) {
                        summonHemdall(player);
                    }
                }
            }
        }
    }

    private void summonHemdall(PlayerState player) {
        Card hemdall = new Card("SK_HERO_HEMDALL", "Hemdall",
                Faction.SKELLIGE, CardType.HERO, null, null, RowType.MELEE, 11);
        player.getMeleeRow().addCard(hemdall);
    }

    private void handleDummy(GameState state) {
        PlayerState current = state.getCurrentPlayer();
        boolean hasValidTargets = false;
        for (RowType rowType : RowType.values()) {
            hasValidTargets = hasValidTargets || current.getRow(rowType).getCards().stream()
                    .anyMatch(c -> c.cardType() == CardType.UNIT);
        }
        if (hasValidTargets) {
            state.setPendingAbility(PendingAbility.DUMMY_CHOICE);
        }
    }

    private void handleCommandersHorn(GameState state, RowType targetRow) {
        PlayerState current = state.getCurrentPlayer();
        BoardRow row = current.getRow(targetRow);
        row.setHornActive(true);
    }

    private void handleMardroeme(GameState state, RowType targetRow) {
        PlayerState current = state.getCurrentPlayer();
        BerserkerHelper.transformBerserkers(current.getRow(targetRow));
    }
}
