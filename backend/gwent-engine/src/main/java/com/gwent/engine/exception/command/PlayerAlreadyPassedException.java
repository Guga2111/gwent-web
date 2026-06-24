package com.gwent.engine.exception.command;

public class PlayerAlreadyPassedException extends InvalidCommandException {
    public PlayerAlreadyPassedException() {
        super("Player has already passed this round");
    }
}
