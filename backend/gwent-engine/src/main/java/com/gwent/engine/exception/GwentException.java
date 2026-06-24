package com.gwent.engine.exception;

public abstract class GwentException extends RuntimeException {
    protected GwentException(String message) {
        super(message);
    }
}