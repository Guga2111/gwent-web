package com.gwent.engine.command;

import com.gwent.engine.domain.Turn;

public record ConfirmMulliganCommand(Turn player) implements GameCommand {
}
