package com.gwent.engine.exception.state;

public class TurnNotSetException extends InvalidStateTransitionException {
    public TurnNotSetException() {
        super("Current turn has not been set");
    }
}
