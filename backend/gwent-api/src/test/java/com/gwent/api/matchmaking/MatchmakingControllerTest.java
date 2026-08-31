package com.gwent.api.matchmaking;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gwent.api.matchmaking.dto.JoinQueueRequest;
import com.gwent.api.shared.TestSecurityConfig;
import com.gwent.api.shared.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MatchmakingController.class)
@ActiveProfiles("test")
@Import({TestSecurityConfig.class, GlobalExceptionHandler.class})
class MatchmakingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MatchmakingService matchmakingService;

    @Test
    @WithMockUser(username = "test@test.com")
    void shouldReturn200_whenMatchFound() throws Exception {
        UUID gameId = UUID.randomUUID();
        UUID deckId = UUID.randomUUID();
        when(matchmakingService.joinQueue(eq("test@test.com"), any(UUID.class)))
                .thenReturn(Optional.of(gameId));

        mockMvc.perform(post("/api/matchmaking/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new JoinQueueRequest(deckId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").value(gameId.toString()));
    }

    @Test
    @WithMockUser(username = "test@test.com")
    void shouldReturn202_whenQueued() throws Exception {
        UUID deckId = UUID.randomUUID();
        when(matchmakingService.joinQueue(eq("test@test.com"), any(UUID.class)))
                .thenReturn(Optional.empty());

        mockMvc.perform(post("/api/matchmaking/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new JoinQueueRequest(deckId))))
                .andExpect(status().isAccepted());
    }

    @Test
    @WithMockUser(username = "test@test.com")
    void shouldReturn409_whenAlreadyInQueue() throws Exception {
        UUID deckId = UUID.randomUUID();
        when(matchmakingService.joinQueue(eq("test@test.com"), any(UUID.class)))
                .thenThrow(new AlreadyInQueueException());

        mockMvc.perform(post("/api/matchmaking/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new JoinQueueRequest(deckId))))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(username = "test@test.com")
    void shouldReturn200_whenLeaveQueueSucceeds() throws Exception {
        when(matchmakingService.leaveQueue("test@test.com")).thenReturn(true);

        mockMvc.perform(post("/api/matchmaking/leave"))
                .andExpect(status().isOk());
    }
}
