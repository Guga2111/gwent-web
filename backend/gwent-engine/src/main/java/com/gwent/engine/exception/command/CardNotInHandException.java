package com.gwent.engine.exception.command;

public class CardNotInHandException extends InvalidCommandException {
    public CardNotInHandException() {
        super("Card is not in player's hand");
    }
}
