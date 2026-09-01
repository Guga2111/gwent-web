package com.gwent.api.game.dto;

public record CardDto(
        String id,
        String name,
        Integer basePower,
        Integer currentPower,
        String cardType,
        String rowType,
        String ability,
        String faction,
        String leaderAbility
) {}
