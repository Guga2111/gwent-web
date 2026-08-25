package com.gwent.engine.exception.command;

public class WrongFactionException extends InvalidCommandException {
    public WrongFactionException () {
        super("Wrong faction.");
    }
}
