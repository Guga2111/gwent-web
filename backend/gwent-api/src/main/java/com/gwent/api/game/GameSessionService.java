package com.gwent.api.game;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gwent.api.catalog.CardCatalogRepository;
import com.gwent.api.catalog.CardEntity;
import com.gwent.api.deck.Deck;
import com.gwent.api.deck.DeckRepository;
import com.gwent.api.deck.exception.DeckNotFoundException;
import com.gwent.api.game.dto.*;
import com.gwent.api.game.exception.GameNotFoundException;
import com.gwent.api.game.exception.GameNotWaitingException;
import com.gwent.api.game.exception.PlayerNotInGameException;
import com.gwent.engine.command.PassCommand;
import com.gwent.engine.command.ResolveLeaderCommand;
import com.gwent.engine.command.ResolveMedicCommand;
import com.gwent.engine.command.ResolveScoiataelCommand;
import com.gwent.engine.core.GwentEngine;
import com.gwent.engine.domain.*;
import com.gwent.engine.state.GameState;
import com.gwent.engine.state.PlayerState;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;

@Service
public class GameSessionService {

    private final GwentEngine engine = new GwentEngine();
    private final Map<UUID, SessionContext> sessions = new ConcurrentHashMap<>();
    private final Map<UUID, Object> gameLocks = new ConcurrentHashMap<>();
    private final Map<UUID, ScheduledFuture<?>> medicTimers = new ConcurrentHashMap<>();
    private final Map<UUID, ScheduledFuture<?>> leaderTimers = new ConcurrentHashMap<>();
    private final Map<UUID, ScheduledFuture<?>> scoiataelTimers = new ConcurrentHashMap<>();
    private final Map<UUID, ScheduledFuture<?>> turnTimers = new ConcurrentHashMap<>();
    private final Map<UUID, Long> turnDeadlines = new HashMap<>();
    // 4 threads: sufficient for MVP. When scaling, replace with Spring TaskScheduler
    // (container-managed, configurable) or Redis TTL + keyspace notifications (zero threads).
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);

    private final GameRepository gameRepository;
    private final ObjectMapper objectMapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final GameModelMapper mapper;
    private final CardCatalogRepository cardCatalogRepository;
    private final DeckRepository deckRepository;

    public GameSessionService(GameRepository gameRepository, ObjectMapper objectMapper,
                              SimpMessagingTemplate messagingTemplate, GameModelMapper mapper,
                              CardCatalogRepository cardCatalogRepository, DeckRepository deckRepository) {
        this.gameRepository = gameRepository;
        this.objectMapper = objectMapper;
        this.messagingTemplate = messagingTemplate;
        this.mapper = mapper;
        this.cardCatalogRepository = cardCatalogRepository;
        this.deckRepository = deckRepository;
    }

    public CreateGameDto createSession(String userId, UUID deckId) {
        UUID gameId = UUID.randomUUID();

        Game game = new Game();
        game.setId(gameId);
        game.setPlayer1Id(userId);
        game.setPlayer1DeckId(deckId);
        game.setStatus(GameStatus.WAITING);
        gameRepository.save(game);

        return new CreateGameDto(gameId, userId);
    }

    public GameStateDto joinSession(UUID gameId, String userId, UUID deckId) {
        Object lock = gameLocks.computeIfAbsent(gameId, k -> new Object());
        synchronized (lock) {
            Game game = gameRepository.findById(gameId)
                    .orElseThrow(() -> new GameNotFoundException(gameId));

            if (game.getStatus() != GameStatus.WAITING) {
                throw new GameNotWaitingException(gameId);
            }

            game.setPlayer2Id(userId);
            game.setPlayer2DeckId(deckId);

            PlayerState player1 = new PlayerState(buildLeaderFromDeckId(game.getPlayer1DeckId()), buildDeckFromDeckId(game.getPlayer1DeckId()));
            PlayerState player2 = new PlayerState(buildLeaderFromDeckId(deckId), buildDeckFromDeckId(deckId));
            GameState gameState = new GameState(player1, player2);

            engine.drawInitialCards(gameState, 10);
            engine.resolveCoinFlip(gameState, Turn.PLAYER_1);

            SessionContext ctx = new SessionContext(gameState, game.getPlayer1Id(), userId);
            sessions.put(gameId, ctx);
            if (gameState.getPhase() == GamePhase.REDRAW) {
                scheduleMulliganTimeout(gameId, ctx);
            } else {
                scheduleScoiataelTimeout(gameId, ctx);
            }

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
            Turn currentTurn = ctx.gameState().getCurrentTurn();
            engine.execute(ctx.gameState(), mapper.toCommand(request, player, ctx.gameState(), ctx));
            Turn afterExecutionTurn = ctx.gameState().getCurrentTurn();

            if (request.commandType() == CommandType.RESOLVE_SCOIATAEL) {
                cancelScoiataelTimer(gameId);
                scheduleMulliganTimeout(gameId, ctx);
            }

            PendingAbility pending = ctx.gameState().getPendingAbility();
            if (pending == PendingAbility.MEDIC_CHOICE) {
                scheduleMedicTimeout(gameId, ctx);
                cancelLeaderTimer(gameId);
                cancelTurnTimer(gameId);
            } else if (pending != null && pending.name().startsWith("LEADER_")) {
                scheduleLeaderTimeout(gameId, ctx);
                cancelMedicTimer(gameId);
                cancelTurnTimer(gameId);
            } else if (pending == null && currentTurn != afterExecutionTurn && ctx.gameState().getPhase().equals(GamePhase.PLAY)) {
                scheduleTurnTimer(gameId, ctx);
                cancelMedicTimer(gameId);
                cancelLeaderTimer(gameId);
            } else {
                cancelMedicTimer(gameId);
                cancelLeaderTimer(gameId);
                if (pending == null && ctx.gameState().getPhase() == GamePhase.PLAY && !ctx.gameState().isGameOver()) {
                    scheduleTurnTimer(gameId, ctx);
                } else {
                    cancelTurnTimer(gameId);
                }
            }

            if (ctx.gameState().isGameOver()) cancelTurnTimer(gameId);

            broadcastState(gameId, ctx);

            if (ctx.gameState().getRevealedCards() != null) {
                ctx.gameState().setRevealedCards(null);
            }

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
            cancelLeaderTimer(gameId);
            cancelScoiataelTimer(gameId);
            cancelTurnTimer(gameId);
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
                        } else if (ctx.gameState().getPhase() == GamePhase.PLAY && !ctx.gameState().isGameOver()) {
                            scheduleTurnTimer(gameId, ctx);
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

    private void scheduleLeaderTimeout(UUID gameId, SessionContext ctx) {
        cancelLeaderTimer(gameId);
        leaderTimers.put(gameId, scheduler.schedule(() -> {
            Object lock = gameLocks.computeIfAbsent(gameId, k -> new Object());
            synchronized (lock) {
                PendingAbility pending = ctx.gameState().getPendingAbility();
                if (pending == null || !pending.name().startsWith("LEADER_")) return;

                Card randomCard = resolveRandomLeaderCard(ctx.gameState(), pending);
                if (randomCard != null) {
                    engine.execute(ctx.gameState(), new ResolveLeaderCommand(randomCard));
                    PendingAbility newPending = ctx.gameState().getPendingAbility();
                    if (newPending != null && newPending.name().startsWith("LEADER_")) {
                        scheduleLeaderTimeout(gameId, ctx);
                    } else if (ctx.gameState().getPhase() == GamePhase.PLAY && !ctx.gameState().isGameOver()) {
                        scheduleTurnTimer(gameId, ctx);
                    }
                    broadcastState(gameId, ctx);
                    if (ctx.gameState().getRevealedCards() != null) {
                        ctx.gameState().setRevealedCards(null);
                    }
                    persist(gameId, ctx);
                }
            }
        }, 30, TimeUnit.SECONDS));
    }

    private void cancelLeaderTimer(UUID gameId) {
        ScheduledFuture<?> timer = leaderTimers.remove(gameId);
        if (timer != null) timer.cancel(false);
    }

    private Card resolveRandomLeaderCard(GameState state, PendingAbility pending) {
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

    private void scheduleScoiataelTimeout(UUID gameId, SessionContext ctx) {
        cancelScoiataelTimer(gameId);
        scoiataelTimers.put(gameId, scheduler.schedule(() -> {
            Object lock = gameLocks.computeIfAbsent(gameId, k -> new Object());
            synchronized (lock) {
                if (ctx.gameState().getPendingAbility() == PendingAbility.SCOIATAEL_FIRST_PLAYER_CHOICE) {
                    Turn randomChoice = Math.random() < 0.5 ? Turn.PLAYER_1 : Turn.PLAYER_2;
                    engine.execute(ctx.gameState(), new ResolveScoiataelCommand(randomChoice));
                    scheduleMulliganTimeout(gameId, ctx);
                    broadcastState(gameId, ctx);
                    persist(gameId, ctx);
                }
            }
        }, 30, TimeUnit.SECONDS));
    }

    private void cancelScoiataelTimer(UUID gameId) {
        ScheduledFuture<?> timer = scoiataelTimers.remove(gameId);
        if (timer != null) timer.cancel(false);
    }

    private void scheduleMulliganTimeout(UUID gameId, SessionContext ctx) {
        scheduler.schedule(() -> {
            Object lock = gameLocks.computeIfAbsent(gameId, k -> new Object());
            synchronized (lock) {
                if (ctx.gameState().getPhase() == GamePhase.REDRAW) {
                    engine.startPlay(ctx.gameState());
                    scheduleTurnTimer(gameId, ctx);
                    broadcastState(gameId, ctx);
                    persist(gameId, ctx);
                }
            }
        }, 30, TimeUnit.SECONDS);
    }

    private void scheduleTurnTimer(UUID gameId, SessionContext ctx) {
        long deadline = System.currentTimeMillis() + 60_000;
        turnDeadlines.put(gameId, deadline);

        cancelTurnTimer(gameId);
        turnTimers.put(gameId, scheduler.schedule(() -> {
            Object lock = gameLocks.computeIfAbsent(gameId, k -> new Object());
            synchronized (lock) {
                GameState state = ctx.gameState();
                if (state.getPendingAbility() == null && state.getPhase() == GamePhase.PLAY && !state.isGameOver()) {
                    engine.execute(state, new PassCommand());
                    broadcastState(gameId, ctx);
                    persist(gameId, ctx);
                    if (state.getPhase() == GamePhase.PLAY && !state.isGameOver()) {
                        scheduleTurnTimer(gameId, ctx);
                    }
                }
            }
        }, 60, TimeUnit.SECONDS));
    }

    private void cancelTurnTimer (UUID gameId) {
        ScheduledFuture<?> timer = turnTimers.remove(gameId);
        Long deadline = turnDeadlines.remove(gameId);
        if (timer != null) timer.cancel(false);
    }

    // --- Mapping delegation ---

    private GameStateDto toDto(UUID gameId, SessionContext ctx, Turn perspective) {
        GameState state = ctx.gameState();
        PlayerState meState = state.getPlayer(perspective);
        PlayerState opponentState = state.getPlayer(perspective == Turn.PLAYER_1 ? Turn.PLAYER_2 : Turn.PLAYER_1);
        Long deadlines = turnDeadlines.get(gameId);

        return mapper.toGameStateDto(
                gameId, ctx, perspective,
                engine.calculateScore(meState),
                engine.calculateScore(opponentState),
                deadlines
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

    // --- Deck-ID-backed deck building ---

    private Card buildLeaderFromDeckId(UUID deckId) {
        Deck deck = deckRepository.findById(deckId)
                .orElseThrow(() -> new DeckNotFoundException(deckId));
        CardEntity leader = cardCatalogRepository.findById(deck.getLeaderId())
                .orElseThrow(() -> new IllegalStateException("Leader card not found: " + deck.getLeaderId()));
        return toEngineCard(leader, leader.getId());
    }

    private List<Card> buildDeckFromDeckId(UUID deckId) {
        Deck deck = deckRepository.findById(deckId)
                .orElseThrow(() -> new DeckNotFoundException(deckId));
        List<Card> cards = new ArrayList<>();
        for (var entry : deck.getCards()) {
            CardEntity template = cardCatalogRepository.findById(entry.getCardId())
                    .orElseThrow(() -> new IllegalStateException("Card not found: " + entry.getCardId()));
            for (int i = 1; i <= entry.getQuantity(); i++) {
                String instanceId = entry.getQuantity() > 1 ? entry.getCardId() + "_" + i : entry.getCardId();
                cards.add(toEngineCard(template, instanceId));
            }
        }
        Collections.shuffle(cards);
        return cards;
    }

    private Card toEngineCard(CardEntity e, String id) {
        return new Card(id, e.getName(), e.getFaction(), e.getCardType(),
                e.getAbility(), e.getLeaderAbility(), e.getRowType(), e.getBasePower());
    }
}
