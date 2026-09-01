package com.gwent.api.game.dto;

public record PresenceDto(String playerEmail, boolean connected, Long forfeitDeadlineUtc) {}
