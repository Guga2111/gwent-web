package com.gwent.engine.command;

import com.gwent.engine.domain.Turn;

public record ResolveScoiataelCommand(Turn chosenFirstPlayer) implements GameCommand {
}
