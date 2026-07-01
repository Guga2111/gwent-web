package com.gwent.api.game.dto;

public record CardDto(
        String id,
        String name,
        Integer basePower,
        String cardType,
        String rowType,
        String ability,
        String faction
) {}
