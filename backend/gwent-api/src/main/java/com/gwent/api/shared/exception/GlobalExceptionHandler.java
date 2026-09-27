package com.gwent.api.shared.exception;

import com.gwent.api.deck.exception.DeckNotFoundException;
import com.gwent.api.game.dto.ErrorDto;
import com.gwent.api.game.exception.*;
import com.gwent.api.user.DuplicateUserException;
import com.gwent.api.user.UserNotFoundException;
import com.gwent.engine.exception.GwentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

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

    @ExceptionHandler(DeckNotFoundException.class)
    public ResponseEntity<ErrorDto> handleDeckNotFound(DeckNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorDto("DECK_NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorDto> handleUserNotFound(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorDto("USER_NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(PlayerAlreadyInGameException.class)
    public ResponseEntity<ErrorDto> handlePlayerAlreadyInGame (PlayerAlreadyInGameException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorDto("PLAYER_ALREADY_IN_GAME", ex.getMessage()));
    }

    @ExceptionHandler(DuplicateUserException.class)
    public ResponseEntity<ErrorDto> handleDuplicateUser(DuplicateUserException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorDto("DUPLICATE_USER", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        fe -> fe.getField(),
                        fe -> fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "Invalid value",
                        (a, b) -> a
                ));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "VALIDATION_ERROR", "fields", fieldErrors));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorDto> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("IllegalArgumentException: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorDto("INVALID_REQUEST", ex.getMessage()));
    }

    @ExceptionHandler({MissingServletRequestParameterException.class, MethodArgumentTypeMismatchException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<ErrorDto> handleBadRequest(Exception ex) {
        log.warn("Bad request: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorDto("BAD_REQUEST", "Invalid request"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDto> handleGenericException(Exception ex) {
        log.error("Unhandled exception", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorDto("INTERNAL_ERROR", "An unexpected error occurred"));
    }
}
