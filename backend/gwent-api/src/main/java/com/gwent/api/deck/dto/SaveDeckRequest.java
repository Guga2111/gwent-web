package com.gwent.api.deck.dto;

import com.gwent.engine.domain.Faction;

import java.util.List;

public record SaveDeckRequest(String name, Faction faction, String leaderId, List<DeckCardEntryDto> cards) {}
