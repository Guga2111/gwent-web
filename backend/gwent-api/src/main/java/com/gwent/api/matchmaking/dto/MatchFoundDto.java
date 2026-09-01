package com.gwent.api.matchmaking.dto;

import java.util.UUID;

public record MatchFoundDto(String type, UUID gameId) {
    public MatchFoundDto(UUID gameId) {
        this("MATCH_FOUND", gameId);
    }
}
