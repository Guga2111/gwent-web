package com.gwent.api.user;

import com.gwent.api.shared.TestSecurityConfig;
import com.gwent.api.shared.exception.GlobalExceptionHandler;
import com.gwent.api.user.dto.UserMeDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@ActiveProfiles("test")
@Import({TestSecurityConfig.class, GlobalExceptionHandler.class})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    @WithMockUser(username = "test@test.com")
    void shouldReturn200_withUserMeDto() throws Exception {
        when(userService.getMe("test@test.com"))
                .thenReturn(new UserMeDto("test@test.com", "tester", false));

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@test.com"))
                .andExpect(jsonPath("$.username").value("tester"))
                .andExpect(jsonPath("$.hasSeenTutorial").value(false));
    }

    @Test
    @WithMockUser(username = "missing@test.com")
    void shouldReturn404_whenUserNotFound() throws Exception {
        when(userService.getMe("missing@test.com"))
                .thenThrow(new UserNotFoundException("missing@test.com"));

        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "test@test.com")
    void shouldReturn204_whenTutorialSeenSuccessful() throws Exception {
        doNothing().when(userService).markTutorialSeen("test@test.com");

        mockMvc.perform(post("/api/users/me/tutorial-seen"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "missing@test.com")
    void shouldReturn404_whenTutorialSeenUserNotFound() throws Exception {
        doThrow(new UserNotFoundException("missing@test.com"))
                .when(userService).markTutorialSeen("missing@test.com");

        mockMvc.perform(post("/api/users/me/tutorial-seen"))
                .andExpect(status().isNotFound());
    }
}
