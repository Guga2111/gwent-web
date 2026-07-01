package com.gwent.api.game.dto;

public record CommandRequestDto (
        String playerId,
        CommandType commandType,
        String cardId, // nullable - just for commands that use cards
        String targetRow // nullable - just for PLAY_CARD
) {}
