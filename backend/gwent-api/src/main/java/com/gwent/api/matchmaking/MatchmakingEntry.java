package com.gwent.api.matchmaking;

import java.util.UUID;

public record MatchmakingEntry(String playerEmail, UUID deckId) {}
