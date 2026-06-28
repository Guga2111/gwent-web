package com.gwent.api.game.dto;

public record CommandRequestDto (
        String playerId,
        String commandType, // PLAY_CARD, PASS, MULLIGAN, USE_LEADER, RESOLVE_MEDIC, CONFIRM_MULLIGAN
        String cardId, // nullable - just for commands that use cards
        String targetRow // nullable - just for PLAY_CARD
) {}
