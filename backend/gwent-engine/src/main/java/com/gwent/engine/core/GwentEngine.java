package com.gwent.engine.core;

import com.gwent.engine.command.*;
import com.gwent.engine.domain.*;
import com.gwent.engine.exception.command.*;
import com.gwent.engine.state.*;

import java.util.List;

public class GwentEngine {

    private final ScoreCalculator scoreCalculator = new ScoreCalculator();
    private final AbilityResolver abilityResolver = new AbilityResolver(scoreCalculator);
    private final LeaderAbilityResolver leaderAbilityResolver = new LeaderAbilityResolver(scoreCalculator);
    private final FactionPassiveResolver factionPassiveResolver = new FactionPassiveResolver();

    public void execute(GameState state, GameCommand command) {
        switch (command) {
            case PlayCardCommand c     -> handlePlayCard(state, c);
            case PassCommand c         -> handlePass(state, c);
            case MulliganCommand c     -> handleMulligan(state, c);
            case UseLeaderCommand c    -> handleUseLeader(state, c);
            case ResolveMedicCommand c -> handleResolveMedic(state, c);
            case ConfirmMulliganCommand c -> handleConfirmMulligan(state, c);
            case ResolveLeaderCommand c     -> handleResolveLeader(state, c);
            case ResolveScoiataelCommand c  -> handleResolveScoiatael(state, c);
        }
    }

    // --- Engine-initiated transitions ---

    public void resolveCoinFlip(GameState state, Turn firstPlayer) {
        if (factionPassiveResolver.hasScoiataelAdvantage(state)) {
            Turn scoiataelPlayer = factionPassiveResolver.getScoiataelPlayer(state);
            state.setCurrentTurn(scoiataelPlayer);
            state.setPendingAbility(PendingAbility.SCOIATAEL_FIRST_PLAYER_CHOICE);
        } else {
            state.setCurrentTurn(firstPlayer);
            state.getPlayer1().setMulligansRemaining(2);
            state.getPlayer2().setMulligansRemaining(2);
            state.setPhase(GamePhase.REDRAW);
        }
    }

    public void drawInitialCards(GameState state, int count) {
        drawCards(state.getPlayer1(), count);
        drawCards(state.getPlayer2(), count);
    }

    public void startPlay(GameState state) {
        state.setPhase(GamePhase.PLAY);
    }

    // --- Command handlers ---

    private void handlePlayCard(GameState state, PlayCardCommand command) {
        Card card = command.card();
        RowType targetRow = command.targetRow();
        PlayerState current = state.getCurrentPlayer();

        if (state.getPhase() != GamePhase.PLAY)
            throw new InvalidPhaseCommandException(GamePhase.PLAY, state.getPhase());
        if (current.isPassed())
            throw new PlayerAlreadyPassedException();
        if (!current.getHand().contains(card))
            throw new CardNotInHandException();
        validateRowCompatibility(card, targetRow);

        current.removeFromHand(card);

        if (card.cardType() == CardType.WEATHER) {
            placeWeatherCard(state, card, current);
        } else if (card.ability() == Ability.SPY) {
            state.getOpponent().getRow(targetRow).addCard(card);
        } else {
            current.getRow(targetRow).addCard(card);
        }

        abilityResolver.resolve(state, card, targetRow);

        if (state.getPendingAbility() == null && !state.getOpponent().isPassed()) {
            state.switchTurn();
        }
    }

    private void handlePass(GameState state, PassCommand command) {
        PlayerState current = state.getCurrentPlayer();

        if (state.getPhase() != GamePhase.PLAY)
            throw new InvalidPhaseCommandException(GamePhase.PLAY, state.getPhase());
        if (current.isPassed())
            throw new PlayerAlreadyPassedException();

        current.pass();

        if (state.getOpponent().isPassed()) {
            resolveRound(state);
        } else {
            state.switchTurn();
        }
    }

    private void handleMulligan(GameState state, MulliganCommand command) {
        Card card = command.cardToReturn();
        PlayerState current = state.getPlayer(command.player());

        if (current.isMulliganConfirmed())
            throw new PlayerAlreadyConfirmedMulliganException();
        if (state.getPhase() != GamePhase.REDRAW)
            throw new InvalidPhaseCommandException(GamePhase.REDRAW, state.getPhase());
        if (current.getMulligansRemaining() == 0)
            throw new NoMulligansRemainingException();
        if (!current.getHand().contains(card))
            throw new CardNotInHandException();

        current.removeFromHand(card);
        current.returnToDeck(card);
        if (!current.isDeckEmpty()) current.drawCard();
        current.decrementMulligans();
    }

    private void handleConfirmMulligan(GameState state, ConfirmMulliganCommand command) {
        // if both players confirmed → startPlay(state)
        Turn oppositePlayerTurn = command.player() == Turn.PLAYER_2 ? Turn.PLAYER_1 : Turn.PLAYER_2;

        PlayerState currentPlayer = state.getPlayer(command.player());
        PlayerState oppositePlayer = state.getPlayer(oppositePlayerTurn);

        if (state.getPhase() != GamePhase.REDRAW) throw new InvalidPhaseCommandException(GamePhase.REDRAW, state.getPhase());

        currentPlayer.confirmMulligan();

        if (oppositePlayer.isMulliganConfirmed()) {
            startPlay(state);
        }
    }

    private void handleUseLeader(GameState state, UseLeaderCommand command) {
        PlayerState current = state.getCurrentPlayer();

        if (state.getPhase() != GamePhase.PLAY)
            throw new InvalidPhaseCommandException(GamePhase.PLAY, state.getPhase());
        if (current.isLeaderUsed())
            throw new LeaderAlreadyUsedException();

        current.useLeader();
        applyLeaderAbility(state, current.getLeader().leaderAbility());
        state.switchTurn();
    }

    private void handleResolveMedic(GameState state, ResolveMedicCommand command) {
        Card card = command.card();
        PlayerState current = state.getCurrentPlayer();

        if (state.getPendingAbility() != PendingAbility.MEDIC_CHOICE)
            throw new InvalidPhaseCommandException(GamePhase.PLAY, state.getPhase());
        if (!current.getGraveyard().contains(card))
            throw new CardNotInGraveyardException();
        if (card.cardType() == CardType.SPECIAL || card.cardType() == CardType.WEATHER
                || card.cardType() == CardType.LEADER)
            throw new InvalidRowException();

        current.removeFromGraveyard(card);
        if (card.ability() == Ability.SPY) {
            state.getOpponent().getRow(card.rowType()).addCard(card);
        } else {
            current.getRow(card.rowType()).addCard(card);
        }
        state.setPendingAbility(null);
        abilityResolver.resolve(state, card, card.rowType());

        if (state.getPendingAbility() == null) {
            state.switchTurn();
        }
    }

    private void handleResolveScoiatael(GameState state, ResolveScoiataelCommand command) {
        if (state.getPendingAbility() != PendingAbility.SCOIATAEL_FIRST_PLAYER_CHOICE)
            throw new InvalidPhaseCommandException(GamePhase.COIN_FLIP, state.getPhase());

        state.setPendingAbility(null);
        state.setCurrentTurn(command.chosenFirstPlayer());
        state.getPlayer1().setMulligansRemaining(2);
        state.getPlayer2().setMulligansRemaining(2);
        state.setPhase(GamePhase.REDRAW);
    }

    private void handleResolveLeader(GameState state, ResolveLeaderCommand command) {
        if (state.getPendingAbility() == null)
            throw new InvalidPhaseCommandException(GamePhase.PLAY, state.getPhase());

        switch (state.getPendingAbility()) {
            case LEADER_GRAVEYARD_PICK          -> resolveLeaderGraveyardPick(state, command.card());
            case LEADER_GRAVEYARD_TO_HAND       -> resolveLeaderGraveyardToHand(state, command.card());
            case LEADER_OPPONENT_GRAVEYARD_PICK -> resolveLeaderOpponentGraveyardPick(state, command.card());
            case LEADER_DECK_PICK               -> resolveLeaderDeckPick(state, command.card());
            case LEADER_HAND_DISCARD            -> resolveLeaderHandDiscard(state, command.card());
            default -> throw new InvalidPhaseCommandException(GamePhase.PLAY, state.getPhase());
        }
    }

    private void resolveLeaderGraveyardPick(GameState state, Card card) {
        PlayerState current = state.getCurrentPlayer();
        if (!current.getGraveyard().contains(card))
            throw new CardNotInGraveyardException();
        if (card.cardType() != CardType.UNIT)
            throw new InvalidRowException();

        current.removeFromGraveyard(card);
        if (card.ability() == Ability.SPY) {
            state.getOpponent().getRow(card.rowType()).addCard(card);
        } else {
            current.getRow(card.rowType()).addCard(card);
        }

        int remaining = state.getPendingAbilityCount() - 1;
        if (remaining > 0) {
            boolean hasMoreUnits = current.getGraveyard().stream()
                    .anyMatch(c -> c.cardType() == CardType.UNIT);
            if (hasMoreUnits) {
                state.setPendingAbilityCount(remaining);
                // resolve abilities of placed card, but keep pending for next pick
                abilityResolver.resolve(state, card, card.rowType());
                return;
            }
        }

        clearLeaderPending(state);
        abilityResolver.resolve(state, card, card.rowType());

        if (state.getPendingAbility() == null) {
            autoPassIfHandEmpty(state.getCurrentPlayer());
            resolveAfterAction(state);
        }
    }

    private void resolveLeaderOpponentGraveyardPick(GameState state, Card card) {
        PlayerState opponent = state.getOpponent();
        PlayerState current = state.getCurrentPlayer();
        if (!opponent.getGraveyard().contains(card))
            throw new CardNotInGraveyardException();
        if (card.cardType() != CardType.UNIT)
            throw new InvalidRowException();

        opponent.removeFromGraveyard(card);
        if (card.ability() == Ability.SPY) {
            state.getOpponent().getRow(card.rowType()).addCard(card);
        } else {
            current.getRow(card.rowType()).addCard(card);
        }
        clearLeaderPending(state);

        abilityResolver.resolve(state, card, card.rowType());

        if (state.getPendingAbility() == null) {
            autoPassIfHandEmpty(state.getCurrentPlayer());
            resolveAfterAction(state);
        }
    }

    private void resolveLeaderDeckPick(GameState state, Card card) {
        PlayerState current = state.getCurrentPlayer();
        if (!current.getDeck().contains(card))
            throw new CardNotInDeckException();

        current.removeFromDeck(card);

        if (state.getPendingLeaderAbility() == LeaderAbility.COMMANDER_OF_THE_RED_RIDERS) {
            current.addToHand(card);
            state.setPendingAbility(PendingAbility.LEADER_HAND_DISCARD);
            // keep pendingLeaderAbility for context
        } else {
            // KING_OF_TEMERIA: play the card immediately
            clearLeaderPending(state);
            playCardFromDeck(state, card);
        }
    }

    private void resolveLeaderGraveyardToHand(GameState state, Card card) {
        PlayerState current = state.getCurrentPlayer();
        if (!current.getGraveyard().contains(card))
            throw new CardNotInGraveyardException();
        if (card.cardType() != CardType.UNIT)
            throw new InvalidRowException();

        current.removeFromGraveyard(card);
        current.addToHand(card);
        clearLeaderPending(state);

        autoPassIfHandEmpty(state.getCurrentPlayer());
        resolveAfterAction(state);
    }

    private void resolveLeaderHandDiscard(GameState state, Card card) {
        PlayerState current = state.getCurrentPlayer();
        if (!current.getHand().contains(card))
            throw new CardNotInHandException();

        current.removeFromHand(card);
        current.addToGraveyard(card);

        int remaining = state.getPendingAbilityCount() - 1;
        if (remaining > 0 && !current.getHand().isEmpty()) {
            state.setPendingAbilityCount(remaining);
            return;
        }

        if (state.getPendingLeaderAbility() == LeaderAbility.DESTROYER_OF_WORLDS
                && !current.isDeckEmpty()) {
            current.drawCard();
        }
        clearLeaderPending(state);

        autoPassIfHandEmpty(current);
        resolveAfterAction(state);
    }

    private void playCardFromDeck(GameState state, Card card) {
        PlayerState current = state.getCurrentPlayer();

        if (card.cardType() == CardType.WEATHER) {
            placeWeatherCard(state, card, current);
        } else if (card.ability() == Ability.SPY) {
            state.getOpponent().getRow(card.rowType()).addCard(card);
        } else {
            current.getRow(card.rowType()).addCard(card);
        }

        abilityResolver.resolve(state, card, card.rowType());

        if (state.getPendingAbility() == null) {
            autoPassIfHandEmpty(state.getCurrentPlayer());
            resolveAfterAction(state);
        }
    }

    private void clearLeaderPending(GameState state) {
        state.setPendingAbility(null);
        state.setPendingLeaderAbility(null);
        state.setPendingAbilityCount(0);
    }

    // --- Round management ---

    private void resolveRound(GameState state) {
        state.setPhase(GamePhase.ROUND_END);

        int p1Score = scoreCalculator.calculate(state.getPlayer1());
        int p2Score = scoreCalculator.calculate(state.getPlayer2());

        Faction p1Faction = state.getPlayer1().getLeader().faction();
        Faction p2Faction = state.getPlayer2().getLeader().faction();

        Turn loser;
        if (p1Score > p2Score) {
            state.getPlayer2().loseLife();
            loser = Turn.PLAYER_2;
            factionPassiveResolver.resolveNorthernRealmsBonus(state.getPlayer1());
        } else if (p2Score > p1Score) {
            state.getPlayer1().loseLife();
            loser = Turn.PLAYER_1;
            factionPassiveResolver.resolveNorthernRealmsBonus(state.getPlayer2());
        } else if (p1Faction == Faction.NILFGAARD || p2Faction == Faction.NILFGAARD) {
            loser = factionPassiveResolver.resolveNilfgaardTie(state);
        } else {
            state.getPlayer1().loseLife();
            state.getPlayer2().loseLife();
            loser = state.getCurrentTurn();
        }

        if (state.getPlayer1().isEliminated() || state.getPlayer2().isEliminated()) {
            state.setPhase(GamePhase.GAME_OVER);
        } else {
            startNewRound(state, loser);
        }
    }

    private void startNewRound(GameState state, Turn loser) {
        FactionPassiveResolver.KeptCard p1Kept = factionPassiveResolver.resolveMonsterKeepCard(state.getPlayer1());
        FactionPassiveResolver.KeptCard p2Kept = factionPassiveResolver.resolveMonsterKeepCard(state.getPlayer2());

        state.getPlayer1().clearRows();
        state.getPlayer2().clearRows();

        if (p1Kept != null) state.getPlayer1().getRow(p1Kept.row()).addCard(p1Kept.card());
        if (p2Kept != null) state.getPlayer2().getRow(p2Kept.row()).addCard(p2Kept.card());

        clearAllWeatherActive(state);
        state.getBoard().clearWeatherCards();
        state.getPlayer1().resetPassed();
        state.getPlayer2().resetPassed();
        state.setCurrentTurn(loser);
        drawCards(state.getPlayer1(), 2);
        drawCards(state.getPlayer2(), 2);
        state.nextRound();

        if (state.getCurrentRound() == 3) {
            factionPassiveResolver.resolveSkelligeRound3(state.getPlayer1());
            factionPassiveResolver.resolveSkelligeRound3(state.getPlayer2());
        }

        state.setPhase(GamePhase.PLAY);
    }

    // --- Helpers ---

    private void validateRowCompatibility(Card card, RowType targetRow) {
        if (card.cardType() == CardType.WEATHER || card.cardType() == CardType.SPECIAL) return;
        if (card.ability() == Ability.AGILE) {
            if (targetRow != RowType.MELEE && targetRow != RowType.RANGED) throw new InvalidRowException();
            return;
        }
        if (card.rowType() != targetRow) throw new InvalidRowException();
    }

    private void placeWeatherCard(GameState state, Card card, PlayerState current) {
        switch (card.ability()) {
            case FROST -> {
                state.getBoard().addWeatherCard(card);
                state.getPlayer1().getMeleeRow().setWeatherActive(true);
                state.getPlayer2().getMeleeRow().setWeatherActive(true);
            }
            case FOG -> {
                state.getBoard().addWeatherCard(card);
                state.getPlayer1().getRangedRow().setWeatherActive(true);
                state.getPlayer2().getRangedRow().setWeatherActive(true);
            }
            case RAIN -> {
                state.getBoard().addWeatherCard(card);
                state.getPlayer1().getSiegeRow().setWeatherActive(true);
                state.getPlayer2().getSiegeRow().setWeatherActive(true);
            }
            case CLEAR_WEATHER -> {
                clearAllWeatherActive(state);
                state.getBoard().getActiveWeatherCards().forEach(current::addToGraveyard);
                state.getBoard().clearWeatherCards();
                current.addToGraveyard(card);
            }
        }
    }

    private void clearAllWeatherActive(GameState state) {
        for (PlayerState player : List.of(state.getPlayer1(), state.getPlayer2())) {
            player.getMeleeRow().setWeatherActive(false);
            player.getRangedRow().setWeatherActive(false);
            player.getSiegeRow().setWeatherActive(false);
        }
    }

    private void drawCards(PlayerState player, int count) {
        for (int i = 0; i < count; i++) {
            if (!player.isDeckEmpty()) player.drawCard();
        }
    }

    private void applyLeaderAbility(GameState state, LeaderAbility ability) {
        leaderAbilityResolver.resolve(state, ability);
    }
}
