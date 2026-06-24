package com.gwent.engine.exception.state;

public class PlayerAlreadyEliminatedException extends InvalidStateTransitionException {
    public PlayerAlreadyEliminatedException() {
        super("Player is already eliminated");
    }
}
