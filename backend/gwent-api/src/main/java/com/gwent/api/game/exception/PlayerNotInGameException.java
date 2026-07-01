package com.gwent.api.game.exception;

public class PlayerNotInGameException extends RuntimeException {
    public PlayerNotInGameException(String userId) {
        super("User " + userId + " is not a player in this game.");
    }
}