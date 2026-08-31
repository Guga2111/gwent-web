package com.gwent.api.matchmaking;

import com.gwent.api.game.GameSessionService;
import com.gwent.api.game.dto.CreateGameDto;
import com.gwent.api.game.dto.GameStateDto;
import com.gwent.api.matchmaking.dto.MatchFoundDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Optional;
import java.util.UUID;

import static com.gwent.api.shared.TestDataFactory.makeGameStateDto;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatchmakingServiceTest {

    @Mock
    private GameSessionService gameSessionService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private MatchmakingService matchmakingService;

    // ── joinQueue ──

    @Test
    void shouldReturnEmpty_whenQueueIsEmpty() {
        UUID deckId = UUID.randomUUID();

        Optional<UUID> result = matchmakingService.joinQueue("player1@test.com", deckId);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnGameId_whenOpponentWaiting() {
        UUID deck1 = UUID.randomUUID();
        UUID deck2 = UUID.randomUUID();
        UUID gameId = UUID.randomUUID();

        matchmakingService.joinQueue("player1@test.com", deck1);

        when(gameSessionService.createSession("player1@test.com", deck1))
                .thenReturn(new CreateGameDto(gameId, "player1@test.com"));
        when(gameSessionService.joinSession(eq(gameId), eq("player2@test.com"), eq(deck2)))
                .thenReturn(makeGameStateDto(gameId));

        Optional<UUID> result = matchmakingService.joinQueue("player2@test.com", deck2);

        assertTrue(result.isPresent());
        assertEquals(gameId, result.get());
    }

    @Test
    void shouldNotifyWaitingPlayer_whenMatchFormed() {
        UUID deck1 = UUID.randomUUID();
        UUID deck2 = UUID.randomUUID();
        UUID gameId = UUID.randomUUID();

        matchmakingService.joinQueue("player1@test.com", deck1);

        when(gameSessionService.createSession("player1@test.com", deck1))
                .thenReturn(new CreateGameDto(gameId, "player1@test.com"));
        when(gameSessionService.joinSession(eq(gameId), eq("player2@test.com"), eq(deck2)))
                .thenReturn(makeGameStateDto(gameId));

        matchmakingService.joinQueue("player2@test.com", deck2);

        verify(messagingTemplate).convertAndSend(
                eq("/topic/matchmaking/player1@test.com"),
                any(MatchFoundDto.class));
    }

    @Test
    void shouldThrowAlreadyInQueueException_whenAlreadyQueued() {
        UUID deckId = UUID.randomUUID();
        matchmakingService.joinQueue("player1@test.com", deckId);

        assertThrows(AlreadyInQueueException.class,
                () -> matchmakingService.joinQueue("player1@test.com", deckId));
    }

    // ── leaveQueue ──

    @Test
    void shouldReturnTrue_whenPlayerInQueue() {
        UUID deckId = UUID.randomUUID();
        matchmakingService.joinQueue("player1@test.com", deckId);

        boolean result = matchmakingService.leaveQueue("player1@test.com");

        assertTrue(result);
    }

    @Test
    void shouldReturnFalse_whenPlayerNotInQueue() {
        boolean result = matchmakingService.leaveQueue("nobody@test.com");

        assertFalse(result);
    }

    @Test
    void shouldRemovePlayer_soNextJoinerWaits() {
        UUID deckId = UUID.randomUUID();
        matchmakingService.joinQueue("player1@test.com", deckId);
        matchmakingService.leaveQueue("player1@test.com");

        Optional<UUID> result = matchmakingService.joinQueue("player2@test.com", UUID.randomUUID());

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldCreateAndJoinSession_whenMatchFormed() {
        UUID deck1 = UUID.randomUUID();
        UUID deck2 = UUID.randomUUID();
        UUID gameId = UUID.randomUUID();

        matchmakingService.joinQueue("player1@test.com", deck1);

        when(gameSessionService.createSession("player1@test.com", deck1))
                .thenReturn(new CreateGameDto(gameId, "player1@test.com"));
        when(gameSessionService.joinSession(eq(gameId), eq("player2@test.com"), eq(deck2)))
                .thenReturn(makeGameStateDto(gameId));

        matchmakingService.joinQueue("player2@test.com", deck2);

        verify(gameSessionService).createSession("player1@test.com", deck1);
        verify(gameSessionService).joinSession(gameId, "player2@test.com", deck2);
    }
}
