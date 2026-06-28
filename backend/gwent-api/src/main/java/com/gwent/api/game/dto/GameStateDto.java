package com.gwent.api.game.dto;

import java.util.UUID;

public record GameStateDto (
        UUID gameId,
        String phase,
        String currentTurn,
        String pendingAbility,
        int currentRound,
        PlayerStateDto player1,
        PlayerStateDto player2
) {}
