package com.gwent.engine.exception.command;

public class InvalidRowException extends InvalidCommandException {
    public InvalidRowException() {
        super("Card cannot be placed on the target row");
    }
}
