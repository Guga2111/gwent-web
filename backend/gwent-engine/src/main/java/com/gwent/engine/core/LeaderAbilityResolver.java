package com.gwent.engine.core;

import com.gwent.engine.domain.*;
import com.gwent.engine.state.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class LeaderAbilityResolver {

    private final ScoreCalculator scoreCalculator;

    LeaderAbilityResolver(ScoreCalculator scoreCalculator) {
        this.scoreCalculator = scoreCalculator;
    }

    void resolve(GameState state, LeaderAbility ability) {
        switch (ability) {

            // --- Northern Realms ---
            case SIEGE_MASTER                -> handleSiegeMaster(state);
            case NORTH_COMMANDER             -> handleNorthCommander(state);
            case KING_OF_TEMERIA             -> handleKingOfTemeria(state);
            case LORD_COMMANDER              -> handleDestroyStrongestInRow(state, RowType.SIEGE);

            // --- Nilfgaard ---
            case WHITE_FLAME                 -> handlePickWeatherFromDeck(state);
            case EMPEROR_OF_NILFGAARD        -> handleEmperorOfNilfgaard(state);
            case INVADER_OF_THE_NORTH        -> handleInvaderOfTheNorth(state);
            case RELENTLESS                  -> handleRelentless(state);

            // --- Monsters ---
            case DESTROYER_OF_WORLDS         -> handleDestroyerOfWorlds(state);
            case BRINGER_OF_DEATH            -> handlePickWeatherFromDeck(state);
            case COMMANDER_OF_THE_RED_RIDERS -> handleCommanderOfTheRedRiders(state);
            case KING_OF_THE_WILD_HUNT       -> handleKingOfTheWildHunt(state);

            // --- Scoia'tael ---
            case DAISY_OF_THE_VALLEY         -> handlePickWeatherFromDeck(state);
            case QUEEN_OF_DOL_BLATHANNA      -> handleDestroyStrongestInRow(state, RowType.MELEE);
            case HOPE_OF_THE_AEN_SEIDHE      -> handleHopeOfTheAenSeidhe(state);
            // PUREBLOOD_ELF — TODO: requires mid-pending turn swap
            case PUREBLOOD_ELF               -> {}

            // --- Skellige ---
            case KING_BRAN                   -> handleKingBran(state);
            case CLAN_AN_CRAITE              -> handleClanAnCraite(state);
        }
    }

    // Northern Realms: clears weather from siege row on both sides
    private void handleSiegeMaster(GameState state) {
        state.getPlayer1().getSiegeRow().setWeatherActive(false);
        state.getPlayer2().getSiegeRow().setWeatherActive(false);
    }

    // Northern Realms: +1 power to all units in siege row
    private void handleNorthCommander(GameState state) {
        state.getCurrentPlayer().getSiegeRow().setLeaderBonusPower(1);
    }

    // Northern Realms: pick any card from deck and play it immediately
    private void handleKingOfTemeria(GameState state) {
        PlayerState current = state.getCurrentPlayer();
        if (current.isDeckEmpty()) return;
        state.setPendingAbility(PendingAbility.LEADER_DECK_PICK);
        state.setPendingLeaderAbility(LeaderAbility.KING_OF_TEMERIA);
    }

    // Nilfgaard / Scoia'tael / Monsters: move first weather card from deck to hand
    private void handlePickWeatherFromDeck(GameState state) {
        PlayerState current = state.getCurrentPlayer();
        current.getDeck().stream()
                .filter(c -> c.cardType() == CardType.WEATHER)
                .findFirst()
                .ifPresent(c -> {
                    current.removeFromDeck(c);
                    current.addToHand(c);
                });
    }

    // Nilfgaard: reveal up to 3 random cards from opponent's hand
    private void handleEmperorOfNilfgaard(GameState state) {
        PlayerState opponent = state.getOpponent();
        List<Card> hand = new ArrayList<>(opponent.getHand());
        Collections.shuffle(hand);
        int count = Math.min(3, hand.size());
        state.setRevealedCards(hand.subList(0, count));
    }

    // Nilfgaard: cancel opponent's leader ability (mark as used)
    private void handleInvaderOfTheNorth(GameState state) {
        PlayerState opponent = state.getOpponent();
        if (!opponent.isLeaderUsed()) {
            opponent.useLeader();
        }
    }

    // Nilfgaard: pick 1 unit from opponent's graveyard, play on your side
    private void handleRelentless(GameState state) {
        PlayerState opponent = state.getOpponent();
        boolean hasUnits = opponent.getGraveyard().stream()
                .anyMatch(c -> c.cardType() == CardType.UNIT);
        if (!hasUnits) return;
        state.setPendingAbility(PendingAbility.LEADER_OPPONENT_GRAVEYARD_PICK);
        state.setPendingLeaderAbility(LeaderAbility.RELENTLESS);
    }

    // Monsters: discard 2 cards from hand and draw 1 from deck
    private void handleDestroyerOfWorlds(GameState state) {
        PlayerState current = state.getCurrentPlayer();
        List<Card> toDiscard = new ArrayList<>(current.getHand()).stream()
                .limit(2)
                .toList();
        toDiscard.forEach(c -> {
            current.removeFromHand(c);
            current.addToGraveyard(c);
        });
        if (!current.isDeckEmpty()) current.drawCard();
    }

    // Monsters: pick any card from deck (goes to hand), then discard 1 from hand
    private void handleCommanderOfTheRedRiders(GameState state) {
        PlayerState current = state.getCurrentPlayer();
        if (current.isDeckEmpty()) return;
        state.setPendingAbility(PendingAbility.LEADER_DECK_PICK);
        state.setPendingLeaderAbility(LeaderAbility.COMMANDER_OF_THE_RED_RIDERS);
    }

    // Monsters: restore 1 unit from own graveyard (like medic)
    private void handleKingOfTheWildHunt(GameState state) {
        PlayerState current = state.getCurrentPlayer();
        boolean hasUnits = current.getGraveyard().stream()
                .anyMatch(c -> c.cardType() == CardType.UNIT);
        if (!hasUnits) return;
        state.setPendingAbility(PendingAbility.LEADER_GRAVEYARD_PICK);
        state.setPendingLeaderAbility(LeaderAbility.KING_OF_THE_WILD_HUNT);
    }

    // LORD_COMMANDER (siege) / QUEEN_OF_DOL_BLATHANNA (melee): destroy strongest non-HERO unit if row score >= 10
    private void handleDestroyStrongestInRow(GameState state, RowType rowType) {
        PlayerState opponent = state.getOpponent();
        BoardRow row = opponent.getRow(rowType);

        int rowScore = scoreCalculator.calculate(row);
        if (rowScore < 10) return;

        Card strongest = null;
        int maxPower = -1;
        for (Card card : row.getCards()) {
            if (card.cardType() == CardType.HERO) continue;
            if (card.cardType() != CardType.UNIT) continue;
            if (card.basePower() > maxPower) {
                maxPower = card.basePower();
                strongest = card;
            }
        }

        if (strongest != null) {
            row.removeCard(strongest);
            opponent.addToGraveyard(strongest);
        }
    }

    // Scoia'tael: +1 power to all units in melee and ranged rows
    private void handleHopeOfTheAenSeidhe(GameState state) {
        state.getCurrentPlayer().getMeleeRow().setLeaderBonusPower(1);
        state.getCurrentPlayer().getRangedRow().setLeaderBonusPower(1);
    }

    // Skellige: move all graveyard cards back to deck
    private void handleKingBran(GameState state) {
        PlayerState current = state.getCurrentPlayer();
        new ArrayList<>(current.getGraveyard()).forEach(c -> {
            current.removeFromGraveyard(c);
            current.returnToDeck(c);
        });
    }

    // Skellige: restore 2 units from own graveyard (2 sequential picks)
    private void handleClanAnCraite(GameState state) {
        PlayerState current = state.getCurrentPlayer();
        boolean hasUnits = current.getGraveyard().stream()
                .anyMatch(c -> c.cardType() == CardType.UNIT);
        if (!hasUnits) return;

        long unitCount = current.getGraveyard().stream()
                .filter(c -> c.cardType() == CardType.UNIT)
                .count();
        state.setPendingAbility(PendingAbility.LEADER_GRAVEYARD_PICK);
        state.setPendingLeaderAbility(LeaderAbility.CLAN_AN_CRAITE);
        state.setPendingAbilityCount((int) Math.min(2, unitCount));
    }

}
