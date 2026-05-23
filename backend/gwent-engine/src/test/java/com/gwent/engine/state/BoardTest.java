package com.gwent.engine.state;

import com.gwent.engine.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BoardTest {

    private Board board;
    private Card weatherCard;

    @BeforeEach
    void setUp() {
        Card leader1 = new Card("foltest", "Foltest", Faction.NORTHERN_REALMS, CardType.LEADER,
                null, LeaderAbility.SIEGE_MASTER, null, null);
        Card leader2 = new Card("emhyr", "Emhyr", Faction.NILFGAARD, CardType.LEADER,
                null, LeaderAbility.EMPEROR_OF_NILFGAARD, null, null);

        PlayerState player1 = new PlayerState(leader1, List.of());
        PlayerState player2 = new PlayerState(leader2, List.of());

        board = new Board(player1, player2);
        weatherCard = new Card("frost", "Biting Frost", Faction.NEUTRAL, CardType.WEATHER,
                null, null, null, null);
    }

    // --- Initial state ---

    @Test
    void shouldStartWithNoWeatherCards() {
        assertTrue(board.getActiveWeatherCards().isEmpty());
    }

    @Test
    void shouldHaveBothPlayers() {
        assertNotNull(board.getPlayer1());
        assertNotNull(board.getPlayer2());
    }

    // --- Weather cards ---

    @Test
    void shouldAddWeatherCard() {
        board.addWeatherCard(weatherCard);

        assertEquals(1, board.getActiveWeatherCards().size());
        assertEquals(weatherCard, board.getActiveWeatherCards().get(0));
    }

    @Test
    void shouldAddMultipleWeatherCards() {
        Card fog = new Card("fog", "Impenetrable Fog", Faction.NEUTRAL, CardType.WEATHER,
                null, null, null, null);

        board.addWeatherCard(weatherCard);
        board.addWeatherCard(fog);

        assertEquals(2, board.getActiveWeatherCards().size());
    }

    @Test
    void shouldClearAllWeatherCards() {
        board.addWeatherCard(weatherCard);
        board.clearWeatherCards();

        assertTrue(board.getActiveWeatherCards().isEmpty());
    }

    @Test
    void shouldClearWhenNoWeatherCards() {
        board.clearWeatherCards();

        assertTrue(board.getActiveWeatherCards().isEmpty());
    }

    @Test
    void shouldReturnUnmodifiableWeatherCards() {
        assertThrows(UnsupportedOperationException.class, () ->
                board.getActiveWeatherCards().add(weatherCard));
    }
}
