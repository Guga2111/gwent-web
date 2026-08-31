package com.gwent.api.game;

import com.gwent.api.game.dto.*;
import com.gwent.api.game.service.*;
import com.gwent.engine.command.PassCommand;
import com.gwent.engine.command.ResolveLeaderCommand;
import com.gwent.engine.command.ResolveMedicCommand;
import com.gwent.engine.command.ResolveScoiataelCommand;
import com.gwent.engine.core.GwentEngine;
import com.gwent.engine.domain.*;
import com.gwent.engine.exception.command.InvalidPhaseCommandException;
import com.gwent.engine.state.GameState;
import com.gwent.engine.state.PlayerState;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class GameSessionService {

    private final GwentEngine engine = new GwentEngine();

    private final GameModelMapper mapper;
    private final GameDeckBuilder gameDeckBuilder;
    private final GamePersistenceService persistenceService;
    private final GameBroadcastService broadcastService;
    private final GameTimerService timerService;
    private final GameSessionRegistry sessionRegistry;

    public GameSessionService(GameBroadcastService gameBroadcastService, GameModelMapper mapper, GameDeckBuilder gameDeckBuilder, GamePersistenceService gamePersistenceService, GameTimerService gameTimerService, GameSessionRegistry gameSessionRegistry) {
        this.broadcastService = gameBroadcastService;
        this.mapper = mapper;
        this.gameDeckBuilder = gameDeckBuilder;
        this.persistenceService = gamePersistenceService;
        this.sessionRegistry = gameSessionRegistry;
        this.timerService = gameTimerService;
    }

    public CreateGameDto createSession (String userId, UUID deckId) {
        return persistenceService.createGame(userId, deckId);
    }

    public GameStateDto joinSession (UUID gameId, String userId, UUID deckId) {
        return sessionRegistry.executeWithInitLock(gameId, () -> {
            Game game = persistenceService.getGameValidatingWaitingStatus(gameId);

            persistenceService.saveJoinedGame(gameId, userId, deckId);

            PlayerState player1 = new PlayerState(gameDeckBuilder.buildLeaderFromDeckId(game.getPlayer1DeckId()), gameDeckBuilder.buildDeckFromDeckId(game.getPlayer1DeckId()));
            PlayerState player2 = new PlayerState(gameDeckBuilder.buildLeaderFromDeckId(deckId), gameDeckBuilder.buildDeckFromDeckId(deckId));
            GameState gameState = new GameState(player1, player2);

            engine.drawInitialCards(gameState, 10);
            engine.resolveCoinFlip(gameState, Turn.PLAYER_1);

            SessionContext ctx = new SessionContext(gameState, game.getPlayer1Id(), userId);
            sessionRegistry.putSession(gameId, ctx);

            if (gameState.getPhase() == GamePhase.REDRAW) {
                scheduleMulliganTimeout(gameId, ctx);
            } else {
                scheduleScoiataelTimeout(gameId, ctx);
            }

            broadcastAndPersist(gameId, ctx);

            return toDto(gameId, ctx, Turn.PLAYER_2);
        });
    }

    public GameStateDto execute (UUID gameId, String userId, CommandRequestDto request) {
        return sessionRegistry.executeWithLock(gameId, ctx -> {
            Turn player = sessionRegistry.resolvePlayer(ctx, userId);
            Turn currentTurn = ctx.gameState().getCurrentTurn();

            engine.execute(ctx.gameState(), mapper.toCommand(request, player, ctx.gameState(), ctx));
            Turn afterExecutionTurn = ctx.gameState().getCurrentTurn();

            if (request.commandType() == CommandType.RESOLVE_SCOIATAEL) {
                timerService.cancelScoiataelTimer(gameId);
                scheduleMulliganTimeout(gameId, ctx);
            }

            updateTimersAfterExecution(gameId, ctx, currentTurn, afterExecutionTurn);

            if (ctx.gameState().isGameOver()) {
                timerService.cancelTurnTimer(gameId);
                timerService.cancelDisconnectTimersForGame(gameId, ctx.player1Id(), ctx.player2Id());
            }

            broadcastAndPersist(gameId, ctx);

            return toDto(gameId, ctx, player);
        });
    }

    public GameStateDto getSession (UUID gameId, String userId) {
        Optional<SessionContext> optCtx = sessionRegistry.getSession(gameId);
        if (optCtx.isPresent()) {
            SessionContext ctx = optCtx.get();
            Turn perspective = sessionRegistry.resolvePlayer(ctx, userId);
            return toDto(gameId, ctx, perspective);
        }

        return persistenceService.getPersistedState(gameId);
    }

    public Optional<ActiveGameDto> getActiveGame (String userId) {
        return persistenceService.findActiveGameForPlayer(userId);
    }

    public void surrender(UUID gameId, String userId) {
        sessionRegistry.executeWithLockVoid(gameId, ctx -> {
            if (ctx.gameState().isGameOver()) throw new InvalidPhaseCommandException(GamePhase.PLAY, GamePhase.GAME_OVER);

            Turn player = sessionRegistry.resolvePlayer(ctx, userId);
            engine.surrender(ctx.gameState(), player);

            timerService.cancelAllGameTimers(gameId, ctx.player1Id(), ctx.player2Id());
            broadcastAndPersist(gameId, ctx);
        });
    }

    // --- Disconnect ---
    public void scheduleDisconnectForfeit (UUID gameId, String playerEmail) {
        timerService.scheduleDisconnectForfeit(gameId, playerEmail, () -> {
            sessionRegistry.addDisconnectForfeit(gameId);
            try {
                surrender(gameId, playerEmail);
            } catch (InvalidPhaseCommandException e) {
                sessionRegistry.removeDisconnectForfeit(gameId);
            }
        });
    }

    public void cancelDisconnectForfeit (UUID gameId, String playerEmail) {
        timerService.cancelDisconnectForfeit(gameId, playerEmail);
    }

    // --- Broadcast & Timeout ---

    private void broadcastAndPersist (UUID gameId, SessionContext ctx) {
        GameStateDto p1Dto = toDto(gameId, ctx, Turn.PLAYER_1);
        GameStateDto p2Dto = toDto(gameId, ctx, Turn.PLAYER_2);
        broadcastService.broadcastState(gameId, ctx, p1Dto, p2Dto);
        if (ctx.gameState().getRevealedCards() != null) {
            ctx.gameState().setRevealedCards(null);
        }
        persistenceService.persist(gameId, ctx, p1Dto);
    }

    private void updateTimersAfterExecution (UUID gameId, SessionContext ctx, Turn beforeTurn, Turn afterTurn) {
        PendingAbility pending = ctx.gameState().getPendingAbility();
        if (pending == PendingAbility.MEDIC_CHOICE) {
            scheduleMedicTimeout(gameId, ctx);
            timerService.cancelLeaderTimer(gameId);
            timerService.cancelTurnTimer(gameId);
        } else if (pending != null && pending.name().startsWith("LEADER_")) {
            scheduleLeaderTimeout(gameId, ctx);
            timerService.cancelMedicTimer(gameId);
            timerService.cancelTurnTimer(gameId);
        } else if (pending == null && beforeTurn != afterTurn && ctx.gameState().getPhase().equals(GamePhase.PLAY)) {
            scheduleTurnTimer(gameId, ctx);
            timerService.cancelMedicTimer(gameId);
            timerService.cancelLeaderTimer(gameId);
        } else {
            timerService.cancelMedicTimer(gameId);
            timerService.cancelLeaderTimer(gameId);
            if (pending == null && ctx.gameState().getPhase() == GamePhase.PLAY && !ctx.gameState().isGameOver()) {
                scheduleTurnTimer(gameId, ctx);
            } else {
                timerService.cancelTurnTimer(gameId);
            }
        }
    }

    private void scheduleMedicTimeout (UUID gameId, SessionContext ctx) {
        timerService.scheduleMedicTimeout(gameId, () -> {
            sessionRegistry.executeWithLockVoid(gameId, lockedCtx -> {
                if (lockedCtx.gameState().getPendingAbility() == PendingAbility.MEDIC_CHOICE) {
                    PlayerState current = lockedCtx.gameState().getCurrentPlayer();
                    Card randomUnit = current.getGraveyard().stream()
                            .filter(c -> c.cardType() == CardType.UNIT)
                            .findAny()
                            .orElse(null);
                    if (randomUnit != null) {
                        engine.execute(lockedCtx.gameState(), new ResolveMedicCommand(randomUnit));
                        if (lockedCtx.gameState().getPendingAbility() == PendingAbility.MEDIC_CHOICE) {
                            scheduleMedicTimeout(gameId, lockedCtx);
                        } else if (lockedCtx.gameState().getPhase() == GamePhase.PLAY && !lockedCtx.gameState().isGameOver()) {
                            scheduleTurnTimer(gameId, lockedCtx);
                        }
                        broadcastAndPersist(gameId, lockedCtx);
                    }
                }
            });
        });
    }

    private void scheduleLeaderTimeout (UUID gameId, SessionContext ctx) {
        timerService.scheduleLeaderTimeout(gameId, () -> {
            sessionRegistry.executeWithLockVoid(gameId, lockedCtx -> {
                PendingAbility pending = lockedCtx.gameState().getPendingAbility();
                if (pending == null || !pending.name().startsWith("LEADER_")) return;

                Card randomCard = resolveRandomLeaderCard(lockedCtx.gameState(), pending);
                if (randomCard != null) {
                    engine.execute(lockedCtx.gameState(), new ResolveLeaderCommand(randomCard));
                    PendingAbility newPending = lockedCtx.gameState().getPendingAbility();
                    if (newPending != null && newPending.name().startsWith("LEADER_")) {
                        scheduleLeaderTimeout(gameId, lockedCtx);
                    } else if (lockedCtx.gameState().getPhase() == GamePhase.PLAY && !lockedCtx.gameState().isGameOver()) {
                        scheduleTurnTimer(gameId, lockedCtx);
                    }
                    broadcastAndPersist(gameId, lockedCtx);
                }
            });
        });
    }

    private Card resolveRandomLeaderCard (GameState state, PendingAbility pending) {
        PlayerState current = state.getCurrentPlayer();
        return switch (pending) {
            case LEADER_GRAVEYARD_PICK -> current.getGraveyard().stream()
                    .filter(c -> c.cardType() == CardType.UNIT).findAny().orElse(null);
            case LEADER_OPPONENT_GRAVEYARD_PICK -> state.getOpponent().getGraveyard().stream()
                    .filter(c -> c.cardType() == CardType.UNIT).findAny().orElse(null);
            case LEADER_DECK_PICK -> current.getDeck().stream().findAny().orElse(null);
            case LEADER_HAND_DISCARD -> current.getHand().stream().findAny().orElse(null);
            default -> null;
        };
    }

    private void scheduleScoiataelTimeout (UUID gameId, SessionContext ctx) {
        timerService.scheduleScoiataelTimeout(gameId, () -> {
            sessionRegistry.executeWithLockVoid(gameId, lockedCtx -> {
                if (lockedCtx.gameState().getPendingAbility() == PendingAbility.SCOIATAEL_FIRST_PLAYER_CHOICE) {
                    Turn randomChoice = Math.random() < 0.5 ? Turn.PLAYER_1 : Turn.PLAYER_2;
                    engine.execute(lockedCtx.gameState(), new ResolveScoiataelCommand(randomChoice));
                    scheduleMulliganTimeout(gameId, lockedCtx);
                    broadcastAndPersist(gameId, lockedCtx);
                }
            });
        });
    }

    private void scheduleMulliganTimeout (UUID gameId, SessionContext ctx) {
        timerService.scheduleMulliganTimeout(gameId, () -> {
            sessionRegistry.executeWithLockVoid(gameId, lockedCtx -> {
                if (lockedCtx.gameState().getPhase() == GamePhase.REDRAW) {
                    engine.startPlay(lockedCtx.gameState());
                    scheduleTurnTimer(gameId, lockedCtx);
                    broadcastAndPersist(gameId, lockedCtx);
                }
            });
        });
    }

    private void scheduleTurnTimer (UUID gameId, SessionContext ctx) {
        timerService.scheduleTurnTimer(gameId, () -> {
            sessionRegistry.executeWithLockVoid(gameId, lockedCtx -> {
                GameState state = lockedCtx.gameState();
                if (state.getPendingAbility() == null && state.getPhase() == GamePhase.PLAY && !state.isGameOver()) {
                    engine.execute(state, new PassCommand());
                    if (state.getPhase() == GamePhase.PLAY && !state.isGameOver()) {
                        scheduleTurnTimer(gameId, lockedCtx);
                    } else if (state.isGameOver()) {
                        timerService.cancelDisconnectTimersForGame(gameId, lockedCtx.player1Id(), lockedCtx.player2Id());
                    }
                    broadcastAndPersist(gameId, lockedCtx);
                }
            });
        });
    }

    // --- Mapping delegation ---

    private GameStateDto toDto (UUID gameId, SessionContext ctx, Turn perspective) {
        GameState state = ctx.gameState();
        PlayerState meState = state.getPlayer(perspective);
        PlayerState opponentState = state.getPlayer(perspective == Turn.PLAYER_1 ? Turn.PLAYER_2 : Turn.PLAYER_1);

        return mapper.toGameStateDto(
                gameId, ctx, perspective,
                engine.calculateScore(meState),
                engine.calculateScore(opponentState),
                timerService.getTurnDeadline(gameId),
                timerService.getAbilityDeadline(gameId),
                sessionRegistry.hasDisconnectForfeit(gameId)
        );
    }

}