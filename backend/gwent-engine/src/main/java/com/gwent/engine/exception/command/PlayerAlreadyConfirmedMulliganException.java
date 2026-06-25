package com.gwent.engine.exception.command;

public class PlayerAlreadyConfirmedMulliganException extends InvalidCommandException {
    public PlayerAlreadyConfirmedMulliganException() {
        super("Player already confirmed the redraw");
    }
}
