package com.gwent.engine.command;

import com.gwent.engine.domain.Card;

public record ResolveMedicCommand(Card card) implements GameCommand {
}
