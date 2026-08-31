package com.gwent.api.game;

import com.gwent.api.game.dto.*;
import com.gwent.api.game.exception.CardNotFoundException;
import com.gwent.engine.command.*;
import com.gwent.engine.core.ScoreCalculator;
import com.gwent.engine.domain.*;
import com.gwent.engine.state.BoardRow;
import com.gwent.engine.state.GameState;
import com.gwent.engine.state.PlayerState;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class GameModelMapper {

    private final ScoreCalculator scoreCalculator = new ScoreCalculator();

    public GameStateDto toGameStateDto(UUID gameId, SessionContext ctx, Turn perspective, int meScore, int opponentScore, Long turnDeadlines, Long abilityDeadlineUtc, boolean disconnectForfeit) {
        GameState state = ctx.gameState();
        String meId = perspective == Turn.PLAYER_1 ? ctx.player1Id() : ctx.player2Id();
        String opponentId = perspective == Turn.PLAYER_1 ? ctx.player2Id() : ctx.player1Id();
        PlayerState meState = state.getPlayer(perspective);
        PlayerState opponentState = state.getPlayer(perspective == Turn.PLAYER_1 ? Turn.PLAYER_2 : Turn.PLAYER_1);
        if (state.getPhase() != GamePhase.PLAY || state.getPendingAbility() != null || state.isGameOver()) turnDeadlines = null;
        if (state.isGameOver()) abilityDeadlineUtc = null;

        return new GameStateDto(
                gameId,
                state.getPhase().name(),
                state.getCurrentTurn().name(),
                perspective.name(),
                state.getPendingAbility() != null ? state.getPendingAbility().name() : null,
                state.getCurrentRound(),
                state.getBoard().getActiveWeatherCards().stream().map(this::toCardDto).toList(),
                toPlayerDto(meId, meState, meScore),
                toOpponentDto(opponentId, opponentState, opponentScore),
                state.getWinner() != null
                        ? (state.getWinner() == Turn.PLAYER_1 ? ctx.player1Id() : ctx.player2Id())
                        : null,
                state.getEndReason() != null ? state.getEndReason().name() : null,
                resolveRevealedCards(state, perspective),
                resolveDeckCards(state, perspective, meState),
                turnDeadlines,
                abilityDeadlineUtc,
                disconnectForfeit
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
                null,
                card.cardType().name(),
                card.rowType() != null ? card.rowType().name() : null,
                card.ability() != null ? card.ability().name() : null,
                card.faction().name(),
                card.leaderAbility() != null ? card.leaderAbility().name() : null
        );
    }

    private CardDto toCardDtoOnBoard(Card card, BoardRow row) {
        return new CardDto(
                card.id(),
                card.name(),
                card.basePower(),
                scoreCalculator.calculateCardPower(card, row),
                card.cardType().name(),
                card.rowType() != null ? card.rowType().name() : null,
                card.ability() != null ? card.ability().name() : null,
                card.faction().name(),
                null
        );
    }

    public BoardRowDto toBoardRowDto(BoardRow row) {
        return new BoardRowDto(
                row.getCards().stream().map(c -> toCardDtoOnBoard(c, row)).toList(),
                row.isHornActive(),
                row.isWeatherActive()
        );
    }

    public GameCommand toCommand(CommandRequestDto request, Turn player, GameState state, SessionContext ctx) {
        return switch (request.commandType()) {
            case PASS             -> new PassCommand();
            case USE_LEADER       -> new UseLeaderCommand();
            case CONFIRM_MULLIGAN -> new ConfirmMulliganCommand(player,
                    request.cardIds() != null
                            ? request.cardIds().stream().map(id -> findCard(id, state.getPlayer(player).getHand())).toList()
                            : List.of());
            case PLAY_CARD        -> new PlayCardCommand(
                    findCard(request.cardId(), state.getPlayer(player).getHand()),
                    RowType.valueOf(request.targetRow()));
            case MULLIGAN         -> new MulliganCommand(
                    player,
                    findCard(request.cardId(), state.getPlayer(player).getHand()));
            case RESOLVE_MEDIC    -> new ResolveMedicCommand(
                    findCard(request.cardId(), state.getPlayer(player).getGraveyard()));
            case RESOLVE_LEADER   -> {
                Card card = switch (state.getPendingAbility()) {
                    case LEADER_GRAVEYARD_PICK -> findCard(request.cardId(), state.getPlayer(player).getGraveyard());
                    case LEADER_OPPONENT_GRAVEYARD_PICK -> {
                        Turn opponentTurn = player == Turn.PLAYER_1 ? Turn.PLAYER_2 : Turn.PLAYER_1;
                        yield findCard(request.cardId(), state.getPlayer(opponentTurn).getGraveyard());
                    }
                    case LEADER_DECK_PICK -> findCardInDeck(request.cardId(), state.getPlayer(player));
                    case LEADER_HAND_DISCARD -> findCard(request.cardId(), state.getPlayer(player).getHand());
                    default -> throw new IllegalStateException("No pending leader ability");
                };
                yield new ResolveLeaderCommand(card);
            }
            case RESOLVE_SCOIATAEL -> {
                Turn chosen;
                if (request.chosenPlayerId().equals(ctx.player1Id())) {
                    chosen = Turn.PLAYER_1;
                } else if (request.chosenPlayerId().equals(ctx.player2Id())) {
                    chosen = Turn.PLAYER_2;
                } else {
                    throw new IllegalArgumentException("Unknown player id: " + request.chosenPlayerId());
                }
                yield new ResolveScoiataelCommand(chosen);
            }
        };
    }

    private Card findCard(String cardId, List<Card> cards) {
        return cards.stream()
                .filter(c -> c.id().equals(cardId))
                .findFirst()
                .orElseThrow(() -> new CardNotFoundException(cardId));
    }

    private Card findCardInDeck(String cardId, PlayerState player) {
        return player.getDeck().stream()
                .filter(c -> c.id().equals(cardId))
                .findFirst()
                .orElseThrow(() -> new CardNotFoundException(cardId));
    }

    private List<CardDto> resolveDeckCards(GameState state, Turn perspective, PlayerState meState) {
        if (state.getPendingAbility() != PendingAbility.LEADER_DECK_PICK) return null;
        if (state.getCurrentTurn() != perspective) return null;
        return meState.getDeck().stream().map(this::toCardDto).toList();
    }

    private List<CardDto> resolveRevealedCards(GameState state, Turn perspective) {
        if (state.getRevealedCards() == null || state.getRevealedCards().isEmpty()) return null;
        // Only show revealed cards to the current player (the one who used the leader)
        if (state.getCurrentTurn() != perspective) return null;
        return state.getRevealedCards().stream().map(this::toCardDto).toList();
    }
}
