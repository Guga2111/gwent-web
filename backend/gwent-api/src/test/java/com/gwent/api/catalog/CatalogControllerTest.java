package com.gwent.api.catalog;

import com.gwent.api.shared.TestSecurityConfig;
import com.gwent.api.shared.exception.GlobalExceptionHandler;
import com.gwent.engine.domain.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.gwent.api.shared.TestDataFactory.makeUnitCardEntity;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CatalogController.class)
@ActiveProfiles("test")
@Import({TestSecurityConfig.class, GlobalExceptionHandler.class})
class CatalogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CardCatalogCache cardCatalogRepository;

    @Test
    @WithMockUser(username = "test@test.com")
    void shouldReturn200_withCards() throws Exception {
        CardEntity card = makeUnitCardEntity("nr_infantry", Faction.NORTHERN_REALMS);
        when(cardCatalogCache.getByFactionIn(anyList())).thenReturn(List.of(card));

        mockMvc.perform(get("/api/catalog").param("faction", "NORTHERN_REALMS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("nr_infantry"))
                .andExpect(jsonPath("$[0].faction").value("NORTHERN_REALMS"));
    }

    @Test
    @WithMockUser(username = "test@test.com")
    void shouldReturn200_withoutNeutral() throws Exception {
        CardEntity card = makeUnitCardEntity("nr_unit", Faction.NORTHERN_REALMS);
        when(cardCatalogCache.getByFactionIn(List.of(Faction.NORTHERN_REALMS)))
                .thenReturn(List.of(card));

        mockMvc.perform(get("/api/catalog")
                        .param("faction", "NORTHERN_REALMS")
                        .param("includeNeutral", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("nr_unit"));
    }

    @Test
    @WithMockUser(username = "test@test.com")
    void shouldReturn400_whenFactionMissing() throws Exception {
        mockMvc.perform(get("/api/catalog"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "test@test.com")
    void shouldReturn400_whenFactionInvalid() throws Exception {
        mockMvc.perform(get("/api/catalog").param("faction", "INVALID"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "test@test.com")
    void shouldReturn200_withEmptyList() throws Exception {
        when(cardCatalogCache.getByFactionIn(anyList())).thenReturn(List.of());

        mockMvc.perform(get("/api/catalog").param("faction", "NORTHERN_REALMS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }
}
