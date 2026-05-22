package com.gwent.engine.domain;

import java.util.Objects;

public record Card(
        String id, String name,
        Faction faction, CardType cardType,
        Ability ability, LeaderAbility leaderAbility,
        RowType rowType, Integer basePower
) {
    public Card {
        Objects.requireNonNull(id);
        Objects.requireNonNull(name);
        id = id.trim().replaceAll("\\s+", "_").toUpperCase();

        if (cardType == CardType.LEADER) {
            if (leaderAbility == null) throw new IllegalArgumentException("Leader cards must have a leader ability");
            if (ability != null) throw new IllegalArgumentException("Leader cards cannot have a unit ability");
        } else {
            if (leaderAbility != null) throw new IllegalArgumentException("Only leader cards can have a leader ability");
        }

        if (cardType == CardType.UNIT || cardType == CardType.HERO) {
            if (rowType == null) throw new IllegalArgumentException("Unit and hero cards must have a row type");
            if (basePower == null) throw new IllegalArgumentException("Unit and hero cards must have base power");
        } else {
            if (rowType != null) throw new IllegalArgumentException("Non-unit cards cannot have a row type");
        }
    }
}
