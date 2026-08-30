package com.gwent.api.matchmaking.dto;

public record MatchmakingTimeoutDto(String type) {
    public MatchmakingTimeoutDto() {
        this("TIMEOUT");
    }
}
