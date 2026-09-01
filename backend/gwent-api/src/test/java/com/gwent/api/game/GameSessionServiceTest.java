package com.gwent.api.game;

import com.gwent.api.game.dto.*;
import com.gwent.api.game.exception.GameNotFoundException;
import com.gwent.api.game.exception.GameNotWaitingException;
import com.gwent.api.game.service.*;
import com.gwent.engine.domain.*;
import com.gwent.engine.exception.command.InvalidPhaseCommandException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static com.gwent.api.shared.TestDataFactory.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameSessionServiceTest {

    @Mock
    private GameModelMapper mapper;

    @Mock
    private GameDeckBuilder gameDeckBuilder;

    @Mock
    private GamePersistenceService persistenceService;

    @Mock
    private GameBroadcastService broadcastService;

    @Mock
    private GameTimerService timerService;

    @Spy
    private GameSessionRegistry sessionRegistry = new GameSessionRegistry();

    @InjectMocks
    private GameSessionService gameSessionService;

    // ── createSession ──

    @Test
    void shouldCreateGameWithWaitingStatus() {
        UUID deckId = UUID.randomUUID();
        CreateGameDto expected = new CreateGameDto(UUID.randomUUID(), "user@test.com");
        when(persistenceService.createGame("user@test.com", deckId)).thenReturn(expected);

        gameSessionService.createSession("user@test.com", deckId);

        verify(persistenceService).createGame("user@test.com", deckId);
    }

    @Test
    void shouldReturnCreateGameDtoWithPlayerAndGameId() {
        UUID deckId = UUID.randomUUID();
        CreateGameDto expected = new CreateGameDto(UUID.randomUUID(), "user@test.com");
        when(persistenceService.createGame("user@test.com", deckId)).thenReturn(expected);

        CreateGameDto result = gameSessionService.createSession("user@test.com", deckId);

        assertNotNull(result.gameId());
        assertEquals("user@test.com", result.playerId());
    }

    // ── joinSession ──

    @Test
    void shouldThrowGameNotFoundException_whenJoinNotFound() {
        UUID gameId = UUID.randomUUID();
        when(persistenceService.getGameValidatingWaitingStatus(gameId))
                .thenThrow(new GameNotFoundException(gameId));

        assertThrows(GameNotFoundException.class,
                () -> gameSessionService.joinSession(gameId, "user@test.com", UUID.randomUUID()));
    }

    @Test
    void shouldThrowGameNotWaitingException_whenNotWaiting() {
        UUID gameId = UUID.randomUUID();
        when(persistenceService.getGameValidatingWaitingStatus(gameId))
                .thenThrow(new GameNotWaitingException(gameId));

        assertThrows(GameNotWaitingException.class,
                () -> gameSessionService.joinSession(gameId, "user@test.com", UUID.randomUUID()));
    }

    @Test
    void shouldInitializeGameState_whenValid() {
        UUID gameId = UUID.randomUUID();
        UUID deck1Id = UUID.randomUUID();
        UUID deck2Id = UUID.randomUUID();

        Game game = new Game();
        game.setId(gameId);
        game.setPlayer1Id("p1@test.com");
        game.setPlayer1DeckId(deck1Id);
        game.setStatus(GameStatus.WAITING);
        when(persistenceService.getGameValidatingWaitingStatus(gameId)).thenReturn(game);

        stubDeckBuilder(deck1Id, Faction.NORTHERN_REALMS, LeaderAbility.SIEGE_MASTER);
        stubDeckBuilder(deck2Id, Faction.NILFGAARD, LeaderAbility.EMPEROR_OF_NILFGAARD);

        GameStateDto mockDto = makeGameStateDto(gameId);
        when(mapper.toGameStateDto(any(), any(), any(), anyInt(), anyInt(), any(), any(), anyBoolean()))
                .thenReturn(mockDto);

        GameStateDto result = gameSessionService.joinSession(gameId, "p2@test.com", deck2Id);

        assertNotNull(result);
        verify(broadcastService, atLeastOnce()).broadcastState(any(), any(), any(), any());
    }

    // ── execute ──

    @Test
    void shouldThrowGameNotFoundException_whenSessionNotFound() {
        UUID gameId = UUID.randomUUID();
        CommandRequestDto request = new CommandRequestDto(null, CommandType.PASS, null, null, null, null);

        assertThrows(GameNotFoundException.class,
                () -> gameSessionService.execute(gameId, "user@test.com", request));
    }

    // ── getSession ──

    @Test
    void shouldThrowGameNotFoundException_whenGetSessionNotFound() {
        UUID gameId = UUID.randomUUID();
        when(persistenceService.getPersistedState(gameId))
                .thenThrow(new GameNotFoundException(gameId));

        assertThrows(GameNotFoundException.class,
                () -> gameSessionService.getSession(gameId, "user@test.com"));
    }

    @Test
    void shouldThrowGameNotFoundException_whenNoStateJson() {
        UUID gameId = UUID.randomUUID();
        when(persistenceService.getPersistedState(gameId))
                .thenThrow(new GameNotFoundException(gameId));

        assertThrows(GameNotFoundException.class,
                () -> gameSessionService.getSession(gameId, "user@test.com"));
    }

    @Test
    void shouldReturnDto_whenSessionInMemory() {
        UUID gameId = UUID.randomUUID();
        UUID deck1Id = UUID.randomUUID();
        UUID deck2Id = UUID.randomUUID();

        Game game = new Game();
        game.setId(gameId);
        game.setPlayer1Id("p1@test.com");
        game.setPlayer1DeckId(deck1Id);
        game.setStatus(GameStatus.WAITING);
        when(persistenceService.getGameValidatingWaitingStatus(gameId)).thenReturn(game);

        stubDeckBuilder(deck1Id, Faction.NORTHERN_REALMS, LeaderAbility.SIEGE_MASTER);
        stubDeckBuilder(deck2Id, Faction.NILFGAARD, LeaderAbility.EMPEROR_OF_NILFGAARD);

        GameStateDto mockDto = makeGameStateDto(gameId);
        when(mapper.toGameStateDto(any(), any(), any(), anyInt(), anyInt(), any(), any(), anyBoolean()))
                .thenReturn(mockDto);

        gameSessionService.joinSession(gameId, "p2@test.com", deck2Id);

        GameStateDto result = gameSessionService.getSession(gameId, "p1@test.com");

        assertNotNull(result);
    }

    // ── getActiveGame ──

    @Test
    void shouldReturnActiveGameDto_whenExists() {
        UUID gameId = UUID.randomUUID();
        when(persistenceService.findActiveGameForPlayer("user@test.com"))
                .thenReturn(Optional.of(new ActiveGameDto(gameId)));

        Optional<ActiveGameDto> result = gameSessionService.getActiveGame("user@test.com");

        assertTrue(result.isPresent());
        assertEquals(gameId, result.get().gameId());
    }

    @Test
    void shouldReturnEmpty_whenNoActiveGame() {
        when(persistenceService.findActiveGameForPlayer("user@test.com"))
                .thenReturn(Optional.empty());

        Optional<ActiveGameDto> result = gameSessionService.getActiveGame("user@test.com");

        assertTrue(result.isEmpty());
    }

    // ── surrender ──

    @Test
    void shouldThrowGameNotFoundException_whenSurrenderSessionNotFound() {
        UUID gameId = UUID.randomUUID();

        assertThrows(GameNotFoundException.class,
                () -> gameSessionService.surrender(gameId, "user@test.com"));
    }

    @Test
    void shouldThrowInvalidPhaseException_whenGameOver() {
        UUID gameId = UUID.randomUUID();
        injectPlayPhaseSession(gameId);

        GameStateDto mockDto = makeGameStateDto(gameId);
        when(mapper.toGameStateDto(any(), any(), any(), anyInt(), anyInt(), any(), any(), anyBoolean()))
                .thenReturn(mockDto);

        // First surrender sets game over
        gameSessionService.surrender(gameId, "p1@test.com");

        // Second surrender should throw
        assertThrows(InvalidPhaseCommandException.class,
                () -> gameSessionService.surrender(gameId, "p1@test.com"));
    }

    @Test
    void shouldCallEngineSurrender_andBroadcast() {
        UUID gameId = UUID.randomUUID();
        injectPlayPhaseSession(gameId);

        GameStateDto mockDto = makeGameStateDto(gameId);
        when(mapper.toGameStateDto(any(), any(), any(), anyInt(), anyInt(), any(), any(), anyBoolean()))
                .thenReturn(mockDto);

        gameSessionService.surrender(gameId, "p1@test.com");

        verify(broadcastService, atLeastOnce()).broadcastState(any(), any(), any(), any());
    }

    // ── Helpers ──

    private void injectPlayPhaseSession(UUID gameId) {
        SessionContext ctx = makeSessionContext("p1@test.com", "p2@test.com");
        ctx.gameState().setPhase(GamePhase.REDRAW);
        ctx.gameState().setPhase(GamePhase.PLAY);
        sessionRegistry.putSession(gameId, ctx);
    }

    private void stubDeckBuilder(UUID deckId, Faction faction, LeaderAbility leaderAbility) {
        Card leader = makeLeaderCard("leader_" + faction.name().toLowerCase(), faction, leaderAbility);
        when(gameDeckBuilder.buildLeaderFromDeckId(deckId)).thenReturn(leader);

        List<Card> cards = new ArrayList<>();
        for (int i = 1; i <= 22; i++) {
            cards.add(makeCard(faction.name().toLowerCase() + "_card_" + i, faction));
        }
        when(gameDeckBuilder.buildDeckFromDeckId(deckId)).thenReturn(cards);
    }
}
