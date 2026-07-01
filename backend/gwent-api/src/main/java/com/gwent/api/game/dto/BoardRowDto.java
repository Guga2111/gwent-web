package com.gwent.api.game.dto;

import java.util.List;

public record BoardRowDto(
        List<CardDto> cards,
        boolean hornActive,
        boolean weatherActive
) {}