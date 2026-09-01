package com.gwent.api.game.exception;

import java.util.UUID;

public class GameNotWaitingException extends RuntimeException {
    public GameNotWaitingException(UUID gameId) {
        super("Game " + gameId + " is not waiting for players.");
    }
}