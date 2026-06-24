package com.gwent.engine.exception.command;

public class WrongTurnException extends InvalidCommandException {
    public WrongTurnException() {
        super("It is not your turn");
    }
}
