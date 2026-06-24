package com.gwent.engine.exception.command;

import com.gwent.engine.domain.GamePhase;

public class InvalidPhaseCommandException extends InvalidCommandException {
    public InvalidPhaseCommandException(GamePhase expected, GamePhase actual) {
        super("Command requires phase " + expected + " but current phase is " + actual);
    }
}
