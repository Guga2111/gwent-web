package com.gwent.engine.exception.command;

public class CardNotInGraveyardException extends InvalidCommandException {
    public CardNotInGraveyardException() {
        super("Card is not in player's graveyard");
    }
}