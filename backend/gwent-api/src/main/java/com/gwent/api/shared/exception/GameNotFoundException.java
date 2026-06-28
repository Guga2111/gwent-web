package com.gwent.api.shared.exception;

import java.util.UUID;

public class GameNotFoundException extends RuntimeException {
    public GameNotFoundException(UUID gameId) {
        super("The game id: " + gameId + " does not exist.");
    }
}
