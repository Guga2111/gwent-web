package com.gwent.api.game.dto;

import java.util.UUID;

public record CreateGameDto(UUID gameId, String playerId) {}
