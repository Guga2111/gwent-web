package com.gwent.api.game.exception;

public class CardNotFoundException extends RuntimeException {
    public CardNotFoundException(String cardId) {
        super("The card with id: " + cardId + " does not exist.");
    }
}
