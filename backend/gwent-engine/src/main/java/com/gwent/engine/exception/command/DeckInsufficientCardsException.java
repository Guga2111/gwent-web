package com.gwent.engine.exception.command;

public class DeckInsufficientCardsException extends InvalidCommandException {
    public DeckInsufficientCardsException() {
        super("Does not have any cards in deck");
    }
}
