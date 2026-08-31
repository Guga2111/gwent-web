package com.gwent.api.shared;

import com.gwent.api.deck.exception.DeckNotFoundException;
import com.gwent.api.game.dto.ErrorDto;
import com.gwent.api.game.exception.CardNotFoundException;
import com.gwent.api.game.exception.GameNotFoundException;
import com.gwent.api.game.exception.GameNotWaitingException;
import com.gwent.api.game.exception.PlayerNotInGameException;
import com.gwent.api.shared.exception.GlobalExceptionHandler;
import com.gwent.engine.exception.GwentException;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void shouldReturn404_forGameNotFoundException() {
        UUID gameId = UUID.randomUUID();
        ResponseEntity<ErrorDto> response = handler.handleGameNotFound(new GameNotFoundException(gameId));

        assertEquals(404, response.getStatusCode().value());
        assertEquals("GAME_NOT_FOUND", response.getBody().error());
    }

    @Test
    void shouldReturn422_forCardNotFoundException() {
        ResponseEntity<ErrorDto> response = handler.handleCardNotFound(new CardNotFoundException("card_1"));

        assertEquals(422, response.getStatusCode().value());
        assertEquals("CARD_NOT_FOUND", response.getBody().error());
    }

    @Test
    void shouldReturn422_forGwentException() {
        GwentException ex = new GwentException("test rule violation") {};
        ResponseEntity<ErrorDto> response = handler.handleGwentException(ex);

        assertEquals(422, response.getStatusCode().value());
        assertEquals("GAME_RULE_VIOLATION", response.getBody().error());
    }

    @Test
    void shouldReturn409_forGameNotWaitingException() {
        UUID gameId = UUID.randomUUID();
        ResponseEntity<ErrorDto> response = handler.handleGameNotWaiting(new GameNotWaitingException(gameId));

        assertEquals(409, response.getStatusCode().value());
        assertEquals("GAME_NOT_WAITING", response.getBody().error());
    }

    @Test
    void shouldReturn403_forPlayerNotInGameException() {
        ResponseEntity<ErrorDto> response = handler.handlePlayerNotInGame(new PlayerNotInGameException("user1"));

        assertEquals(403, response.getStatusCode().value());
        assertEquals("PLAYER_NOT_IN_GAME", response.getBody().error());
    }

    @Test
    void shouldReturn404_forDeckNotFoundException() {
        UUID deckId = UUID.randomUUID();
        ResponseEntity<ErrorDto> response = handler.handleDeckNotFound(new DeckNotFoundException(deckId));

        assertEquals(404, response.getStatusCode().value());
        assertEquals("DECK_NOT_FOUND", response.getBody().error());
    }

    @Test
    void shouldReturn400_forIllegalArgumentException() {
        ResponseEntity<ErrorDto> response = handler.handleIllegalArgument(new IllegalArgumentException("bad input"));

        assertEquals(400, response.getStatusCode().value());
        assertEquals("INVALID_REQUEST", response.getBody().error());
    }

    @Test
    void shouldIncludeExceptionMessage_inErrorDto() {
        ResponseEntity<ErrorDto> response = handler.handleIllegalArgument(new IllegalArgumentException("specific message"));

        assertEquals("specific message", response.getBody().message());
    }
}
