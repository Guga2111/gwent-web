package com.gwent.engine.state;

import com.gwent.engine.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameStateTest {

    private GameState gameState;
    private PlayerState player1;
    private PlayerState player2;

    @BeforeEach
    void setUp() {
        Card leader1 = new Card("foltest", "Foltest", Faction.NORTHERN_REALMS, CardType.LEADER,
                null, LeaderAbility.SIEGE_MASTER, null, null);
        Card leader2 = new Card("emhyr", "Emhyr", Faction.NILFGAARD, CardType.LEADER,
                null, LeaderAbility.EMPEROR_OF_NILFGAARD, null, null);

        player1 = new PlayerState(leader1, List.of());
        player2 = new PlayerState(leader2, List.of());
        gameState = new GameState(player1, player2);
    }

    // --- Initial state ---

    @Test
    void shouldStartWithCorrectDefaults() {
        assertEquals(1, gameState.getCurrentRound());
        assertEquals(GamePhase.COIN_FLIP, gameState.getPhase());
        assertNotNull(gameState.getBoard());
    }

    @Test
    void shouldHaveBothPlayers() {
        assertEquals(player1, gameState.getPlayer1());
        assertEquals(player2, gameState.getPlayer2());
    }

    // --- Turn (null guards) ---

    @Test
    void shouldThrowWhenGetCurrentPlayerBeforeCoinFlip() {
        assertThrows(IllegalStateException.class, () -> gameState.getCurrentPlayer());
    }

    @Test
    void shouldThrowWhenGetOpponentBeforeCoinFlip() {
        assertThrows(IllegalStateException.class, () -> gameState.getOpponent());
    }

    @Test
    void shouldThrowWhenGetCurrentTurnBeforeCoinFlip() {
        assertThrows(IllegalStateException.class, () -> gameState.getCurrentTurn());
    }

    @Test
    void shouldThrowWhenSwitchTurnBeforeCoinFlip() {
        assertThrows(IllegalStateException.class, () -> gameState.switchTurn());
    }

    // --- Turn (set and switch) ---

    @Test
    void shouldSetCurrentTurnToPlayer1() {
        gameState.setCurrentTurn(Turn.PLAYER_1);

        assertEquals(Turn.PLAYER_1, gameState.getCurrentTurn());
        assertEquals(player1, gameState.getCurrentPlayer());
        assertEquals(player2, gameState.getOpponent());
    }

    @Test
    void shouldSetCurrentTurnToPlayer2() {
        gameState.setCurrentTurn(Turn.PLAYER_2);

        assertEquals(Turn.PLAYER_2, gameState.getCurrentTurn());
        assertEquals(player2, gameState.getCurrentPlayer());
        assertEquals(player1, gameState.getOpponent());
    }

    @Test
    void shouldSwitchTurnFromPlayer1ToPlayer2() {
        gameState.setCurrentTurn(Turn.PLAYER_1);
        gameState.switchTurn();

        assertEquals(Turn.PLAYER_2, gameState.getCurrentTurn());
    }

    @Test
    void shouldSwitchTurnFromPlayer2ToPlayer1() {
        gameState.setCurrentTurn(Turn.PLAYER_2);
        gameState.switchTurn();

        assertEquals(Turn.PLAYER_1, gameState.getCurrentTurn());
    }

    // --- Rounds ---

    @Test
    void shouldAdvanceToNextRound() {
        gameState.nextRound();

        assertEquals(2, gameState.getCurrentRound());
    }

    @Test
    void shouldAdvanceToRound3() {
        gameState.nextRound();
        gameState.nextRound();

        assertEquals(3, gameState.getCurrentRound());
    }

    @Test
    void shouldThrowWhenExceedingMaxRounds() {
        gameState.nextRound();
        gameState.nextRound();

        assertThrows(IllegalStateException.class, () -> gameState.nextRound());
    }

    // --- Phase transitions (valid) ---

    @Test
    void shouldTransitionFromCoinFlipToRedraw() {
        gameState.setPhase(GamePhase.REDRAW);

        assertEquals(GamePhase.REDRAW, gameState.getPhase());
    }

    @Test
    void shouldTransitionFromRedrawToPlay() {
        gameState.setPhase(GamePhase.REDRAW);
        gameState.setPhase(GamePhase.PLAY);

        assertEquals(GamePhase.PLAY, gameState.getPhase());
    }

    @Test
    void shouldTransitionFromPlayToRoundEnd() {
        gameState.setPhase(GamePhase.REDRAW);
        gameState.setPhase(GamePhase.PLAY);
        gameState.setPhase(GamePhase.ROUND_END);

        assertEquals(GamePhase.ROUND_END, gameState.getPhase());
    }

    @Test
    void shouldTransitionFromRoundEndToRedraw() {
        gameState.setPhase(GamePhase.REDRAW);
        gameState.setPhase(GamePhase.PLAY);
        gameState.setPhase(GamePhase.ROUND_END);
        gameState.setPhase(GamePhase.REDRAW);

        assertEquals(GamePhase.REDRAW, gameState.getPhase());
    }

    @Test
    void shouldTransitionFromRoundEndToGameOver() {
        gameState.setPhase(GamePhase.REDRAW);
        gameState.setPhase(GamePhase.PLAY);
        gameState.setPhase(GamePhase.ROUND_END);
        gameState.setPhase(GamePhase.GAME_OVER);

        assertEquals(GamePhase.GAME_OVER, gameState.getPhase());
    }

    // --- Phase transitions (invalid) ---

    @Test
    void shouldThrowWhenSkippingFromCoinFlipToPlay() {
        assertThrows(IllegalStateException.class, () -> gameState.setPhase(GamePhase.PLAY));
    }

    @Test
    void shouldThrowWhenGoingBackFromRedrawToCoinFlip() {
        gameState.setPhase(GamePhase.REDRAW);

        assertThrows(IllegalStateException.class, () -> gameState.setPhase(GamePhase.COIN_FLIP));
    }

    @Test
    void shouldThrowWhenSkippingFromRedrawToRoundEnd() {
        gameState.setPhase(GamePhase.REDRAW);

        assertThrows(IllegalStateException.class, () -> gameState.setPhase(GamePhase.ROUND_END));
    }

    @Test
    void shouldThrowWhenTransitioningFromGameOver() {
        gameState.setPhase(GamePhase.REDRAW);
        gameState.setPhase(GamePhase.PLAY);
        gameState.setPhase(GamePhase.ROUND_END);
        gameState.setPhase(GamePhase.GAME_OVER);

        assertThrows(IllegalStateException.class, () -> gameState.setPhase(GamePhase.REDRAW));
    }

    @Test
    void shouldThrowWhenGoingFromPlayToRedraw() {
        gameState.setPhase(GamePhase.REDRAW);
        gameState.setPhase(GamePhase.PLAY);

        assertThrows(IllegalStateException.class, () -> gameState.setPhase(GamePhase.REDRAW));
    }
}
