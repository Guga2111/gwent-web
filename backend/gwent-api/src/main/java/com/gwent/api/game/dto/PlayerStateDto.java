package com.gwent.api.game.dto;

import java.util.List;

public record PlayerStateDto (
        String playerId,
        int lives,
        int score,
        boolean passed,
        boolean leaderUsed,
        int mulligansRemaining,
        boolean mulliganConfirmed,
        List<String> handCardIds,
        List<String> meleeRowCardIds,
        List<String> rangedRowCardIds,
        List<String> siegeRowCardIds,
        List<String> graveyardCardIds
) {}
