package com.gwent.engine.exception.command;

public class InvalidLeaderPickException extends InvalidCommandException {
    public InvalidLeaderPickException() {
        super("Card cannot be picked for this leader ability");
    }
}
