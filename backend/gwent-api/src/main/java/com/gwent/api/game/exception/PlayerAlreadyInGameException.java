package com.gwent.api.game.exception;

public class PlayerAlreadyInGameException extends RuntimeException {
    public PlayerAlreadyInGameException(String userId) {
        super("The player with id: " + userId + " is already in the game.");
    }
}
