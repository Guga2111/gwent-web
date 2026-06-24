package com.gwent.engine.exception.command;

public class LeaderAlreadyUsedException extends InvalidCommandException {
    public LeaderAlreadyUsedException() {
        super("Leader ability has already been used");
    }
}
