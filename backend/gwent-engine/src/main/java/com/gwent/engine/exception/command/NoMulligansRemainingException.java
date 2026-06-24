package com.gwent.engine.exception.command;

public class NoMulligansRemainingException extends InvalidCommandException {
    public NoMulligansRemainingException() {
        super("No mulligans remaining for this player");
    }
}
