package com.gwent.api.deck.dto;

import com.gwent.engine.domain.Faction;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record DeckDto(
        UUID id,
        String name,
        Faction faction,
        String leaderId,
        List<DeckCardEntryDto> cards,
        LocalDateTime createdAt
) {}
