package com.gwent.engine.exception.state;

import com.gwent.engine.domain.GamePhase;

public class InvalidPhaseTransitionException extends InvalidStateTransitionException {
    public InvalidPhaseTransitionException(GamePhase from, GamePhase to) {
        super("Cannot transition from " + from + " to " + to);
    }
}
