package com.gwent.engine.exception.state;

public class RoundLimitExceededException extends InvalidStateTransitionException {
    public RoundLimitExceededException() {
        super("Cannot exceed 3 rounds");
    }
}
