package com.gwent.engine.state;

import com.gwent.engine.domain.*;
import com.gwent.engine.exception.state.InvalidPhaseTransitionException;
import com.gwent.engine.exception.state.RoundLimitExceededException;
import com.gwent.engine.exception.state.TurnNotSetException;

public class GameState {
    private final Board board;
    private final PlayerState player1;
    private final PlayerState player2;
    private Turn currentTurn;
    private int currentRound;
    private GamePhase phase;
    private PendingAbility pendingAbility;
    private Turn winner;
    private EndReason endReason;

    public GameState (PlayerState player1, PlayerState player2) {
        this.player1 = player1;
        this.player2 = player2;
        this.board = new Board(player1, player2);
        this.currentRound = 1;
        this.phase = GamePhase.COIN_FLIP;
    }

    public Board getBoard () {
        return board;
    }

    public PlayerState getPlayer1 () {
        return player1;
    }

    public PlayerState getPlayer2 () {
        return player2;
    }

    public PlayerState getCurrentPlayer () {
        if (currentTurn == null) throw new TurnNotSetException();
        return currentTurn == Turn.PLAYER_1 ? player1 : player2;
    }

    public PlayerState getOpponent () {
        if (currentTurn == null) throw new TurnNotSetException();
        return currentTurn == Turn.PLAYER_1 ? player2 : player1;
    }

    public Turn getCurrentTurn () {
        if (currentTurn == null) throw new TurnNotSetException();
        return currentTurn;
    }

    public PlayerState getPlayer (Turn player) {
        return player == Turn.PLAYER_1 ? player1 : player2;
    }

    public PendingAbility getPendingAbility() {
        return pendingAbility;
    }

    public void setPendingAbility(PendingAbility pendingAbility) {
        this.pendingAbility = pendingAbility;
    }

    public void setCurrentTurn (Turn currentTurn) {
        this.currentTurn = currentTurn;
    }

    public void switchTurn () {
        if (currentTurn == null) throw new TurnNotSetException();
        currentTurn = (currentTurn == Turn.PLAYER_1) ? Turn.PLAYER_2 : Turn.PLAYER_1;
    }

    public int getCurrentRound () {
        return currentRound;
    }

    public void nextRound () {
        if (currentRound >= 3) throw new RoundLimitExceededException();
        currentRound++;
    }

    public GamePhase getPhase () {
        return phase;
    }

    public void setPhase (GamePhase phase) {
        boolean valid = switch (this.phase) {
            case COIN_FLIP -> phase == GamePhase.REDRAW;
            case REDRAW -> phase == GamePhase.PLAY;
            case PLAY -> phase == GamePhase.ROUND_END || phase == GamePhase.GAME_OVER;
            case ROUND_END -> phase == GamePhase.PLAY || phase == GamePhase.GAME_OVER;
            case GAME_OVER -> false;
        };

        if (!valid) throw new InvalidPhaseTransitionException(this.phase, phase);

        this.phase = phase;
    }

    public Turn getWinner () {
        return winner;
    }

    public EndReason getEndReason () {
        return endReason;
    }

    public void finishGame (Turn winner, EndReason endReason) {
        this.winner = winner;
        this.endReason = endReason;
        setPhase(GamePhase.GAME_OVER); // isso eh possivel? fazer um set dentro de outra funcao do state
    }
}
