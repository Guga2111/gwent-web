package com.gwent.api.game;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gwent.api.game.dto.*;
import com.gwent.api.game.exception.GameNotFoundException;
import com.gwent.api.game.exception.GameNotWaitingException;
import com.gwent.api.game.exception.PlayerNotInGameException;
import com.gwent.engine.command.ResolveMedicCommand;
import com.gwent.engine.core.GwentEngine;
import com.gwent.engine.domain.*;
import com.gwent.engine.state.GameState;
import com.gwent.engine.state.PlayerState;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

@Service
public class GameSessionService {

    private final GwentEngine engine = new GwentEngine();
    private final Map<UUID, SessionContext> sessions = new ConcurrentHashMap<>();
    private final Map<UUID, Object> gameLocks = new ConcurrentHashMap<>();
    private final Map<UUID, ScheduledFuture<?>> medicTimers = new ConcurrentHashMap<>();
    // 4 threads: sufficient for MVP. When scaling, replace with Spring TaskScheduler
    // (container-managed, configurable) or Redis TTL + keyspace notifications (zero threads).
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);

    private final GameRepository gameRepository;
    private final ObjectMapper objectMapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final GameModelMapper mapper;

    public GameSessionService(GameRepository gameRepository, ObjectMapper objectMapper,
                              SimpMessagingTemplate messagingTemplate, GameModelMapper mapper) {
        this.gameRepository = gameRepository;
        this.objectMapper = objectMapper;
        this.messagingTemplate = messagingTemplate;
        this.mapper = mapper;
    }

    public CreateGameDto createSession(String userId) {
        UUID gameId = UUID.randomUUID();

        Game game = new Game();
        game.setId(gameId);
        game.setPlayer1Id(userId);
        game.setStatus(GameStatus.WAITING);
        gameRepository.save(game);

        return new CreateGameDto(gameId, userId);
    }

    public GameStateDto joinSession(UUID gameId, String userId) {
        Object lock = gameLocks.computeIfAbsent(gameId, k -> new Object());
        synchronized (lock) {
            Game game = gameRepository.findById(gameId)
                    .orElseThrow(() -> new GameNotFoundException(gameId));

            if (game.getStatus() != GameStatus.WAITING) {
                throw new GameNotWaitingException(gameId);
            }

            game.setPlayer2Id(userId);

            PlayerState player1 = new PlayerState(makeLeader(), makePresetDeck());
            PlayerState player2 = new PlayerState(makeLeader(), makePresetDeck());
            GameState gameState = new GameState(player1, player2);

            engine.drawInitialCards(gameState, 10);
            engine.resolveCoinFlip(gameState, Turn.PLAYER_1);

            SessionContext ctx = new SessionContext(gameState, game.getPlayer1Id(), userId);
            sessions.put(gameId, ctx);
            scheduleMulliganTimeout(gameId, ctx);

            broadcastState(gameId, ctx);
            persist(gameId, ctx);

            return toDto(gameId, ctx, Turn.PLAYER_2);
        }
    }

    public GameStateDto execute(UUID gameId, String userId, CommandRequestDto request) {
        Object lock = gameLocks.computeIfAbsent(gameId, k -> new Object());
        synchronized (lock) {
            SessionContext ctx = sessions.get(gameId);
            if (ctx == null) throw new GameNotFoundException(gameId);

            Turn player = resolvePlayer(ctx, userId);
            engine.execute(ctx.gameState(), mapper.toCommand(request, player, ctx.gameState()));

            if (ctx.gameState().getPendingAbility() == PendingAbility.MEDIC_CHOICE) {
                scheduleMedicTimeout(gameId, ctx);
            } else {
                cancelMedicTimer(gameId);
            }

            broadcastState(gameId, ctx);
            persist(gameId, ctx);

            return toDto(gameId, ctx, player);
        }
    }

    public GameStateDto getSession(UUID gameId, String userId) {
        SessionContext ctx = sessions.get(gameId);
        if (ctx != null) {
            Turn perspective = resolvePlayer(ctx, userId);
            return toDto(gameId, ctx, perspective);
        }

        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException(gameId));

        if (game.getStateJson() == null) throw new GameNotFoundException(gameId);

        try {
            return objectMapper.readValue(game.getStateJson(), GameStateDto.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize game state", e);
        }
    }

    // --- Player resolution ---

    private Turn resolvePlayer(SessionContext ctx, String userId) {
        if (userId.equals(ctx.player1Id())) return Turn.PLAYER_1;
        if (userId.equals(ctx.player2Id())) return Turn.PLAYER_2;
        throw new PlayerNotInGameException(userId);
    }

    // --- Broadcast & Timeout ---

    private void broadcastState(UUID gameId, SessionContext ctx) {
        GameStateDto p1Dto = toDto(gameId, ctx, Turn.PLAYER_1);
        GameStateDto p2Dto = toDto(gameId, ctx, Turn.PLAYER_2);
        messagingTemplate.convertAndSend("/topic/games/" + gameId + "/" + ctx.player1Id(), p1Dto);
        messagingTemplate.convertAndSend("/topic/games/" + gameId + "/" + ctx.player2Id(), p2Dto);
    }

    private void scheduleMedicTimeout(UUID gameId, SessionContext ctx) {
        cancelMedicTimer(gameId);
        medicTimers.put(gameId, scheduler.schedule(() -> {
            Object lock = gameLocks.computeIfAbsent(gameId, k -> new Object());
            synchronized (lock) {
                if (ctx.gameState().getPendingAbility() == PendingAbility.MEDIC_CHOICE) {
                    PlayerState current = ctx.gameState().getCurrentPlayer();
                    Card randomUnit = current.getGraveyard().stream()
                            .filter(c -> c.cardType() == CardType.UNIT)
                            .findAny()
                            .orElse(null);
                    if (randomUnit != null) {
                        engine.execute(ctx.gameState(), new ResolveMedicCommand(randomUnit));
                        if (ctx.gameState().getPendingAbility() == PendingAbility.MEDIC_CHOICE) {
                            scheduleMedicTimeout(gameId, ctx);
                        }
                        broadcastState(gameId, ctx);
                        persist(gameId, ctx);
                    }
                }
            }
        }, 30, TimeUnit.SECONDS));
    }

    private void cancelMedicTimer(UUID gameId) {
        ScheduledFuture<?> timer = medicTimers.remove(gameId);
        if (timer != null) timer.cancel(false);
    }

    private void scheduleMulliganTimeout(UUID gameId, SessionContext ctx) {
        scheduler.schedule(() -> {
            Object lock = gameLocks.computeIfAbsent(gameId, k -> new Object());
            synchronized (lock) {
                if (ctx.gameState().getPhase() == GamePhase.REDRAW) {
                    engine.startPlay(ctx.gameState());
                    broadcastState(gameId, ctx);
                    persist(gameId, ctx);
                }
            }
        }, 30, TimeUnit.SECONDS);
    }

    // --- Mapping delegation ---

    private GameStateDto toDto(UUID gameId, SessionContext ctx, Turn perspective) {
        GameState state = ctx.gameState();
        PlayerState meState = state.getPlayer(perspective);
        PlayerState opponentState = state.getPlayer(perspective == Turn.PLAYER_1 ? Turn.PLAYER_2 : Turn.PLAYER_1);

        return mapper.toGameStateDto(
                gameId, ctx, perspective,
                engine.calculateScore(meState),
                engine.calculateScore(opponentState)
        );
    }

    private void persist(UUID gameId, SessionContext ctx) {
        try {
            GameStatus status = ctx.gameState().getPhase() == GamePhase.GAME_OVER
                    ? GameStatus.FINISHED : GameStatus.IN_PROGRESS;
            GameStateDto dto = toDto(gameId, ctx, Turn.PLAYER_1);
            Game game = gameRepository.findById(gameId).orElse(new Game());
            game.setId(gameId);
            game.setStateJson(objectMapper.writeValueAsString(dto));
            game.setStatus(status);
            gameRepository.save(game);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize game state", e);
        }
    }

    // --- Preset data ---

    private Card makeLeader() {
        return new Card("FOLTEST", "Foltest", Faction.NORTHERN_REALMS,
                CardType.LEADER, null, LeaderAbility.SIEGE_MASTER, null, null);
    }

    private List<Card> makePresetDeck() {
        return List.of(
                new Card("NR_MELEE_1",  "Swordsman",   Faction.NORTHERN_REALMS, CardType.UNIT, null, null, RowType.MELEE,  5),
                new Card("NR_MELEE_2",  "Knight",      Faction.NORTHERN_REALMS, CardType.UNIT, null, null, RowType.MELEE,  6),
                new Card("NR_MELEE_3",  "Foot Soldier",Faction.NORTHERN_REALMS, CardType.UNIT, null, null, RowType.MELEE,  4),
                new Card("NR_MELEE_4",  "Field Medic",  Faction.NORTHERN_REALMS, CardType.UNIT, Ability.MEDIC, null, RowType.MELEE,  1),
                new Card("NR_MELEE_5",  "Cavalry",     Faction.NORTHERN_REALMS, CardType.UNIT, null, null, RowType.MELEE,  7),
                new Card("NR_RANGED_1", "Archer",      Faction.NORTHERN_REALMS, CardType.UNIT, null, null, RowType.RANGED, 5),
                new Card("NR_RANGED_2", "Crossbowman", Faction.NORTHERN_REALMS, CardType.UNIT, null, null, RowType.RANGED, 4),
                new Card("NR_RANGED_3", "Rifleman",    Faction.NORTHERN_REALMS, CardType.UNIT, null, null, RowType.RANGED, 6),
                new Card("NR_RANGED_4", "Scout",       Faction.NORTHERN_REALMS, CardType.UNIT, null, null, RowType.RANGED, 3),
                new Card("NR_RANGED_5", "Marksman",    Faction.NORTHERN_REALMS, CardType.UNIT, null, null, RowType.RANGED, 7),
                new Card("NR_SIEGE_1",  "Catapult",    Faction.NORTHERN_REALMS, CardType.UNIT, null, null, RowType.SIEGE,  8),
                new Card("NR_SIEGE_2",  "Ballista",    Faction.NORTHERN_REALMS, CardType.UNIT, null, null, RowType.SIEGE,  6),
                new Card("NR_SIEGE_3",  "Trebuchet",   Faction.NORTHERN_REALMS, CardType.UNIT, null, null, RowType.SIEGE,  7),
                new Card("NR_SIEGE_4",  "Ram",         Faction.NORTHERN_REALMS, CardType.UNIT, null, null, RowType.SIEGE,  5),
                new Card("NR_SIEGE_5",  "Siege Tower", Faction.NORTHERN_REALMS, CardType.UNIT, null, null, RowType.SIEGE,  9)
        );
    }
}
