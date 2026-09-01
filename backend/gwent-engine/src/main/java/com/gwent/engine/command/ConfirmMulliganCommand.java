package com.gwent.engine.command;

import com.gwent.engine.domain.Card;
import com.gwent.engine.domain.Turn;

import java.util.List;

public record ConfirmMulliganCommand(Turn player, List<Card> cardsToReturn) implements GameCommand {
}
