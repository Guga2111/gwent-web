package com.gwent.api.game;

import com.gwent.engine.state.GameState;

public record SessionContext(GameState gameState, String player1Id, String player2Id) {}