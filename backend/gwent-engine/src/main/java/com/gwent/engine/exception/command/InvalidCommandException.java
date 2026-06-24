package com.gwent.engine.exception.command;

import com.gwent.engine.exception.GwentException;

public abstract class InvalidCommandException extends GwentException {
    protected InvalidCommandException(String message) {
        super(message);
    }
}
