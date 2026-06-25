package com.gwent.engine.command;

import com.gwent.engine.domain.Card;
import com.gwent.engine.domain.Turn;

public record MulliganCommand(Turn player, Card cardToReturn) implements GameCommand {}
