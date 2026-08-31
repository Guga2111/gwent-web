package com.gwent.api.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gwent.api.shared.TestSecurityConfig;
import com.gwent.api.shared.exception.GlobalExceptionHandler;
import com.gwent.api.user.User;
import com.gwent.api.user.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static com.gwent.api.shared.TestDataFactory.makeUser;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@ActiveProfiles("test")
@Import({TestSecurityConfig.class, GlobalExceptionHandler.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @Test
    void shouldReturn201_withRegisteredUser() throws Exception {
        User user = makeUser("new@test.com", "newuser");
        when(userService.registerUser("new@test.com", "newuser", "password123"))
                .thenReturn(user);

        RegisterRequest request = new RegisterRequest("new@test.com", "newuser", "password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("new@test.com"))
                .andExpect(jsonPath("$.username").value("newuser"));
    }

    @Test
    void shouldReturn400_whenRequestBodyMalformed() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("not-valid-json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldBeAccessibleWithoutAuthentication() throws Exception {
        User user = makeUser("anon@test.com", "anon");
        when(userService.registerUser("anon@test.com", "anon", "pass"))
                .thenReturn(user);

        RegisterRequest request = new RegisterRequest("anon@test.com", "anon", "pass");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }
}
