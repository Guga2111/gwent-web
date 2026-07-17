package com.gwent.api.game.dto;

import java.util.List;
import java.util.UUID;

public record GameStateDto(
        UUID gameId,
        String phase,
        String currentTurn,
        String myTurn,
        String pendingAbility,
        int currentRound,
        List<CardDto> weatherCards,
        PlayerStateDto me,
        OpponentStateDto opponent,
        String winner,
        String endReason,
        List<CardDto> revealedCards
) {}