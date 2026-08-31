package com.gwent.api.deck;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gwent.api.deck.dto.DeckCardEntryDto;
import com.gwent.api.deck.dto.DeckDto;
import com.gwent.api.deck.dto.SaveDeckRequest;
import com.gwent.api.deck.exception.DeckNotFoundException;
import com.gwent.api.shared.TestSecurityConfig;
import com.gwent.api.shared.exception.GlobalExceptionHandler;
import com.gwent.engine.domain.Faction;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static com.gwent.api.shared.TestDataFactory.makeValidSaveDeckRequest;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DeckController.class)
@ActiveProfiles("test")
@Import({TestSecurityConfig.class, GlobalExceptionHandler.class})
class DeckControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DeckService deckService;

    // ── GET /api/decks ──

    @Test
    @WithMockUser(username = "test@test.com")
    void shouldReturn200_withDeckList() throws Exception {
        DeckDto deck = makeDeckDto();
        when(deckService.getUserDecks("test@test.com")).thenReturn(List.of(deck));

        mockMvc.perform(get("/api/decks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test Deck"));
    }

    @Test
    @WithMockUser(username = "test@test.com")
    void shouldReturn200_withEmptyList() throws Exception {
        when(deckService.getUserDecks("test@test.com")).thenReturn(List.of());

        mockMvc.perform(get("/api/decks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    // ── GET /api/decks/{id} ──

    @Test
    @WithMockUser(username = "test@test.com")
    void shouldReturn200_withDeckDto() throws Exception {
        UUID deckId = UUID.randomUUID();
        DeckDto deck = makeDeckDto(deckId);
        when(deckService.getDeck(deckId, "test@test.com")).thenReturn(deck);

        mockMvc.perform(get("/api/decks/" + deckId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Deck"));
    }

    @Test
    @WithMockUser(username = "test@test.com")
    void shouldReturn404_whenDeckNotFound() throws Exception {
        UUID deckId = UUID.randomUUID();
        when(deckService.getDeck(deckId, "test@test.com"))
                .thenThrow(new DeckNotFoundException(deckId));

        mockMvc.perform(get("/api/decks/" + deckId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("DECK_NOT_FOUND"));
    }

    // ── POST /api/decks ──

    @Test
    @WithMockUser(username = "test@test.com")
    void shouldReturn201_withCreatedDeck() throws Exception {
        SaveDeckRequest request = makeValidSaveDeckRequest(Faction.NORTHERN_REALMS);
        DeckDto createdDeck = makeDeckDto();
        when(deckService.createDeck(eq("test@test.com"), any(SaveDeckRequest.class))).thenReturn(createdDeck);

        mockMvc.perform(post("/api/decks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Deck"));
    }

    @Test
    @WithMockUser(username = "test@test.com")
    void shouldReturn400_whenValidationFails() throws Exception {
        SaveDeckRequest request = makeValidSaveDeckRequest(Faction.NORTHERN_REALMS);
        when(deckService.createDeck(eq("test@test.com"), any(SaveDeckRequest.class)))
                .thenThrow(new IllegalArgumentException("Deck name is required"));

        mockMvc.perform(post("/api/decks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_REQUEST"));
    }

    @Test
    @WithMockUser(username = "test@test.com")
    void shouldReturn400_whenDeckNameBlank() throws Exception {
        SaveDeckRequest request = new SaveDeckRequest("", Faction.NORTHERN_REALMS, "leader", List.of());
        when(deckService.createDeck(eq("test@test.com"), any(SaveDeckRequest.class)))
                .thenThrow(new IllegalArgumentException("Deck name is required"));

        mockMvc.perform(post("/api/decks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ── PUT /api/decks/{id} ──

    @Test
    @WithMockUser(username = "test@test.com")
    void shouldReturn200_withUpdatedDeck() throws Exception {
        UUID deckId = UUID.randomUUID();
        SaveDeckRequest request = makeValidSaveDeckRequest(Faction.NORTHERN_REALMS);
        DeckDto updatedDeck = makeDeckDto(deckId);
        when(deckService.updateDeck(eq(deckId), eq("test@test.com"), any(SaveDeckRequest.class)))
                .thenReturn(updatedDeck);

        mockMvc.perform(put("/api/decks/" + deckId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Deck"));
    }

    @Test
    @WithMockUser(username = "test@test.com")
    void shouldReturn404_whenUpdateNotFound() throws Exception {
        UUID deckId = UUID.randomUUID();
        SaveDeckRequest request = makeValidSaveDeckRequest(Faction.NORTHERN_REALMS);
        when(deckService.updateDeck(eq(deckId), eq("test@test.com"), any(SaveDeckRequest.class)))
                .thenThrow(new DeckNotFoundException(deckId));

        mockMvc.perform(put("/api/decks/" + deckId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "test@test.com")
    void shouldReturn400_whenUpdateValidationFails() throws Exception {
        UUID deckId = UUID.randomUUID();
        SaveDeckRequest request = makeValidSaveDeckRequest(Faction.NORTHERN_REALMS);
        when(deckService.updateDeck(eq(deckId), eq("test@test.com"), any(SaveDeckRequest.class)))
                .thenThrow(new IllegalArgumentException("Invalid"));

        mockMvc.perform(put("/api/decks/" + deckId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ── DELETE /api/decks/{id} ──

    @Test
    @WithMockUser(username = "test@test.com")
    void shouldReturn204_whenDeleted() throws Exception {
        UUID deckId = UUID.randomUUID();
        doNothing().when(deckService).deleteDeck(deckId, "test@test.com");

        mockMvc.perform(delete("/api/decks/" + deckId))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "test@test.com")
    void shouldReturn404_whenDeleteNotFound() throws Exception {
        UUID deckId = UUID.randomUUID();
        doThrow(new DeckNotFoundException(deckId)).when(deckService).deleteDeck(deckId, "test@test.com");

        mockMvc.perform(delete("/api/decks/" + deckId))
                .andExpect(status().isNotFound());
    }

    // ── Helper ──

    private DeckDto makeDeckDto() {
        return makeDeckDto(UUID.randomUUID());
    }

    private DeckDto makeDeckDto(UUID id) {
        return new DeckDto(id, "Test Deck", Faction.NORTHERN_REALMS, "leader_nr",
                List.of(new DeckCardEntryDto("card_1", 3)), LocalDateTime.now());
    }
}
