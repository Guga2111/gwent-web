package com.gwent.api.game.dto;

import java.util.List;

public record OpponentStateDto(
        String playerId,
        int lives,
        int score,
        boolean passed,
        boolean leaderUsed,
        CardDto leader,
        int handSize,
        int deckSize,
        BoardRowDto meleeRow,
        BoardRowDto rangedRow,
        BoardRowDto siegeRow,
        List<CardDto> graveyard
) {}
