package com.gwent.api.game;

import com.gwent.api.game.dto.*;
import com.gwent.engine.domain.Card;
import com.gwent.engine.domain.Turn;
import com.gwent.engine.state.BoardRow;
import com.gwent.engine.state.GameState;
import com.gwent.engine.state.PlayerState;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class GameModelMapper {

    public GameStateDto toGameStateDto(UUID gameId, SessionContext ctx, Turn perspective, int meScore, int opponentScore) {
        GameState state = ctx.gameState();
        String meId = perspective == Turn.PLAYER_1 ? ctx.player1Id() : ctx.player2Id();
        String opponentId = perspective == Turn.PLAYER_1 ? ctx.player2Id() : ctx.player1Id();
        PlayerState meState = state.getPlayer(perspective);
        PlayerState opponentState = state.getPlayer(perspective == Turn.PLAYER_1 ? Turn.PLAYER_2 : Turn.PLAYER_1);

        return new GameStateDto(
                gameId,
                state.getPhase().name(),
                state.getCurrentTurn().name(),
                perspective.name(),
                state.getPendingAbility() != null ? state.getPendingAbility().name() : null,
                state.getCurrentRound(),
                state.getBoard().getActiveWeatherCards().stream().map(this::toCardDto).toList(),
                toPlayerDto(meId, meState, meScore),
                toOpponentDto(opponentId, opponentState, opponentScore)
        );
    }

    public PlayerStateDto toPlayerDto(String playerId, PlayerState player, int score) {
        return new PlayerStateDto(
                playerId,
                player.getLives(),
                score,
                player.isPassed(),
                player.isLeaderUsed(),
                toCardDto(player.getLeader()),
                player.getMulligansRemaining(),
                player.isMulliganConfirmed(),
                player.getHand().stream().map(this::toCardDto).toList(),
                player.getDeck().size(),
                toBoardRowDto(player.getMeleeRow()),
                toBoardRowDto(player.getRangedRow()),
                toBoardRowDto(player.getSiegeRow()),
                player.getGraveyard().stream().map(this::toCardDto).toList()
        );
    }

    public OpponentStateDto toOpponentDto(String playerId, PlayerState player, int score) {
        return new OpponentStateDto(
                playerId,
                player.getLives(),
                score,
                player.isPassed(),
                player.isLeaderUsed(),
                toCardDto(player.getLeader()),
                player.getHand().size(),
                player.getDeck().size(),
                toBoardRowDto(player.getMeleeRow()),
                toBoardRowDto(player.getRangedRow()),
                toBoardRowDto(player.getSiegeRow()),
                player.getGraveyard().stream().map(this::toCardDto).toList()
        );
    }

    public CardDto toCardDto(Card card) {
        return new CardDto(
                card.id(),
                card.name(),
                card.basePower(),
                card.cardType().name(),
                card.rowType() != null ? card.rowType().name() : null,
                card.ability() != null ? card.ability().name() : null,
                card.faction().name()
        );
    }

    public BoardRowDto toBoardRowDto(BoardRow row) {
        return new BoardRowDto(
                row.getCards().stream().map(this::toCardDto).toList(),
                row.isHornActive(),
                row.isWeatherActive()
        );
    }
}
