package com.gwent.api.game;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gwent.api.catalog.CardCatalogRepository;
import com.gwent.api.catalog.CardEntity;
import com.gwent.api.deck.Deck;
import com.gwent.api.deck.DeckCardEntry;
import com.gwent.api.deck.DeckRepository;
import com.gwent.api.game.dto.*;
import com.gwent.api.game.exception.GameNotFoundException;
import com.gwent.api.game.exception.GameNotWaitingException;
import com.gwent.api.game.exception.PlayerNotInGameException;
import com.gwent.engine.domain.*;
import com.gwent.engine.state.GameState;
import com.gwent.engine.exception.command.InvalidPhaseCommandException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static com.gwent.api.shared.TestDataFactory.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameSessionServiceTest {

    @Mock
    private GameRepository gameRepository;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private GameModelMapper mapper;

    @Mock
    private CardCatalogRepository cardCatalogRepository;

    @Mock
    private DeckRepository deckRepository;

    @InjectMocks
    private GameSessionService gameSessionService;

    // ── createSession ──

    @Test
    void shouldCreateGameWithWaitingStatus() {
        UUID deckId = UUID.randomUUID();
        when(gameRepository.save(any(Game.class))).thenAnswer(inv -> inv.getArgument(0));

        gameSessionService.createSession("user@test.com", deckId);

        verify(gameRepository).save(argThat(game ->
                game.getStatus() == GameStatus.WAITING &&
                        "user@test.com".equals(game.getPlayer1Id()) &&
                        deckId.equals(game.getPlayer1DeckId())
        ));
    }

    @Test
    void shouldReturnCreateGameDtoWithPlayerAndGameId() {
        UUID deckId = UUID.randomUUID();
        when(gameRepository.save(any(Game.class))).thenAnswer(inv -> inv.getArgument(0));

        CreateGameDto result = gameSessionService.createSession("user@test.com", deckId);

        assertNotNull(result.gameId());
        assertEquals("user@test.com", result.playerId());
    }

    // ── joinSession ──

    @Test
    void shouldThrowGameNotFoundException_whenJoinNotFound() {
        UUID gameId = UUID.randomUUID();
        when(gameRepository.findById(gameId)).thenReturn(Optional.empty());

        assertThrows(GameNotFoundException.class,
                () -> gameSessionService.joinSession(gameId, "user@test.com", UUID.randomUUID()));
    }

    @Test
    void shouldThrowGameNotWaitingException_whenNotWaiting() {
        UUID gameId = UUID.randomUUID();
        Game game = new Game();
        game.setId(gameId);
        game.setStatus(GameStatus.IN_PROGRESS);
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));

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
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));

        stubDeckAndCards(deck1Id, Faction.NORTHERN_REALMS, LeaderAbility.SIEGE_MASTER);
        stubDeckAndCards(deck2Id, Faction.NILFGAARD, LeaderAbility.EMPEROR_OF_NILFGAARD);

        GameStateDto mockDto = makeGameStateDto(gameId);
        when(mapper.toGameStateDto(any(), any(), any(), anyInt(), anyInt(), any(), any(), anyBoolean()))
                .thenReturn(mockDto);

        GameStateDto result = gameSessionService.joinSession(gameId, "p2@test.com", deck2Id);

        assertNotNull(result);
        verify(messagingTemplate, atLeastOnce()).convertAndSend(anyString(), any(GameStateDto.class));
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
        when(gameRepository.findById(gameId)).thenReturn(Optional.empty());

        assertThrows(GameNotFoundException.class,
                () -> gameSessionService.getSession(gameId, "user@test.com"));
    }

    @Test
    void shouldThrowGameNotFoundException_whenNoStateJson() {
        UUID gameId = UUID.randomUUID();
        Game game = new Game();
        game.setId(gameId);
        game.setStateJson(null);
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));

        assertThrows(GameNotFoundException.class,
                () -> gameSessionService.getSession(gameId, "user@test.com"));
    }

    @Test
    void shouldReturnDto_whenSessionInMemory() {
        UUID gameId = UUID.randomUUID();
        UUID deck1Id = UUID.randomUUID();
        UUID deck2Id = UUID.randomUUID();

        // Set up an in-memory session via joinSession
        Game game = new Game();
        game.setId(gameId);
        game.setPlayer1Id("p1@test.com");
        game.setPlayer1DeckId(deck1Id);
        game.setStatus(GameStatus.WAITING);
        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));

        stubDeckAndCards(deck1Id, Faction.NORTHERN_REALMS, LeaderAbility.SIEGE_MASTER);
        stubDeckAndCards(deck2Id, Faction.NILFGAARD, LeaderAbility.EMPEROR_OF_NILFGAARD);

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
        Game game = new Game();
        game.setId(UUID.randomUUID());
        when(gameRepository.findActiveGameForPlayer(GameStatus.IN_PROGRESS, "user@test.com"))
                .thenReturn(Optional.of(game));

        Optional<ActiveGameDto> result = gameSessionService.getActiveGame("user@test.com");

        assertTrue(result.isPresent());
        assertEquals(game.getId(), result.get().gameId());
    }

    @Test
    void shouldReturnEmpty_whenNoActiveGame() {
        when(gameRepository.findActiveGameForPlayer(GameStatus.IN_PROGRESS, "user@test.com"))
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
    void shouldThrowInvalidPhaseException_whenGameOver() throws Exception {
        UUID gameId = UUID.randomUUID();
        SessionContext ctx = injectPlayPhaseSession(gameId);

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
    void shouldCallEngineSurrender_andBroadcast() throws Exception {
        UUID gameId = UUID.randomUUID();
        injectPlayPhaseSession(gameId);

        GameStateDto mockDto = makeGameStateDto(gameId);
        when(mapper.toGameStateDto(any(), any(), any(), anyInt(), anyInt(), any(), any(), anyBoolean()))
                .thenReturn(mockDto);

        gameSessionService.surrender(gameId, "p1@test.com");

        // Verify broadcast was called for both players
        verify(messagingTemplate, atLeast(2)).convertAndSend(anyString(), any(GameStateDto.class));
    }

    // ── Helpers ──

    @SuppressWarnings("unchecked")
    private SessionContext injectPlayPhaseSession(UUID gameId) throws Exception {
        SessionContext ctx = makeSessionContext("p1@test.com", "p2@test.com");
        ctx.gameState().setPhase(GamePhase.REDRAW);
        ctx.gameState().setPhase(GamePhase.PLAY);

        Field sessionsField = GameSessionService.class.getDeclaredField("sessions");
        sessionsField.setAccessible(true);
        Map<UUID, SessionContext> sessions = (Map<UUID, SessionContext>) sessionsField.get(gameSessionService);
        sessions.put(gameId, ctx);

        return ctx;
    }

    private void stubDeckAndCards(UUID deckId, Faction faction, LeaderAbility leaderAbility) {
        Deck deck = new Deck();
        deck.setId(deckId);
        deck.setLeaderId("leader_" + faction.name().toLowerCase());
        List<DeckCardEntry> entries = new ArrayList<>();
        for (int i = 1; i <= 22; i++) {
            entries.add(new DeckCardEntry(faction.name().toLowerCase() + "_card_" + i, 1));
        }
        deck.setCards(entries);
        deck.setFaction(faction);
        when(deckRepository.findById(deckId)).thenReturn(Optional.of(deck));

        CardEntity leaderEntity = makeLeaderCardEntity("leader_" + faction.name().toLowerCase(), faction, leaderAbility);
        when(cardCatalogRepository.findById("leader_" + faction.name().toLowerCase())).thenReturn(Optional.of(leaderEntity));

        for (int i = 1; i <= 22; i++) {
            String cardId = faction.name().toLowerCase() + "_card_" + i;
            CardEntity unitEntity = makeUnitCardEntity(cardId, faction);
            when(cardCatalogRepository.findById(cardId)).thenReturn(Optional.of(unitEntity));
        }
    }
}
