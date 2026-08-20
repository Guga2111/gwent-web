package com.gwent.api.catalog.dto;

import com.gwent.engine.domain.*;

public record CatalogCardDto(
        String id,
        String name,
        Faction faction,
        CardType cardType,
        Ability ability,
        LeaderAbility leaderAbility,
        RowType rowType,
        Integer basePower,
        int deckCopies
) {}
