package com.gwent.engine.command;

import com.gwent.engine.domain.Card;
import com.gwent.engine.domain.RowType;

public record PlayCardCommand(Card card, RowType targetRow) implements GameCommand {

}
