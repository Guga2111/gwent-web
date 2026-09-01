package com.gwent.engine.exception.command;

public class CardNotInDeckException extends InvalidCommandException {
    public CardNotInDeckException() {
        super("Card is not in player's deck");
    }
}
