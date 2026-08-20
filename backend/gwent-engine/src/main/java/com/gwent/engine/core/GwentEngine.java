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

    public void execute(GameState state, GameCommand command) {
        switch (command) {
            case PlayCardCommand c     -> handlePlayCard(state, c);
            case PassCommand c         -> handlePass(state, c);
            case MulliganCommand c     -> handleMulligan(state, c);
            case UseLeaderCommand c    -> handleUseLeader(state, c);
            case ResolveMedicCommand c -> handleResolveMedic(state, c);
            case ConfirmMulliganCommand c -> handleConfirmMulligan(state, c);
            case ResolveLeaderCommand c  -> handleResolveLeader(state, c);
        }
    }

    public int calculateScore(PlayerState player) {
        return scoreCalculator.calculate(player);
    }

    // --- Engine-initiated transitions ---

    public void resolveCoinFlip(GameState state, Turn firstPlayer) {
        state.setCurrentTurn(firstPlayer);
        state.getPlayer1().setMulligansRemaining(2);
        state.getPlayer2().setMulligansRemaining(2);
        state.setPhase(GamePhase.REDRAW);
    }

    public void drawInitialCards(GameState state, int count) {
        drawCards(state.getPlayer1(), count);
        drawCards(state.getPlayer2(), count);
    }

    public void startPlay(GameState state) {
        state.setPhase(GamePhase.PLAY);
    }

    public void surrender(GameState state, Turn player) {
        Turn opposite = player == Turn.PLAYER_1 ? Turn.PLAYER_2 : Turn.PLAYER_1;
        state.finishGame(opposite, EndReason.SURRENDER);
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

        if (state.getPendingAbility() == null) {
            autoPassIfHandEmpty(state.getCurrentPlayer());
            resolveAfterAction(state);
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
        Turn oppositePlayerTurn = command.player() == Turn.PLAYER_2 ? Turn.PLAYER_1 : Turn.PLAYER_2;

        PlayerState currentPlayer = state.getPlayer(command.player());
        PlayerState oppositePlayer = state.getPlayer(oppositePlayerTurn);

        if (state.getPhase() != GamePhase.REDRAW) throw new InvalidPhaseCommandException(GamePhase.REDRAW, state.getPhase());
        if (currentPlayer.isMulliganConfirmed()) throw new PlayerAlreadyConfirmedMulliganException();

        // Process mulligans atomically before confirming
        for (Card card : command.cardsToReturn()) {
            if (currentPlayer.getMulligansRemaining() == 0) break;
            if (!currentPlayer.getHand().contains(card)) continue;
            currentPlayer.removeFromHand(card);
            currentPlayer.returnToDeck(card);
            if (!currentPlayer.isDeckEmpty()) currentPlayer.drawCard();
            currentPlayer.decrementMulligans();
        }

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
        if (state.getPendingAbility() == null && !state.getOpponent().isPassed()) {
            state.switchTurn();
        }
    }

    private void handleResolveMedic(GameState state, ResolveMedicCommand command) {
        Card card = command.card();
        PlayerState current = state.getCurrentPlayer();

        if (state.getPendingAbility() != PendingAbility.MEDIC_CHOICE)
            throw new InvalidPhaseCommandException(GamePhase.PLAY, state.getPhase());
        if (!current.getGraveyard().contains(card))
            throw new CardNotInGraveyardException();
        if (card.cardType() != CardType.UNIT)
            throw new InvalidRowException();

        current.removeFromGraveyard(card);
        current.getRow(card.rowType()).addCard(card);
        state.setPendingAbility(null);
        abilityResolver.resolve(state, card, card.rowType());

        if (state.getPendingAbility() == null) {
            autoPassIfHandEmpty(state.getCurrentPlayer());
            resolveAfterAction(state);
        }
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
        current.getRow(card.rowType()).addCard(card);

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
        current.getRow(card.rowType()).addCard(card);
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

        Turn loser;
        if (p1Score > p2Score) {
            state.getPlayer2().loseLife();
            loser = Turn.PLAYER_2;
        } else if (p2Score > p1Score) {
            state.getPlayer1().loseLife();
            loser = Turn.PLAYER_1;
        } else {
            state.getPlayer1().loseLife();
            state.getPlayer2().loseLife();
            loser = state.getCurrentTurn();
        }

        boolean p1Out = state.getPlayer1().isEliminated();
        boolean p2Out = state.getPlayer2().isEliminated();

        if (p1Out && p2Out) {
            state.finishGame(null, EndReason.NORMAL);
        } else if (p1Out) {
            state.finishGame(Turn.PLAYER_2, EndReason.NORMAL);
        } else if (p2Out) {
            state.finishGame(Turn.PLAYER_1, EndReason.NORMAL);
        } else {
            startNewRound(state, loser);
        }
    }

    private void startNewRound(GameState state, Turn loser) {
        state.getPlayer1().clearRows();
        state.getPlayer2().clearRows();
        clearAllWeatherActive(state);
        state.getBoard().clearWeatherCards();
        state.getPlayer1().resetPassed();
        state.getPlayer2().resetPassed();
        state.setCurrentTurn(loser);
        state.nextRound();
        state.setPhase(GamePhase.PLAY);
    }

    // --- Helpers ---

    private void autoPassIfHandEmpty(PlayerState player) {
        if (player.getHand().isEmpty() && player.isLeaderUsed() && !player.isPassed()) {
            player.pass();
        }
    }

    private void resolveAfterAction(GameState state) {
        PlayerState current = state.getCurrentPlayer();
        PlayerState opponent = state.getOpponent();
        if (current.isPassed() && opponent.isPassed()) {
            resolveRound(state);
        } else if (current.isPassed() || !opponent.isPassed()) {
            state.switchTurn();
        }
        // else: opponent already passed, current can still play → no switch
    }

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
