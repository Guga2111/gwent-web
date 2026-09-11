package com.gwent.engine.command;

import com.gwent.engine.domain.Card;

public record ResolveDummyCommand(Card card) implements GameCommand {
}
