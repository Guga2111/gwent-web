package com.gwent.api.matchmaking;

public class AlreadyInQueueException extends RuntimeException {
    public AlreadyInQueueException() {
        super("Player is already in the matchmaking queue");
    }
}
