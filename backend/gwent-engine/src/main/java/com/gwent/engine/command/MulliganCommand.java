package com.gwent.engine.command;

import com.gwent.engine.domain.Card;

public record MulliganCommand(Card cardToReturn) implements GameCommand {}
