package com.gwent.api.game;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gwent.api.catalog.CardCatalogRepository;
import com.gwent.api.catalog.CardEntity;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    private final CardCatalogRepository cardCatalogRepository;

    public GameSessionService(GameRepository gameRepository, ObjectMapper objectMapper,
                              SimpMessagingTemplate messagingTemplate, GameModelMapper mapper,
                              CardCatalogRepository cardCatalogRepository) {
        this.gameRepository = gameRepository;
        this.objectMapper = objectMapper;
        this.messagingTemplate = messagingTemplate;
        this.mapper = mapper;
        this.cardCatalogRepository = cardCatalogRepository;
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

            PlayerState player1 = new PlayerState(buildLeader(Faction.NORTHERN_REALMS), buildDeck(Faction.NORTHERN_REALMS));
            PlayerState player2 = new PlayerState(buildLeader(Faction.NORTHERN_REALMS), buildDeck(Faction.NORTHERN_REALMS));
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

    public Optional<ActiveGameDto> getActiveGame(String userId) {
        return gameRepository.findActiveGameForPlayer(GameStatus.IN_PROGRESS, userId)
                .map(g -> new ActiveGameDto(g.getId()));
    }

    public void surrender(UUID gameId, String userId) {
        Object lock = gameLocks.computeIfAbsent(gameId, k -> new Object());
        synchronized (lock) {
            SessionContext ctx = sessions.get(gameId);
            if (ctx == null) throw new GameNotFoundException(gameId);

            Turn player = resolvePlayer(ctx, userId);
            engine.surrender(ctx.gameState(), player);

            cancelMedicTimer(gameId);
            broadcastState(gameId, ctx);
            persist(gameId, ctx);
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

    // --- Catalog-backed deck building ---

    private Card buildLeader(Faction faction) {
        return cardCatalogRepository.findByFaction(faction).stream()
                .filter(e -> e.getCardType() == CardType.LEADER)
                .findFirst()
                .map(e -> toEngineCard(e, e.getId()))
                .orElseThrow(() -> new IllegalStateException("No leader found for faction: " + faction));
    }

    private List<Card> buildDeck(Faction faction) {
        List<Card> deck = new ArrayList<>();
        for (CardEntity template : cardCatalogRepository.findByFaction(faction)) {
            if (template.getCardType() == CardType.LEADER || template.getDeckCopies() == 0) continue;
            for (int i = 1; i <= template.getDeckCopies(); i++) {
                String id = template.getDeckCopies() > 1 ? template.getId() + "_" + i : template.getId();
                deck.add(toEngineCard(template, id));
            }
        }
        Collections.shuffle(deck);
        return deck;
    }

    private Card toEngineCard(CardEntity e, String id) {
        return new Card(id, e.getName(), e.getFaction(), e.getCardType(),
                e.getAbility(), e.getLeaderAbility(), e.getRowType(), e.getBasePower());
    }
}
