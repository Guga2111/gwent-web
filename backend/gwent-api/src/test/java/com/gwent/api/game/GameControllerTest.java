package com.gwent.api.game;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gwent.api.game.dto.*;
import com.gwent.api.game.exception.GameNotFoundException;
import com.gwent.api.game.exception.GameNotWaitingException;
import com.gwent.api.shared.TestSecurityConfig;
import com.gwent.api.shared.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;

import static com.gwent.api.shared.TestDataFactory.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GameController.class)
@ActiveProfiles("test")
@Import({TestSecurityConfig.class, GlobalExceptionHandler.class})
class GameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private GameSessionService gameSessionService;

    @MockitoBean
    private SimpMessagingTemplate messagingTemplate;

    // ── POST /api/games ──

    @Test
    @WithMockUser(username = "test@test.com")
    void shouldReturn201_withCreateGameDto() throws Exception {
        UUID gameId = UUID.randomUUID();
        UUID deckId = UUID.randomUUID();
        when(gameSessionService.createSession(eq("test@test.com"), any(UUID.class)))
                .thenReturn(makeCreateGameDto(gameId, "test@test.com"));

        mockMvc.perform(post("/api/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateGameRequest(deckId))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.gameId").value(gameId.toString()))
                .andExpect(jsonPath("$.playerId").value("test@test.com"));
    }

    // ── POST /api/games/{id}/join ──

    @Test
    @WithMockUser(username = "test@test.com")
    void shouldReturn200_withGameStateDtoOnJoin() throws Exception {
        UUID gameId = UUID.randomUUID();
        UUID deckId = UUID.randomUUID();
        when(gameSessionService.joinSession(eq(gameId), eq("test@test.com"), any(UUID.class)))
                .thenReturn(makeGameStateDto(gameId));

        mockMvc.perform(post("/api/games/" + gameId + "/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new JoinGameRequest(deckId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").value(gameId.toString()));
    }

    @Test
    @WithMockUser(username = "test@test.com")
    void shouldReturn404_whenJoinGameNotFound() throws Exception {
        UUID gameId = UUID.randomUUID();
        UUID deckId = UUID.randomUUID();
        when(gameSessionService.joinSession(eq(gameId), eq("test@test.com"), any(UUID.class)))
                .thenThrow(new GameNotFoundException(gameId));

        mockMvc.perform(post("/api/games/" + gameId + "/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new JoinGameRequest(deckId))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("GAME_NOT_FOUND"));
    }

    @Test
    @WithMockUser(username = "test@test.com")
    void shouldReturn409_whenGameNotWaiting() throws Exception {
        UUID gameId = UUID.randomUUID();
        UUID deckId = UUID.randomUUID();
        when(gameSessionService.joinSession(eq(gameId), eq("test@test.com"), any(UUID.class)))
                .thenThrow(new GameNotWaitingException(gameId));

        mockMvc.perform(post("/api/games/" + gameId + "/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new JoinGameRequest(deckId))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("GAME_NOT_WAITING"));
    }

    // ── GET /api/games/{id} ──

    @Test
    @WithMockUser(username = "test@test.com")
    void shouldReturn200_withGameStateDto() throws Exception {
        UUID gameId = UUID.randomUUID();
        when(gameSessionService.getSession(gameId, "test@test.com"))
                .thenReturn(makeGameStateDto(gameId));

        mockMvc.perform(get("/api/games/" + gameId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").value(gameId.toString()));
    }

    @Test
    @WithMockUser(username = "test@test.com")
    void shouldReturn404_whenGameNotFound() throws Exception {
        UUID gameId = UUID.randomUUID();
        when(gameSessionService.getSession(gameId, "test@test.com"))
                .thenThrow(new GameNotFoundException(gameId));

        mockMvc.perform(get("/api/games/" + gameId))
                .andExpect(status().isNotFound());
    }

    // ── GET /api/games/active ──

    @Test
    @WithMockUser(username = "test@test.com")
    void shouldReturn200_whenActiveGameExists() throws Exception {
        UUID gameId = UUID.randomUUID();
        when(gameSessionService.getActiveGame("test@test.com"))
                .thenReturn(Optional.of(makeActiveGameDto(gameId)));

        mockMvc.perform(get("/api/games/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").value(gameId.toString()));
    }

    @Test
    @WithMockUser(username = "test@test.com")
    void shouldReturn204_whenNoActiveGame() throws Exception {
        when(gameSessionService.getActiveGame("test@test.com"))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/games/active"))
                .andExpect(status().isNoContent());
    }

    // ── POST /api/games/{id}/surrender ──

    @Test
    @WithMockUser(username = "test@test.com")
    void shouldReturn200_whenSurrenderSucceeds() throws Exception {
        UUID gameId = UUID.randomUUID();
        doNothing().when(gameSessionService).surrender(gameId, "test@test.com");

        mockMvc.perform(post("/api/games/" + gameId + "/surrender"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "test@test.com")
    void shouldReturn404_whenSurrenderGameNotFound() throws Exception {
        UUID gameId = UUID.randomUUID();
        doThrow(new GameNotFoundException(gameId))
                .when(gameSessionService).surrender(gameId, "test@test.com");

        mockMvc.perform(post("/api/games/" + gameId + "/surrender"))
                .andExpect(status().isNotFound());
    }
}
