package com.gwent.engine.exception.state;

import com.gwent.engine.exception.GwentException;

public abstract class InvalidStateTransitionException extends GwentException {
    protected InvalidStateTransitionException(String message) {
        super(message);
    }
}
