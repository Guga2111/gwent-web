package com.gwent.api.game;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gwent.api.game.dto.CommandRequestDto;
import com.gwent.api.game.dto.CreateGameDto;
import com.gwent.api.game.dto.GameStateDto;
import com.gwent.api.game.dto.PlayerStateDto;
import com.gwent.api.shared.exception.CardNotFoundException;
import com.gwent.api.shared.exception.GameNotFoundException;
import com.gwent.engine.command.*;
import com.gwent.engine.core.GwentEngine;
import com.gwent.engine.domain.*;
import com.gwent.engine.state.GameState;
import com.gwent.engine.state.PlayerState;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class GameSessionService {

    private record SessionContext(GameState gameState, String player1Id, String player2Id) {}

    private final GwentEngine engine = new GwentEngine();
    private final Map<UUID, SessionContext> sessions = new ConcurrentHashMap<>();
    // 4 threads: sufficient for MVP. When scaling, replace with Spring TaskScheduler
    // (container-managed, configurable) or Redis TTL + keyspace notifications (zero threads).
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);

    private final GameRepository gameRepository;
    private final ObjectMapper objectMapper;
    private final SimpMessagingTemplate messagingTemplate;

    public GameSessionService(GameRepository gameRepository, ObjectMapper objectMapper,
                              SimpMessagingTemplate messagingTemplate) {
        this.gameRepository = gameRepository;
        this.objectMapper = objectMapper;
        this.messagingTemplate = messagingTemplate;
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
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new GameNotFoundException(gameId));

        if (game.getStatus() != GameStatus.WAITING) {
            throw new IllegalArgumentException("Game is not waiting for players");
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

        GameStateDto dto = toDto(gameId, ctx);
        persist(gameId, dto, GameStatus.IN_PROGRESS);
        broadcastState(gameId, dto);

        return dto;
    }

    public GameStateDto execute(UUID gameId, String userId, CommandRequestDto request) {
        SessionContext ctx = sessions.get(gameId);
        if (ctx == null) throw new GameNotFoundException(gameId);

        Turn player = resolvePlayer(ctx, userId);
        GameCommand command = toCommand(request, player, ctx.gameState());
        engine.execute(ctx.gameState(), command);

        GameStatus status = ctx.gameState().getPhase() == GamePhase.GAME_OVER
                ? GameStatus.FINISHED : GameStatus.IN_PROGRESS;

        GameStateDto dto = toDto(gameId, ctx);
        persist(gameId, dto, status);
        broadcastState(gameId, dto);
        return dto;
    }

    public GameStateDto getSession(UUID gameId) {
        SessionContext ctx = sessions.get(gameId);
        if (ctx != null) return toDto(gameId, ctx);

        // Fallback: return last persisted state from DB (handles missed broadcast / server restart)
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
        throw new IllegalArgumentException("User is not a player in this game");
    }

    // --- Broadcast & Timeout ---

    private void broadcastState(UUID gameId, GameStateDto dto) {
        messagingTemplate.convertAndSend("/topic/games/" + gameId, dto);
    }

    private void scheduleMulliganTimeout(UUID gameId, SessionContext ctx) {
        scheduler.schedule(() -> {
            if (ctx.gameState().getPhase() == GamePhase.REDRAW) {
                engine.startPlay(ctx.gameState());
                GameStateDto dto = toDto(gameId, ctx);
                persist(gameId, dto, GameStatus.IN_PROGRESS);
                broadcastState(gameId, dto);
            }
        }, 30, TimeUnit.SECONDS);
    }

    // --- Mapping ---

    private GameStateDto toDto(UUID gameId, SessionContext ctx) {
        GameState state = ctx.gameState();
        return new GameStateDto(
                gameId,
                state.getPhase().name(),
                state.getCurrentTurn().name(),
                state.getPendingAbility() != null ? state.getPendingAbility().name() : null,
                state.getCurrentRound(),
                toPlayerDto(ctx.player1Id(), state.getPlayer1(), state),
                toPlayerDto(ctx.player2Id(), state.getPlayer2(), state)
        );
    }

    private GameCommand toCommand(CommandRequestDto request, Turn player, GameState state) {
        return switch (request.commandType()) {
            case "PASS"             -> new PassCommand();
            case "USE_LEADER"       -> new UseLeaderCommand();
            case "CONFIRM_MULLIGAN" -> new ConfirmMulliganCommand(player);
            case "PLAY_CARD"        -> new PlayCardCommand(
                    findCard(request.cardId(), state.getPlayer(player).getHand()),
                    RowType.valueOf(request.targetRow()));
            case "MULLIGAN"         -> new MulliganCommand(
                    player,
                    findCard(request.cardId(), state.getPlayer(player).getHand()));
            case "RESOLVE_MEDIC"    -> new ResolveMedicCommand(
                    findCard(request.cardId(), state.getPlayer(player).getGraveyard()));
            default -> throw new IllegalArgumentException("Unknown command: " + request.commandType());
        };
    }

    private Card findCard(String cardId, List<Card> cards) {
        return cards.stream()
                .filter(c -> c.id().equals(cardId))
                .findFirst()
                .orElseThrow(() -> new CardNotFoundException(cardId));
    }

    private PlayerStateDto toPlayerDto(String playerId, PlayerState player, GameState state) {
        return new PlayerStateDto(
                playerId,
                player.getLives(),
                engine.calculateScore(player),
                player.isPassed(),
                player.isLeaderUsed(),
                player.getMulligansRemaining(),
                player.isMulliganConfirmed(),
                player.getHand().stream().map(Card::id).toList(),
                player.getMeleeRow().getCards().stream().map(Card::id).toList(),
                player.getRangedRow().getCards().stream().map(Card::id).toList(),
                player.getSiegeRow().getCards().stream().map(Card::id).toList(),
                player.getGraveyard().stream().map(Card::id).toList()
        );
    }

    // --- Persistence ---

    private void persist(UUID gameId, GameStateDto dto, GameStatus status) {
        try {
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
                new Card("NR_MELEE_4",  "Guard",       Faction.NORTHERN_REALMS, CardType.UNIT, null, null, RowType.MELEE,  3),
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