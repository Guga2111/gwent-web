package com.gwent.engine.state;

import com.gwent.engine.domain.GamePhase;
import com.gwent.engine.domain.Turn;

public class GameState {
    private final Board board;
    private final PlayerState player1;
    private final PlayerState player2;
    private Turn currentTurn;
    private int currentRound;
    private GamePhase phase;

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
        if (currentTurn == null) throw new IllegalStateException("Current turn has not been set");
        return currentTurn == Turn.PLAYER_1 ? player1 : player2;
    }

    public PlayerState getOpponent () {
        if (currentTurn == null) throw new IllegalStateException("Current turn has not been set");
        return currentTurn == Turn.PLAYER_1 ? player2 : player1;
    }

    public Turn getCurrentTurn () {
        if (currentTurn == null) throw new IllegalStateException("Current turn has not been set");
        return currentTurn;
    }

    public void setCurrentTurn (Turn currentTurn) {
        this.currentTurn = currentTurn;
    }

    public void switchTurn () {
        if (currentTurn == null) throw new IllegalStateException("Current turn has not been set");
        currentTurn = (currentTurn == Turn.PLAYER_1) ? Turn.PLAYER_2 : Turn.PLAYER_1;
    }

    public int getCurrentRound () {
        return currentRound;
    }

    public void nextRound () {
        if (currentRound >= 3) throw new IllegalStateException("Cannot exceed 3 rounds");
        currentRound++;
    }

    public GamePhase getPhase () {
        return phase;
    }

    public void setPhase (GamePhase phase) {
        boolean valid = switch (this.phase) {
            case COIN_FLIP -> phase == GamePhase.REDRAW;
            case REDRAW -> phase == GamePhase.PLAY;
            case PLAY -> phase == GamePhase.ROUND_END;
            case ROUND_END -> phase == GamePhase.REDRAW || phase == GamePhase.GAME_OVER;
            case GAME_OVER -> false;
        };

        if (!valid) throw new IllegalStateException(
                "Cannot transition from " + this.phase + " to " + phase);

        this.phase = phase;
    }
}
