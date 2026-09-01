package com.gwent.api.game.dto;

import java.util.List;

public record PlayerStateDto(
        String playerId,
        int lives,
        int score,
        boolean passed,
        boolean leaderUsed,
        CardDto leader,
        int mulligansRemaining,
        boolean mulliganConfirmed,
        List<CardDto> hand,
        int deckSize,
        BoardRowDto meleeRow,
        BoardRowDto rangedRow,
        BoardRowDto siegeRow,
        List<CardDto> graveyard
) {}
