package com.gwent.engine.command;

import com.gwent.engine.domain.Card;

public record ResolveLeaderCommand(Card card) implements GameCommand {}
