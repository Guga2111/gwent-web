package com.gwent.api.shared.exception;

import com.gwent.api.game.dto.ErrorDto;
import com.gwent.api.game.exception.CardNotFoundException;
import com.gwent.api.game.exception.GameNotFoundException;
import com.gwent.api.game.exception.GameNotWaitingException;
import com.gwent.api.game.exception.PlayerNotInGameException;
import com.gwent.engine.exception.GwentException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(GameNotFoundException.class)
    public ResponseEntity<ErrorDto> handleGameNotFound(GameNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorDto("GAME_NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(CardNotFoundException.class)
    public ResponseEntity<ErrorDto> handleCardNotFound(CardNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new ErrorDto("CARD_NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(GwentException.class)
    public ResponseEntity<ErrorDto> handleGwentException(GwentException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new ErrorDto("GAME_RULE_VIOLATION", ex.getMessage()));
    }

    @ExceptionHandler(GameNotWaitingException.class)
    public ResponseEntity<ErrorDto> handleGameNotWaiting(GameNotWaitingException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorDto("GAME_NOT_WAITING", ex.getMessage()));
    }

    @ExceptionHandler(PlayerNotInGameException.class)
    public ResponseEntity<ErrorDto> handlePlayerNotInGame(PlayerNotInGameException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorDto("PLAYER_NOT_IN_GAME", ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorDto> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorDto("INVALID_REQUEST", ex.getMessage()));
    }
}
